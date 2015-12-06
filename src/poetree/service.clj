(ns poetree.service
  (:require [poetree.db :as db]
            [clojure.data.json :as json]))

;; TODO move to utils
(defn debug [m o] (println m " " o) o)

(defn users
  []
  (db/users))



(defn collapse-poems [poems]
  (let [ids (map :id poems)
        state (zipmap ids poems)
        has-childs (into #{} (map :poems_id poems)) 
        poems-view
        (->> ids
             (filter (complement has-childs))
             (map (fn [id]
                    (loop [parent id acc []]
                      (cond
                        (nil? parent) {:lines (reverse acc)}
                        true (let [parent-content (get state parent)]
                               (recur (:poems_id parent-content) (conj acc parent-content))))))))]
    poems-view
    ))

(defn feed []
  (collapse-poems (db/get-poems)))

(defn poem [id]
  (first (collapse-poems (db/view-poem id))))

(defn add-poem [content owner-line-id user-id]
  (:id (first (db/add-poem content owner-line-id user-id))))

(defn user-id-by-name [name]
  (when name
    (:id (db/get-user-by-name name))))

(defn fork [id]
  (throw (IllegalArgumentException. "Not Implemented Yet")))


(defn likers [id]
  (throw (IllegalArgumentException. "Not Implemented Yet")))
