(ns poetree.config
  (:require [clojure.edn :as edn]
            [mount.core :refer [defstate]]
            ))

(defn load-config
  "Given a filename, load a config file and set app config."
  [path]
  (println "Loading config from " path)
  (let [config (->> path
                    slurp
                    edn/read-string
                    )]
    (defstate app-config :start config)))
