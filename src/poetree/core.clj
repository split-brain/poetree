(ns poetree.core
  (:require [mount.core :as mount :refer [defstate]]
            [poetree.config :refer [app-config load-config]]
            [poetree.handler :refer [server]])
  (:gen-class))

(defn -main [path]
  (load-config path)
  (mount/start))
