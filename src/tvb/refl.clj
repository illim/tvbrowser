(ns tvb.refl
  (:require edu.arizona.fnparse.cat)
  (:use tvb.utils)
  (:import (java.io File)))

(defn- metaArgs [forms]
  (letfn [(
           argsMeta [args] (map #(vector (str %) (meta %)) args)
            )]
    (let [recordForms (filter #(.equals (first %) 'defrecord) forms )]
      (into {} (map #(vector (str (second %)) (into (array-map) (argsMeta (nth % 2)))) recordForms) ))))

(defmacro recordArgs [f]
  (let [source (str "[" (slurp (File. (str "src/tvb/" f))) "]")
        forms (read-string source)]
    (metaArgs forms) ))


(defn cleanRefString [ref] (let [[[ _ clean ]] (re-seq #"(.*)@.*" ref )] clean))

(defn isInt [data]
  (and data (.equals (.getCanonicalName (class int)) (cleanRefString (str data)))))

(def intFormatter #(Integer/parseInt %))

(defn formatter [{data :tag}]
  (cond
   (isInt data) 'tvb.refl/intFormatter
   :else 'clojure.core/identity))

(defn default [{data :tag}]
  (cond
   (isInt data) 0
   :else nil))

(defn formatArgs [formatters args]
  (map-indexed #((nth formatters %1) %2) args))

(def domainRecordArgs (recordArgs "domain.clj") )

(defmacro construct [typeNameSym args]
  "Dummy functional constructor (record file/formatters hardcoded and no namespace management)"
  (let [typeName      (str typeNameSym)
        consArgs      (domainRecordArgs typeName)
        consKeys      (map #(keyword %) (keys consArgs))
        consVals      (vals consArgs)
        defaults      (map default consVals)
        argFormatters (map formatter consVals) ]
  `(into (new ~typeNameSym ~@defaults) (zipmap '~consKeys (formatArgs [~@argFormatters] ~args)))))
