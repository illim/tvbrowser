(ns tvb.test
  (:use :reload-all tvb.web)
  (:use :reload-all tvb.utils)
  (:use :reload-all tvb.refl)
  (:use :reload-all tvb.domain))

(def ex0 "\\mapname\\Foregone (x2)\\numplayers\\10\\maxplayers\\16\\hostname\\The Waking Fields - Euro League\\hostport\\7777\\gametype\\CTF\\gamever\\63446\\password\\0\\gamename\\tribesv\\gamemode\\openplaying\\gamevariant\\0\\teamone\\Blood Eagle\\teamtwo\\Imperial\\teamonescore\\10\\teamtwoscore\\2\\adminname\\Yidbon\\adminemail\\http://tveurope.leaguecentral.info/\\p\\WebAdminKmA_INTERcEPTOR'VipaT.Juppelorddragon93sYnesthesiaTetsuoaT.WhiteMetaL\\hurlanTZhelatinel conejito|^'\\trackingstats\\False\\dedicated\\true\\minver\\63446\\player_0\\el conejito|^'\\player_1\\Zhelatin\\player_2\\MetaL\\hurlanT\\player_3\\aT.White\\player_4\\Tetsuo\\player_5\\sYnesthesia\\player_6\\lorddragon93\\player_7\\aT.Juppe\\player_8\\'Vip\\player_9\\KmA_INTERcEPTOR\\ping_0\\13\\ping_1\\35\\ping_2\\81\\ping_3\\41\\ping_4\\28\\ping_5\\42\\ping_6\\76\\ping_7\\50\\ping_8\\72\\ping_9\\62\\score_0\\2\\score_1\\20\\score_2\\26\\score_3\\10\\score_4\\9\\score_5\\24\\score_6\\0\\score_7\\26\\score_8\\9\\score_9\\47\\team_0\\Imperial\\team_1\\Imperial\\team_2\\Blood Eagle\\team_3\\Blood Eagle\\team_4\\Imperial\\team_5\\Blood Eagle\\team_6\\Imperial\\team_7\\Blood E")

(def ex "\\mapname\\MP-TIS3-[FE:L]-Frostbyte\\numplayers\\5\\maxplayers\\16\\hostname\\The Waking Fields - Euro League\\hostport\\7777\\gametype\\CTF\\gamever\\63446\\password\\0\\gamename\\tribesv\\gamemode\\openplaying\\gamevariant\\0\\teamone\\Blood Eagle\\teamtwo\\Imperial\\teamonescore\\0\\teamtwoscore\\0\\adminname\\Yidbon\\adminemail\\http://tveurope.leaguecentral.info/\\p\\WebAdmin<Shades>HolginB7ADESlazerWhiteYidbon|^'\\trackingstats\\False\\dedicated\\true\\minver\\63446\\player_0\\Yidbon|^'\\player_1\\White\\player_2\\Slazer\\player_3\\B7ADE\\player_4\\<Shades>Holgin\\ping_0\\0\\ping_1\\0\\ping_2\\2102\\ping_3\\1912\\ping_4\\2491\\score_0\\0\\score_1\\0\\score_2\\0\\score_3\\0\\score_4\\0\\team_0\\Blood Eagle\\team_1\\Imperial\\team_2\\Imperial\\team_3\\Blood Eagle\\team_4\\Blood Eagle\\final\\queryid\\1.1")

(def ex3 "\\mapname\\Highlands\\numplayers\\0\\maxplayers\\2\\hostname\\Aksel@sagstuen.org\\hostport\\7777\\gametype\\CTF\\gamever\\61735\\password\\1\\gamename\\tribesv\\gamemode\\openplaying\\teamone\\Imperial\\teamtwo\\Blood Eagle\\teamonescore\\0\\teamtwoscore\\0\\adminname\\a\\adminemail\\a\\p\\a\\trackingstats\\False\\dedicated\\true\\final\\\\queryid\\1.1")

(comment (ask ["94.23.229.44" 7782]))

(defn tinfos []
  (parseScores ex))

(defn t1 []
  (toJsonStr (tinfos)))

;(def infos {"player_0" "machin" "ping_0" "2" "score_0" "3" "team_0" "be" })
;(prn (macroexpand '(defrecordx Player [name ping #^int score team])))
;(prn (macroexpand '(construct Player (map #(infos (str % "_" 0)) ["player" "ping" "score" "team"]))))

(defn t2 []
  (toJsonStr (toScoreBoard (tinfos))))

(def exs "144.140.154.67:20001 \\hostname\\GameArena Tribes: Vengeance #1 [CTF]\\numplayers\\0\\maxplayers\\28\\mapname\\Isle")

(defn t3 []
  (parseServerInfo exs))
