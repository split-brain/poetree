(ns poetree.utils
  (:import (java.net InetAddress)))

(defn get-hostname
  []
  (.getHostName (InetAddress/getLocalHost)))