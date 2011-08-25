(defproject tvbrowser "0.0.1"
  :warn-on-reflection true
  :dependencies
  [[org.clojure/clojure "1.2.1"]
   [org.clojure/clojure-contrib "1.2.0"]
   [ring/ring-jetty-adapter "0.3.9"]]

  :aot [tvb.web]
  :repl-init tvb.test )