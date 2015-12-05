(ns poetree.db
  (:require [korma.db :as korma]
            ;; TODO remove conflict with clojure.core/update
            [korma.core :refer :all]))


;; TODO replace it with prod configuration
(def connection
  {
   :db "poetree"
   :user "mkoz"
   :password ""
   :host "localhost"
   :port "5432"
   :scheme "public"
   
   })

(korma/defdb db (korma/postgres connection))


(declare users poems likers)

(defentity users
  (pk :id)
  (table :users)
  (database db)
  (entity-fields :id :name :link)
  )

(defentity poems
  (pk :id)
  (table :poems)
  (database db)
  (entity-fields :id :line_order :type :content :users_id :poems_id :lang)

  (belongs-to users) ;; poems.users_id = users.id
  (belongs-to poems) ;; poems.poems_id = poems.id

  )

(defentity likers
  ;;  (pk :id)
  (table :likers)
  (database db)
  (entity-fields :users_id :poems_id)

  (belongs-to users) ;; likers.users_id = users.id
  (belongs-to poems) ;; likers.poems_id = poems.id

  )


;; (select poem) => SELECT * FROM POEM


;; DATABASE ACCESSOR

;; USERS


(defn get-users []
  (select users))

(defn get-user-by-id [id]
  (-> 
   (select users
           (where {:id id}))
   first))

;; POEMS

(defn get-poems []
  (select poems))

(defn get-poems-for-user [userid]
  (select poems
          (where {:users_id userid})))
