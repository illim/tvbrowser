(ns demo.utils
  (:use clojure.contrib.json)
  (:import (java.io PrintWriter StringWriter)))
  
(defn toJsonStr [x]
  (with-open [sw (StringWriter.)
              pw (PrintWriter. sw)]
    (write-json x pw)
    (.toString sw)))

(defn jsonPath [path]
  (let [[[ _ path]] (re-seq #"/(.*)\.json" path )]
    path))

(def coolDownMap (ref {}))

(defn coolDown [f & args]
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
