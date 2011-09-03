(ns tvb.web
  (:use ring.adapter.jetty)
  (:use ring.util.response)
  (:use clojure.contrib.monads)
  (:use tvb.utils)
  (:use tvb.domain)
  (:use tvb.net)
  (:use tvb.gs))

(def basic (partial uask "\\basic\\"))

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

(defn handleList []
  (letfn [(index [{:strs [ip port numplayers]}] [(Integer/parseInt numplayers) ip port])]
    (let [gsTvServers   (listServers "tribesv" "\\hostname\\numplayers\\maxplayers\\mapname")
          tvServers     (map #(serverInfoMap %) gsTvServers)
          sortTvServers (into (sorted-set-by #(compare (index %2) (index %1))) tvServers)]
    (plainText (toJsonStr sortTvServers)))))

(def statics
  (let [ m (mapResources "static")
         indexResource (m "/index") ]
    (assoc m "/" indexResource)))

(def coolDownServerCache (ref {}))
(def coolDownServersCache (ref {}))

(defn handler [req]
  (orElse
   (statics (:uri req))
   (cond
    (= "/list.json" (:uri req)) (coolDown coolDownServersCache handleList)
    :else
    (try
      (coolDown coolDownServerCache handleJsonPath (jsonPath (:uri req)))
      (catch Throwable t (do
                           (.printStackTrace t)
                           (plainText (str (.getClass t) (.getMessage t)) 503)))))))

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty handler {:port port})))
