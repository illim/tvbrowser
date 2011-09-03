(ns tvb.gs
  (:import (com.sun.jna Native)))

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
      (.main_with_cb libgslist 5 gsArgs p)
      @buf)))
