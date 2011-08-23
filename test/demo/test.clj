(ns demo.test
  (:use :reload-all demo.web)
  (:use :reload-all demo.utils)
  (:use :reload-all demo.domain))
    
(def ex0 "\\mapname\\Foregone (x2)\\numplayers\\10\\maxplayers\\16\\hostname\\The Waking Fields - Euro League\\hostport\\7777\\gametype\\CTF\\gamever\\63446\\password\\0\\gamename\\tribesv\\gamemode\\openplaying\\gamevariant\\0\\teamone\\Blood Eagle\\teamtwo\\Imperial\\teamonescore\\10\\teamtwoscore\\2\\adminname\\Yidbon\\adminemail\\http://tveurope.leaguecentral.info/\\p\\WebAdminKmA_INTERcEPTOR'VipaT.Juppelorddragon93sYnesthesiaTetsuoaT.WhiteMetaL\\hurlanTZhelatinel conejito|^'\\trackingstats\\False\\dedicated\\true\\minver\\63446\\player_0\\el conejito|^'\\player_1\\Zhelatin\\player_2\\MetaL\\hurlanT\\player_3\\aT.White\\player_4\\Tetsuo\\player_5\\sYnesthesia\\player_6\\lorddragon93\\player_7\\aT.Juppe\\player_8\\'Vip\\player_9\\KmA_INTERcEPTOR\\ping_0\\13\\ping_1\\35\\ping_2\\81\\ping_3\\41\\ping_4\\28\\ping_5\\42\\ping_6\\76\\ping_7\\50\\ping_8\\72\\ping_9\\62\\score_0\\2\\score_1\\20\\score_2\\26\\score_3\\10\\score_4\\9\\score_5\\24\\score_6\\0\\score_7\\26\\score_8\\9\\score_9\\47\\team_0\\Imperial\\team_1\\Imperial\\team_2\\Blood Eagle\\team_3\\Blood Eagle\\team_4\\Imperial\\team_5\\Blood Eagle\\team_6\\Imperial\\team_7\\Blood E")

(def ex "\\mapname\\Junk\\numplayers\\2\\maxplayers\\18\\hostname\\Frenchie TV Server\\hostport\\7779\\gametype\\CTF\\gamever\\63446\\password\\0\\gamename\\tribesv\\gamemode\\openplaying\\gamevariant\\0\\teamone\\Phoenix\\teamtwo\\Imperial\\teamonescore\\0\\teamtwoscore\\0\\adminname\\Titeuf\\adminemail\\titeuf.lol@orange.fr\\p\\WebAdminPadin[TS] Varilian\\trackingstats\\False\\dedicated\\true\\minver\\63446\\player_0\\[TS] Varilian\\player_1\\Padin\\ping_0\\45\\ping_1\\56\\score_0\\4\\score_1\\0\\team_0\\Imperial\\team_1\\Phoenix\\final\\\\queryid\\1.1")

(comment (ask ["94.23.229.44" 7782]))

(defn tinfos []
  (infoMap ex))

(defn t1 []
  (toJsonStr (tinfos)))

(defn t2 []
  (toJsonStr (serverInfos (tinfos))))
