(defproject poetree "0.0.1"
  :description "Colaborative Poems"
  :url "http://clojurecup.com/poetree"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]

                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [http-kit "2.1.18"]
                 [hiccup "1.0.5"]

                 [com.taoensso/timbre "4.1.4"]

                 [org.clojure/core.match "0.3.0-alpha4"]

                 [clj-oauth "1.5.2"]
                 [twitter-api "0.7.8"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler poetree.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
