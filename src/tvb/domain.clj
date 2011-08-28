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

(defn getTeam [infos idx]
  (Team. (get infos (str "team" idx))
         (get infos (str "team" idx "score"))))

(defn getPlayers [infos team1 numplayers]
  (letfn [(playerIndex [team1 player]
            (domonad maybe-m
                    [team  (:team player)
                     score (:score player) ] [(= (:name team1) team) score]))]
    (loop [result (sorted-set-by #(compare (playerIndex team1 %2) (playerIndex team1 %1)) []) idx numplayers]
      (if (zero? idx)
        result
        (recur (conj result (getPlayer infos (- idx 1))) (dec idx))))))

(defn serverInfos [{:strs [numplayers maxplayers password mapname gametype hostname adminname adminemail] :as infos}]
  (let [nump          (Integer/parseInt numplayers)
        [team1 team2] (map #(getTeam infos %) ["one" "two"])
        players       (getPlayers infos team1 nump)]
    (Server. hostname
             (Admin. adminname adminemail)
             (Game. mapname gametype team1 team2 players)
             nump
             (Integer/parseInt maxplayers)
             (not= "0" password))))


(defn extractNumPlayers [message]
  (Integer/parseInt (second (re-find #".*\\numplayers\\([^\\]*)\\.*" message) )))

(defn toPattern [attributes]
  (Pattern/compile (str "\\\\" (apply str (interpose "\\\\(.*)\\\\" attributes)) "\\\\(.*)\\\\final.*")))

(def baseAttributes
   [ "mapname", "numplayers", "maxplayers", "hostname", "hostport", "gametype", "gamever", "password", "gamename", "gamemode", "gamevariant", "teamone", "teamtwo", "teamonescore", "teamtwoscore", "adminname", "adminemail", "p","trackingstats", "dedicated", "minver" ])

(defn playerAttributes [numPlayers]
  (for [f (map #(fn [x] (str % "_" x)) ["player", "ping", "score", "team"])
        n (range 0 numPlayers)]
    (f n)))

(defn infoMap [message]
  (let [numPlayers (extractNumPlayers message)
        attributes (into baseAttributes (playerAttributes numPlayers))
        pattern    (toPattern attributes)
        infos      (rest (re-find pattern message)) ]
    (apply hash-map (interleave attributes infos))))
