(ns poetree.handler
  (:require [poetree.service :as service]
            [poetree.templates :as t]
            
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]

            [org.httpkit.server :as http-server]))

(defroutes app-routes
  (GET "/" [] "Welcome to Poetree :: Login Page")
  (GET "/feed" [] (t/page "Poems Feed" (t/view-feed (service/feed))))
  (GET "/fork" [id] "Create New")
  (GET "/fork/:id" [id] (service/fork id))

  (GET "/likers/:id" [id] (service/likers id))

  (route/resources "/")
  (route/not-found "TODO: ERROR PAGE"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn start-server [app]
  (let [port (Integer/parseInt
              (or (System/getenv "PORT") "8080"))]
    (http-server/run-server app {:port port :join? false})))

;;(def server (start-server app))
