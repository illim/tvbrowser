(ns tvb.utils
  (:use clojure.contrib.json)
  (:import (java.io PrintWriter StringWriter)))


(defmacro orElse [x y]
  `(let [z# ~x] (if z# z# ~y)))

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
  "Dummy cache that somewhat cools down the hammering of the game server if there's a lot of demands. There still is a possibility to flood the game server every 5 seconds."
  (dosync
   (let [compute (fn [] (let [newVal (apply f args)]
                          (commute coolDownMap assoc args [newVal (atom 0) (System/currentTimeMillis)])
                          newVal))
         tuple (get @coolDownMap args)]
     (if (nil? tuple)
       (compute)
       (let [[current dirty time] tuple
             delta (- (System/currentTimeMillis) time)
             swapAndCompute (fn [] (if (== 1 (swap! dirty (fn [x] (if (== x 0) 1 0)))) (compute) current))]
         (cond
          (and (> delta 2000) (< delta 5000) (== @dirty 0)) (swapAndCompute)
          (> delta 5000) (compute)
          :else current))))))
