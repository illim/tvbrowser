(ns tvb.web
  (:use ring.adapter.jetty)
  (:use ring.util.response)
  (:use clojure.contrib.monads)
  (:use tvb.utils)
  (:use tvb.domain)
  (:use tvb.net)
  (:import (com.sun.jna Native))
  (:import (java.io File)))

(defn basic [server]
  (uask server "\\basic\\"))

(defn plainText
  ([text] (plainText text 200))
  ([text status] {:status status :headers {"Content-Type" "text/plain"} :body text}))

(defn handleJsonPath [^String path]
  (let [ [ip port] (seq (.split path "/"))
         server [ip (Integer/parseInt port)] ]
    (with-monad maybe-m
      (orElse
       (m-fmap #(plainText(toJsonStr(serverInfos(infoMap (basic %))))) server)
       (plainText "Unknown path") ))))

(def statics
  (let [ m (mapResources "static")
         indexResource (get m "/index") ]
    (assoc m "/" indexResource)))

(defn handler [req]
  (orElse
   (get statics (:uri req))
   (try
     (coolDown handleJsonPath (jsonPath (:uri req)))
     (catch Throwable t (do
                          (.printStackTrace t)
                          (plainText (str (.getClass t) (.getMessage t)) 503))))))

(gen-interface
 :name jna.GSLibrary
 :extends [com.sun.jna.Library]
 :methods [[main [Integer #=(java.lang.Class/forName "[Ljava.lang.String;") ] Integer]])

(defmacro jna-call [lib func ret & args]
  `(let [library#  (name ~lib)
         function# (com.sun.jna.Function/getFunction library# ~func)]
     (.invoke function# ~ret (to-array [~@args]))))

(defmacro jna-malloc [size]
  `(let [buffer# (java.nio.ByteBuffer/allocateDirect ~size)
         pointer# (Native/getDirectBufferPointer buffer#)]
     (.order buffer# java.nio.ByteOrder/LITTLE_ENDIAN)
     {:pointer pointer# :buffer buffer#}))

(defn -main []
  (println (str "lib exists " (.exists (File. "/lib"))))
  (println (str "libc.so.6 exists " (.exists (File. "/lib/libc.so.6"))))
  (System/loadLibrary "c")
  (System/loadLibrary "gslist")
  (let [gs (Native/loadLibrary "gslist" jna.GSLibrary)
        args (into-array ["rr" "-n" "tribesv"])]
    (println (jna-call :gslist "main" Integer 2 args)))
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty handler {:port port})))
