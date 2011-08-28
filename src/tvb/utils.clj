(ns tvb.utils
  (:use clojure.contrib.json)
  (:import (java.io PrintWriter StringWriter))
  (:import (java.io File)))


(defmacro orElse [x y]
  `(let [z# ~x] (if z# z# ~y)))


(defmacro mapResources [folder]
  (letfn [(toResourceResponseCall [fileName]
            (list 'resource-response (str "/" folder fileName)))]
    (let [ dir (File. (str "src/" folder))
          fileNames (map #(str "/" (.getName ^File %)) (seq (.listFiles dir))) ]
      (zipmap fileNames (map toResourceResponseCall fileNames)))))


(defn toJsonStr [x]
  (with-open [sw (StringWriter.)
              pw (PrintWriter. sw)]
    (write-json x pw)
    (.toString sw)))

(defn jsonPath [path]
  (let [[[ _ path]] (re-seq #"/(.*)\.json" path )]
    path))



(def ^{:private true} coolDownMap (ref {}))
(def ^{:private true} timeBeforeRecompute 4000)
(def ^{:private true} timeToLive 8000)

(defn coolDown [f & args]
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
   (let [tuple (get @coolDownMap args)]
     (if tuple
       (let [[current dirty time] tuple
             delta (- (System/currentTimeMillis) time)]
         (cond
          (and (> delta timeBeforeRecompute) (< delta timeToLive) (== @dirty 0)) (swapAndCompute dirty current)
          (> delta timeToLive) (compute)
          :else current))
       (compute))))))
