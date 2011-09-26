(ns tvb.domain
  (:import (java.util.regex Pattern))
  (:use tvb.recordx)
  (:use [clojure.string :only [join]])
  (:use tvb.utils))

(defrecordx Player [name ping #^int score team])
(defrecordx Team   [name score])

(defrecord Admin [name email])
(defrecord Game  [map gametype team1 team2 players])
(defrecord Board [name admin game numplayers maxplayers password])


(defn toPlayers [infos team1 numplayers]
  (letfn [(toPlayer [idx] (xPlayer (map #(infos (str % "_" idx)) ["player" "ping" "score" "team"]) :coerce))
          (team-player [player]
            [(= (:name team1) (:team player)) (:score player) (:name player)])]
    (loop [result (sorted-set-by (->> compare (by team-player) flip) []) idx numplayers]
      (if (zero? idx)
        result
        (recur (conj result (toPlayer (- idx 1))) (dec idx))))))

(defn toScoreBoard [{:strs [numplayers maxplayers password mapname gametype hostname adminname adminemail] :as infos}]
  (letfn [(toTeam [idx] (xTeam (map #(infos (str "team" idx %)) ["" "score"])))]
    (let [nump          (Integer/parseInt numplayers)
          [team1 team2] [(toTeam "one") (toTeam "two")]
          players       (toPlayers infos team1 nump)]
      (Board. hostname
              (Admin. adminname adminemail)
              (Game. mapname gametype team1 team2 players)
              nump
              (Integer/parseInt maxplayers)
              (not= "0" password)))))


(defn extractNumPlayers [message]     (Integer/parseInt (second (re-find #".*\\numplayers\\([^\\]*)\\.*" message) )))
(defn toRegex           [attributes]  (str "\\\\" (join "\\\\(.*)\\\\" attributes) "\\\\(.*)\\\\final.*"))
(def serverInfoPattern #"(.*):(.*) \\hostname\\(.*)\\numplayers\\(.*)\\maxplayers\\(.*)\\mapname\\(.*)")
(def baseAttributes
   [ "mapname", "numplayers", "maxplayers", "hostname", "hostport", "gametype", "gamever", "password", "gamename", "gamemode", "teamone", "teamtwo", "teamonescore", "teamtwoscore", "adminname", "adminemail", "p","trackingstats", "dedicated" ])


(defn parseScores [message]
  (letfn [(playerAttributes [numPlayers]
            (for [suffix ["player", "ping", "score", "team"]
                  n      (range 0 numPlayers)]
              (str suffix "_" n)))]
    (let [numPlayers (extractNumPlayers message)
          attributes (into baseAttributes (playerAttributes numPlayers))
          regex      (toRegex attributes)
          infos      (rest (re-find (Pattern/compile regex) message)) ]
      (zipmap attributes infos))))


(defn parseServerInfo [message]
  (let [infos      (rest (re-find serverInfoPattern message))
        attributes ["ip" "port" "hostname" "numplayers" "maxplayers" "mapname"]]
    (zipmap attributes infos)))