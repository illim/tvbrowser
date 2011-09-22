(ns tvb.utils
  (:use clojure.contrib.core)
  (:use clojure.contrib.json)
  (:use clojure.contrib.def)
  (:import (java.io PrintWriter StringWriter))
  (:import (java.io File)))

(defn debug [value] (do (pr '>>)(pr value)(prn '<<) value))

(defmacro orElse [x y]
  `(if-let [z# ~x] z# ~y))


(defmacro resourcesFrom [folder]
  (letfn [(asResourceResponse [fileName]
            (list 'resource-response (str "/" folder fileName)))]
    (let [ dir (File. (str "src/" folder))
          fileNames (map #(str "/" (.getName ^File %)) (seq (.listFiles dir))) ]
      (zipmap fileNames (map asResourceResponse fileNames)))))

(defmacro flip [f]
  "flip f arguments"
  `(fn [& args#] (apply ~f (reverse args#))))

(defmacro by [f g]
  "map f to arguments of g"
  `(fn [& args#] (apply ~g (map ~f args#))))


(defn toJsonStr [x]
  (with-open [sw (StringWriter.)
              pw (PrintWriter. sw)]
    (write-json x pw)
    (.toString sw)))

(defn jsonPath [path]
  (let [[[ _ path]] (re-seq #"/(.*)\.json" path )]
    path))

(defvar- timeBeforeRecompute 4000)
(defvar- timeToLive 8000)

(defn coolDown [coolDownMap f & args]
  "Dummy cache that somewhat cools down the hammering of the game server if there's a lot of demands. There still is a possibility to flood the game server every 'timeToLive' seconds."
  (letfn [(compute
            ([] (compute (atom 0)))
            ([dirty]
               (let [newVal (apply f args)]
                 (commute coolDownMap assoc args [newVal dirty (System/currentTimeMillis)])
                 newVal)))
          (swapAndCompute [dirty current]
            (if (== 1 (swap! dirty #(if (== % 0) 1 0)))
              (compute dirty)
              current))]
  (dosync
   (if-let [[current dirty time] (@coolDownMap args)]
     (let [delta (- (System/currentTimeMillis) time)]
       (cond
        (and (> delta timeBeforeRecompute) (< delta timeToLive) (== @dirty 0)) (swapAndCompute dirty current)
        (>= delta timeToLive) (compute)
        :else current))
     (compute)))))
