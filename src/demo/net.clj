(ns demo.net
  (:import (java.net InetAddress DatagramSocket DatagramPacket)))

(defn usend [cmd ip port]
  (let [ address (InetAddress/getByName ip)
         socket  (DatagramSocket.)
         buf     (.getBytes cmd)
         packet  (DatagramPacket. buf (alength buf) address port) ]
    (.send socket packet)
    socket ))

(defn urecv [socket]
  (let [ bufsize 2048
         buf     (byte-array bufsize)
         packet  (DatagramPacket. buf bufsize) ]
    (doto socket
      (.setSoTimeout 5000 )
      (.setReceiveBufferSize bufsize)
      (.receive packet))
    (new String (.getData packet) 0 (.getLength packet) "UTF-8") ))