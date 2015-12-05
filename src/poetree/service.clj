(ns poetree.service
  (:require [poetree.db :as db]
            [clojure.data.json :as json]))

;; TODO move to utils
(defn debug [m o]
  (println m " " o)
  o)

(defn users
  []
  (db/users))

(defn feed []
  ;; TODO transform to some structure
  (let [poems (db/get-poems)
        ids (map :id poems)
        state (zipmap ids poems)
        has-childs (into #{} (map :poems_id poems))
        poems-view
        (->> ids
             (debug "IDS")
             (filter (complement has-childs))
             (debug "FILTERED")
             (map (fn [id]
                    (loop [parent id acc []]
                      (cond
                        (nil? parent) {:lines (reverse acc)}
                        true (let [parent-content (get state parent)]
                               (recur (:poems_id parent-content) (conj acc parent-content))))))))]
    (println ids)
    (println has-childs)
    (println poems-view)
    poems-view))

(defn fork [id]
  (throw (IllegalArgumentException. "Not Implemented Yet")))


(defn likers [id]
  (throw (IllegalArgumentException. "Not Implemented Yet")))
