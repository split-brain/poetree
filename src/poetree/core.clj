(ns poetree.core
  (:require [poetree.config :refer [load-config]]
            [poetree.handler :as handler]
            [poetree.db :as db])
  (:gen-class))

(defn -main [path]
  (let [config (load-config path)]
    (db/initdb (:database config))
    (handler/start-server)
    (.join (Thread/currentThread))))
