(ns tvb.web
  (:use ring.adapter.jetty)
  (:use ring.util.response)
  (:use clojure.contrib.core)
  (:use tvb.utils)
  (:use tvb.domain)
  (:use tvb.net)
  (:use tvb.gs))

(def sendBasic (partial uask "\\basic\\"))

(defn asPlainText
  ([text] (asPlainText text 200))
  ([text status] {:status status :headers {"Content-Type" "text/plain"} :body text}))

(defn handleJsonPath [^String path]
  (let [ [ip port] (seq (.split path "/"))
         server [ip (Integer/parseInt port)] ]
    (orElse
     (-?> server sendBasic parseScores toScoreBoard toJsonStr asPlainText)
     (asPlainText "Unknown path") )))

(defn handleList []
  (letfn [(player-ip-port [{:strs [ip port numplayers]}] [(Integer/parseInt numplayers) ip port])]
    (let [gsTvServers (listServers "tribesv" "\\hostname\\numplayers\\maxplayers\\mapname")
          tvServers   (map #(parseServerInfo %) gsTvServers)]
    (->> tvServers (sort-by player-ip-port (flip compare)) toJsonStr asPlainText))))

(def statics
  (let [ m (resourcesFrom "static")
         home (m "/index") ]
    (assoc m "/" home)))

(def coolDownServerCache (ref {}))
(def coolDownServersCache (ref {}))

(defn handler [{uri :uri}]
  (orElse
   (statics uri)
   (cond
    (= "/list.json" uri) (coolDown coolDownServersCache handleList)
    :else
    (try
      (coolDown coolDownServerCache handleJsonPath (jsonPath uri))
      (catch Throwable t (do
                           (.printStackTrace t)
                           (asPlainText (str (.getClass t) (.getMessage t)) 503)))))))

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty handler {:port port})))

