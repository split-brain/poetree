(ns poetree.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]

            [org.httpkit.server :as http-server]))

(defroutes app-routes
  (GET "/" [] "Welcome to Poetree")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn start-server [app]
  (let [port (Integer/parseInt
              (or (System/getenv "PORT") "8080"))]
    (http-server/run-server app {:port port :join? false})))

(def server (start-server app))
