(ns tvb.domain
  (:import (java.util.regex Pattern))
  (:use tvb.utils)
  (:use clojure.contrib.monads))

(defrecord Player [name ping score team])

(defrecord Team [name score])

(defrecord Admin [name email])

(defrecord Game [map gametype team1 team2 players])

(defrecord Server [name admin game numplayers maxplayers password])

(defn getPlayer [infos idx]
  (Player. (get infos (str "player_" idx))
           (get infos (str "ping_" idx))
           (Integer/parseInt (get infos (str "score_" idx)))
           (get infos (str "team_" idx))))

(defn getPlayers [infos numplayers]
  (loop [result [] idx numplayers]
    (if (zero? idx)
      result
      (recur (conj result (getPlayer infos (- idx 1))) (dec idx)))))

(defn playerIndex [player]
  (domonad maybe-m
           [^Object team  (:team player)
            teamx (* 100 (.hashCode team))
            score (:score player) ] (- teamx score)))

(defn sortByTeamScore [players]
  (sort-by playerIndex players))

(defn getTeam [infos idx]
  (Team. (get infos (str "team" idx))
         (get infos (str "team" idx "score"))))

(defn getAdmin [infos]
  (Admin. (get infos "adminname")
          (get infos "adminemail")))
          
(defn serverInfos [infos]
  (let [numplayers (Integer/parseInt (get infos "numplayers"))
        maxplayers (Integer/parseInt (get infos "maxplayers"))
        players    (sortByTeamScore (getPlayers infos numplayers))
        [team1 team2] (map #(getTeam infos %) ["one" "two"])
        game       (Game. (get infos "mapname") (get infos "gametype") team1 team2 players)
        password   (not= (get infos "password") "0")
        server     (Server. (get infos "hostname") (getAdmin infos) game numplayers maxplayers password) ]
    server))



(defn extractNumPlayers [message]
  (Integer/parseInt (second (re-find #".*\\numplayers\\([^\\]*)\\.*" message) )))

(defn toPattern [args]
  (Pattern/compile (str "\\\\" (apply str (interpose "\\\\(.*)\\\\" args)) "\\\\(.*)\\\\final.*")))

(def baseArgs
   [ "mapname", "numplayers", "maxplayers", "hostname", "hostport", "gametype", "gamever", "password", "gamename", "gamemode", "gamevariant", "teamone", "teamtwo", "teamonescore", "teamtwoscore", "adminname", "adminemail", "p","trackingstats", "dedicated", "minver" ])

(defn playerArgs [numPlayers]
  (flatTabulates numPlayers (map #(fn [x] (str % "_" x)) ["player", "ping", "score", "team"])))

(defn infoMap [message]
  (let [numPlayers (extractNumPlayers message)
        args       (into baseArgs (playerArgs numPlayers))
        pattern    (toPattern args)
        infos      (rest (re-find pattern message)) ]
    (apply hash-map (interleave args infos))))
