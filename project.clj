(defproject hello-world "0.0.1"
  :dependencies
  [[org.clojure/clojure "1.2.1"]
   [org.clojure/clojure-contrib "1.2.0"]
   [ring/ring-jetty-adapter "0.3.9"]]

  :aot [demo.web]
  :repl-init demo.web )