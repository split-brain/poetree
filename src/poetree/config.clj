(ns poetree.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            ))

(defn load-resource
  [path]
  (-> (io/resource path)
      slurp
      edn/read-string))

(def default-config (load-resource "config/defaults.edn"))

(defn load-config
  "Given a filename, load a config file and set app config."
  [path]
  (println "Reading config from" path)
  (let [config (-> path
                   slurp
                   edn/read-string
                   (merge default-config))]
    (println config)
    ;(defstate app-config :start config)
    config))
