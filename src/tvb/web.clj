(ns tvb.web
  (:use ring.adapter.jetty)
  (:use ring.util.response)
  (:use clojure.contrib.monads)
  (:use tvb.utils)
  (:use tvb.domain)
  (:use tvb.net))

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
      (m-plus (m-fmap #(plainText(toJsonStr(serverInfos(infoMap (basic %))))) server) (plainText "Unknown path") ))))

(defn handler [req]
  (cond
   (= "/" (:uri req)) (resource-response "/static/index")
   (= "/favicon.ico" (:uri req)) (resource-response "/static/favicon.ico")
   :else
   (try
     (coolDown handleJsonPath (jsonPath (:uri req)))
     (catch Throwable t (do
                          (.printStackTrace t)
                          (plainText (str (.getClass t) (.getMessage t)) 406))))))

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty handler {:port port})))