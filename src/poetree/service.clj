(ns poetree.service
  (:require [poetree.db :as db]
            [clojure.data.json :as json]))

(defn feed []
  ;; TODO transform to some structure
  (-> (db/get-poems)
      (json/write-str)))
