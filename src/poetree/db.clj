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
    (entity-fields :id :name :profile_image_url :access_token :access_token_secret)
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

    )

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

(defn get-user-by-name [name]
  (first (select users (where {:name name}))))

;; POEMS

(defn get-poems []
  ; TODO(dima) - remove isers_id2 from results
  (exec-raw db
            ["select * from poems p
              left outer join (select id as users_id, name as username, profile_image_url from users) u
              on p.users_id = u.users_id
              order by p.ts desc
"] :results))

(defn get-poems-for-user [userid]
  (select poems
          (where {:users_id userid})))

(defn add-user
  ([name] (add-user name nil nil nil))
  ([name profile-image-url access-token access-token-secret]
   (insert users (values {:name name
                          :profile_image_url profile-image-url
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

(defn view-finished-poems []
  (let [sql "with recursive poem_tree as (
  select * from poems where line_order = 3

  union all

  select p.* from poems p 
         join poem_tree t on t.poems_id = p.id
)
select distinct * from poem_tree p
left outer join (select id as users_id, name as username, profile_image_url from users) u
on p.users_id = u.users_id"]
    (exec-raw db sql :results)))

(defn view-poem [id]
  (let [sql "with recursive poem_tree as (
  select * from poems where id = ? 

  union all

  select p.* from poems p 
         join poem_tree t on t.poems_id = p.id
)
select distinct * from poem_tree p
left outer join (select id as users_id, name as username, profile_image_url from users) u
on p.users_id = u.users_id"]
    (exec-raw db [sql [id]] :results)))

(defn add-poem [content owner-id user-id]
  (if owner-id
    (let [sql "insert into poems (line_order, type, content, lang, users_id, poems_id)
               select (poems.line_order + 1), poems.type, ?, poems.lang, ?, ?
               from poems
               where poems.id = ?
               RETURNING id"]
      (exec-raw db [sql [content user-id owner-id owner-id]] :results))
    (let [sql "insert into poems (line_order, type, content, lang, users_id, poems_id)
               values (1, 'HAIKU', ?, 'EN', ?, null)
               returning id"]
      (exec-raw db [sql [content user-id]] :results))))
