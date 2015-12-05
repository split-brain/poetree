(ns poetree.db
  (:refer-clojure :exclude [update])
  (:require [korma.db :as korma]
            [korma.core :refer :all]))

(defn initdb
  [config]

  (korma/defdb db (korma/postgres config))

  (declare users poems likers)

  (defentity users
    (pk :id)
    (table :users)
    (database db)
    (entity-fields :id :name :access_token :access_token_secret)
    )

  (defentity poems
    (pk :id)
    (table :poems)
    (database db)
    (entity-fields :id :line_order :type :content :users_id :poems_id #_:lang)

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

    ))




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

(defn get-user-by-name [name]
  (first (select users (where {:name name}))))

;; POEMS

(defn get-poems []
  (select poems))

(defn get-poems-for-user [userid]
  (select poems
          (where {:users_id userid})))

(defn add-user
  ([name] (add-user name nil))
  ([name access-token access-token-secret]
   (insert users (values {:name name
                          :access_token access-token
                          :access_token_secret access-token-secret}))))

(defn update-user
  [name access-token access-token-secret]
  (update users
          (set-fields {:access_token access-token
                       :access_token_secret access-token-secret})
          (where {:name name})))

;; GET ALL LEAFS
;; GET ALL FINISHED
;; GET ALL UNFINISHED


(defn view-poem [id]
  (let [sql "with recursive poem_tree as (
  select * from poems where id = ? 

  union all

  select p.* from poems p 
         join poem_tree t on t.poems_id = p.id
)
select distinct * from poem_tree"]
    (exec-raw db [sql [id]] :results)))
