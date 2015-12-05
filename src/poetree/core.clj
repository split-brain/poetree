(ns poetree.core
  (:require [poetree.config :refer [load-config]]
            [poetree.handler :as handler]
            [poetree.db :as db]
            [ring.adapter.jetty :refer :all]
            )
  (:gen-class))

(defn -main [path]
  (let [config (load-config path)]
    ;; configure database
    (db/initdb (:database config))

    ;; start server
    (run-jetty handler/app (:server config))))
