(ns demo.web
  (:use ring.adapter.jetty)
  (:use clojure.contrib.monads)
  (:import (java.net InetAddress DatagramSocket DatagramPacket)))

(def servers { "Waking_fields" ["94.23.229.44" 7778] })

(defn usend [cmd ip port]
  (let [ address (InetAddress/getByName ip)
         socket  (new DatagramSocket)
         buf     (.getBytes cmd)
         packet  (new DatagramPacket buf (alength buf) address port) ]
    (.send socket packet)
    socket ))

(defn urecv [socket]
  (let [ bufsize 1024
         buf     (byte-array bufsize)
         packet  (new DatagramPacket buf bufsize) ]
    (doto socket
      (.setSoTimeout 5000 )
      (.setReceiveBufferSize bufsize)
      (.receive packet))
    (new String (.getData packet) 0 (.getLength packet) "UTF-8") ))

(defn ask [[ip port]]
  (with-open [socket (usend "\\basic\\" ip port )]
    (urecv socket)))

(defmacro link [target]
  `(str "<a href='/" ~target "'>" ~target "</a>"))

(def homepage
  (let [links (for [k (keys servers)] (link k) )]
  {:status 200 :headers {"Content-Type" "text/html"} :body links }))

(defn plainText [text]
  {:status 200 :headers {"Content-Type" "text/plain"} :body text})

(defn app [req]
  (let [ path (.substring (:uri req) 1 )
        server (get servers path)]
    (with-monad maybe-m
      (m-plus (m-fmap #(plainText(ask %)) server) homepage ))))


(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty app {:port port})))