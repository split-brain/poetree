(ns poetree.utils
  (:import (java.net InetAddress)))

(defn get-hostname
  []
  (.getCanonicalHostName (InetAddress/getLocalHost)))