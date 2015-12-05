(defproject poetree "0.0.1"
  :description "Colaborative Poems"
  :url "http://clojurecup.com/poetree"
  :min-lein-version "2.0.0"
  :dependencies [
                 [org.clojure/clojure "1.7.0"]

                 ;; web server, routing
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]

                 ;; database
                 [org.clojure/java.jdbc "0.4.1"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]

                 ;; sql dsl
                 [korma "0.4.2"]
                 
                 ]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler poetree.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
