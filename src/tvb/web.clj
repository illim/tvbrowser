(ns tvb.web
  (:use ring.adapter.jetty)
  (:use ring.util.response)
  (:use clojure.contrib.monads)
  (:use tvb.utils)
  (:use tvb.domain)
  (:use tvb.net)
  (:import (java.io File)))

(defn basic [server]
  (uask server "\\basic\\"))

(defmacro link [target]
  `(str "<a href='/" ~target "'>" ~target "</a>"))

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

(defn findStatics []
  (let [dir (File. "src/static")]
    (map #(.getName %) (seq (.listFiles dir)))))

(defmacro mapStatics []
  (let [res (findStatics)]
    (zipmap (map #(str "/" %) res) (map #(list 'resource-response (str "/static/" %)) res))))

(def statics
  (assoc (mapStatics) "/" (resource-response "/static/index" )))

(defn handler [req]
  (orElse
   (get statics (:uri req))
   (try
     (coolDown handleJsonPath (jsonPath (:uri req)))
     (catch Throwable t (do
                          (.printStackTrace t)
                          (plainText (str (.getClass t) (.getMessage t)) 503))))))

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty handler {:port port})))