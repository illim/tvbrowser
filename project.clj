(defproject tvbrowser "0.0.1"
  :warn-on-reflection true
  :native-path "native"
  :dependencies
  [[org.clojure/clojure "1.3.0"]
   [org.clojure/core.incubator "0.1.0"]
   [org.clojure/data.json "0.1.1"]
   [ring/ring-jetty-adapter "0.3.9"]
   [net.java.dev.jna/jna "3.3.0"]]

  :jvm-opts ["-Djna.library.path=native/linux/x86_64"]
  :aot [tvb.web]
  :repl-init tvb.test )