(ns demo.utils
  (:use clojure.contrib.json)
  (:import (java.io PrintWriter StringWriter)))
  
(defn cutForEven [infos]
  (let[ nbInfos (count infos)]
    (take (- nbInfos (mod nbInfos 2)) infos)))

(defn toJsonStr [x]
  (with-open [sw (StringWriter.)
              pw (PrintWriter. sw)]
    (write-json x pw)
    (.toString sw)))

(defn jsonPath [path]
  (let [[[ _ path]] (re-seq #"/(.*)\.json" path )]
    path))