(ns tvb.utils
  (:use clojure.data.json)
  (:import (java.io PrintWriter StringWriter))
  (:import (java.io File)))

(defn debug [value] (do (pr '>>)(pr value)(prn '<<) value))

(defmacro orElse [x y]
  `(if-let [z# ~x] z# ~y))

(defmacro flip [f]
  "flip f arguments"
  `(fn [& args#] (apply ~f (reverse args#))))

(defmacro by [f g]
  "map f to arguments of g"
  `(fn [& args#] (apply ~g (map ~f args#))))


(defn toJsonStr [x]
  (with-open [sw (StringWriter.)
              pw (PrintWriter. sw)]
    (write-json x pw true)
    (.toString sw)))

(defn jsonPath [path] (second (first (re-seq #"/(.*)\.json" path ))))


(defmacro resourcesFrom [folder]
  (letfn [(asResourceResponse [fileName]
            (list 'resource-response (str "/" folder fileName)))]
    (let [ dir (File. (str "src/" folder))
          fileNames (map #(str "/" (.getName ^File %)) (seq (.listFiles dir))) ]
      (zipmap fileNames (map asResourceResponse fileNames)))))


(def timeBeforeRecompute 4000)
(def timeToLive 8000)

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

(defn cooler [] (partial coolDown (ref {})))