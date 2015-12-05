(ns poetree.db
  (:require [korma.db :as korma]
            [korma.core :as sql]))

(def connection
  {
   :db "mkoz"
   :user "mkoz"
   :password ""
   :host "localhost"
   :port "5432"
   :scheme "public"
   
   })

(korma/defdb db (korma/postgres connection))

(sql/defentity poem)

;; Database Layer

;; (sql/select poem) => SELECT * FROM POEM
