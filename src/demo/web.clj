(ns demo.web
  (:use ring.adapter.jetty)
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
        (new String (.getData packet) 0 (.getLength packet)) ))


(defn ask [[ip port]]
  (with-open [socket (usend "\\basic\\" ip port )]
    (urecv socket)))

(defn link [server]
  (str "<a href='/" server "'>" server "</a>"))

(def links (map link (keys servers)))

(defn plainText [text]
  {:status 200 :headers {"Content-Type" "text/plain"} :body text})

(defn app [req]
  (if (= "/" (:uri req))
    {:status 200 :headers {"Content-Type" "text/html"} :body links }
    (let [ path (.substring (:uri req) 1 )
           server (get servers path)]
      (if (nil? server)
        (plainText (str "Unknown " server))
        (plainText (ask server))))))


(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty app {:port port})))