(ns tvb.utils
  (:use clojure.contrib.json)
  (:import (java.io PrintWriter StringWriter))
  (:import (java.io File)))


(defmacro orElse [x y]
  `(let [z# ~x] (if z# z# ~y)))


(defmacro mapResources [folder]
  (let [ dir (File. (str "src/" folder))
         fileNames (map #(.getName ^File %) (seq (.listFiles dir))) ]
    (zipmap (map #(str "/" %) fileNames) (map #(list 'resource-response (str "/" folder "/" %)) fileNames))))


(defn toJsonStr [x]
  (with-open [sw (StringWriter.)
              pw (PrintWriter. sw)]
    (write-json x pw)
    (.toString sw)))

(defn jsonPath [path]
  (let [[[ _ path]] (re-seq #"/(.*)\.json" path )]
    path))



(def ^{:private true} coolDownMap (ref {}))

(defn coolDown [f & args]
  "Dummy cache that somewhat cools down the hammering of the game server if there's a lot of demands. There still is a possibility to flood the game server every 8 seconds."
  (dosync
   (let [compute (fn [dirty] (let [newVal (apply f args)]
                          (commute coolDownMap assoc args [newVal dirty (System/currentTimeMillis)])
                          newVal))
         tuple (get @coolDownMap args)]
     (if (nil? tuple)
       (compute (atom 0))
       (let [[current dirty time] tuple
             delta (- (System/currentTimeMillis) time)
             swapAndCompute (fn [] (if (== 1 (swap! dirty (fn [x] (if (== x 0) 1 0)))) (compute dirty) current))]
         (cond
          (and (> delta 4000) (< delta 8000) (== @dirty 0)) (swapAndCompute)
          (> delta 8000) (compute (atom 0))
          :else current))))))
