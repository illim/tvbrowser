(ns tvb.recordx
  (:use tvb.utils)
  (:import (java.io File)))

(def parseInt #(Integer/parseInt %))
(defn parseArgs [parses args] (map-indexed #((nth parses %1) %2) args))

(def typeInfos
  {'int {:parse 'tvb.recordx/parseInt :default 0}
    nil {:parse 'clojure.core/identity :default nil}})


(defmacro defrecordx [typeName fields]
  "crappy functional constructor/parse"
  (let [consxName     (symbol (str "x" typeName))
        dummy         (symbol (str "xdummy-" typeName))
        fieldMetas    (map meta fields)
        consKeys      (map #(keyword (str %)) fields)
        defaults      (map #(:default (typeInfos (:tag %))) fieldMetas)
        argParses (map #(:parse (typeInfos (:tag %))) fieldMetas)]
    `(do
       (defrecord ~typeName ~fields)
       (def ~dummy (new ~typeName ~@defaults))
       (defn ~consxName [args# & opts#]
         (let [finalArgs# (if (some #{:coerce} opts#) (parseArgs [~@argParses] args#) args#)]
           (into ~dummy (zipmap '~consKeys finalArgs#))
           )))))
