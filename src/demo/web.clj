(ns demo.web
  (:use ring.adapter.jetty)
  (:use ring.middleware.resource)
  (:use clojure.contrib.monads)
  (:use demo.utils)
  (:use demo.domain)
  (:use demo.net))

(def servers { "waking_fields" ["94.23.229.44" 7778] })

(defn ask [[ip port]]
  (with-open [socket (usend "\\basic\\" ip port )]
    (urecv socket)))

(defn extractInfo [message]
  (let [infos (filter #(not (empty? %)) (seq (.split message  "\\\\")))
        eveninfos (cutForEven infos)]
    (apply hash-map eveninfos)))

(defmacro link [target]
  `(str "<a href='/" ~target "'>" ~target "</a>"))

(def homepage
  (let [links (for [k (keys servers)] (link k) )]
  {:status 200 :headers {"Content-Type" "text/html"} :body links }))

(defn plainText [text]
  {:status 200 :headers {"Content-Type" "text/plain"} :body text})

(defn handler [req]
  (let [ path (jsonPath (:uri req))
        server (get servers path)]
    (with-monad maybe-m
      (m-plus (m-fmap #(plainText(toJsonStr(serverInfos(extractInfo (ask %))))) server) homepage ))))

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty (wrap-resource handler "/static/index" ) {:port port})))