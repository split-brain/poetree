(ns poetree.core
  (:require [poetree.config :refer [load-config]]
            [poetree.handler :as handler]
            [poetree.db :as db]
            [ring.adapter.jetty :refer :all]
            )
  (:gen-class))

(defn init
  ([]
   (init
    (load-config (str (System/getProperty "user.home") "/poetree.db"))))
  ([config]
   (db/initdb (:database config))))

(defn -main [path]
  (let [config (load-config path)]
    ;; configure database
    (init config)

    ;; start server
    (run-jetty handler/app (:server config))))
