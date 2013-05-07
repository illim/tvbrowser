(ns tvb.gs
  (:import (com.sun.jna Native))
  (:use tvb.domain))

(gen-interface
 :name tvb.gs.GSCallback
 :extends [com.sun.jna.Callback]
 :methods [[invoke [String] void]])

(gen-interface
 :name tvb.gs.GSLibrary
 :extends [com.sun.jna.Library]
 :methods [[main_with_cb [Integer #=(java.lang.Class/forName "[Ljava.lang.String;") tvb.gs.GSCallback] Integer]])


(def initNativeLibs
  (System/loadLibrary "gslist"))

(def libgslist (Native/synchronizedLibrary (Native/loadLibrary "gslist" tvb.gs.GSLibrary)))

(defn listServers [gamename queryargs]
  (let [buf    (ref [])
        gsArgs (into-array ["fake" "-q" "-n" gamename "-X" queryargs])
        p      (proxy [tvb.gs.GSCallback] []
                 (invoke [message] (dosync (commute buf conj message))))]
    (do
      (.main_with_cb libgslist (Integer. 5) gsArgs p)
      @buf)))

(defn masterList []
  (let [gsTvServers (listServers "tribesv" "\\hostname\\numplayers\\maxplayers\\mapname")]
    (map parseServerInfo gsTvServers)))


(defn hardList []
  (let [servers  [
                  [ "94.225.99.179", "7778", "Cobra server", "999", "14", "X" ],
                  [ "64.27.56.209", "7778", "America", "999", "14", "X" ]]]
    (map #(zipmap serverAttr %) servers)))