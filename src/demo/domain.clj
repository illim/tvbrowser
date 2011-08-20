(ns demo.domain)

(defrecord Player [name ping score team])

(defrecord Team [name score])

(defrecord Admin [name email])

(defrecord Game [map gametype team1 team2 players])

(defrecord Server [admin game numplayers maxplayers password])

(defn getPlayer [infos idx]
  (Player. (get infos (str "player_" idx))
           (get infos (str "ping_" idx))
           (get infos (str "score_" idx))
           (get infos (str "team_" idx))))

(defn getPlayers [infos numplayers]
  (loop [result {} idx numplayers]
    (if (zero? idx)
      result
      (recur (conj result (getPlayer infos (- idx 1))) (dec idx)))))

(defn getTeam [infos idx]
  (Team. (get infos (str "team" idx))
         (get infos (str "team" idx "score"))))

(defn getAdmin [infos]
  (Admin. (get infos "adminname")
          (get infos "adminemail")))
          
(defn serverInfos [infos]
  (let [numplayers (Integer/parseInt (get infos "numplayers"))
        maxplayers (Integer/parseInt (get infos "maxplayers"))
        players    (getPlayers infos numplayers)
        [team1 team2] (map #(getTeam infos %) ["one" "two"])
        game       (Game. (get infos "mapname") (get infos "gametype") team1 team2 players)
        server     (Server. (getAdmin infos) game numplayers maxplayers (get infos "password")) ]
    server))


                              