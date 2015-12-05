(ns poetree.handler
  (:require [poetree.service :as service]
            [poetree.templates :as t]
            [poetree.config :refer [app-config]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]

            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.util.response :refer [redirect]]

            [mount.core :refer [defstate]]

            [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [hiccup.def :refer :all]
            [hiccup.page :refer :all]

            [cemerick.friend :as friend]
            [cemerick.friend.workflows :as friend-workflows]

            [oauth.client :as oauth-client]
            [poetree.oauth :as poetree-oauth]

            [org.httpkit.server :as http-server]))

(def consumer (poetree-oauth/make-app-consumer))

(defn twitter-auth-workflow []
  (fn [req]
    (let [credential-fn (get-in req [::friend/auth-config :credential-fn])]

      (cond
        (= "/logged" (:uri req))
        (when-let [request-token (get-in req [:session :oauth-request-token])]
          (when-let [access-token (oauth-client/access-token
                                   consumer
                                   request-token
                                   (get-in req [:params :oauth_verifier]))]
            (friend-workflows/make-auth (credential-fn access-token))))))))

(defroutes login-logout-routes
  (GET "/login" []
    (let [request-token (oauth-client/request-token
                         consumer
                         "http://localhost:8080/logged")
          approval-uri (oauth-client/user-approval-uri consumer (:oauth_token
                                                                 request-token))
          resp (redirect approval-uri)]
      (update-in resp [:session] assoc :oauth-request-token request-token)))
  (friend/logout (ANY "/logout" []
                   (redirect "/"))))

(defroutes app-routes
  (GET "/" [] "Welcome to Poetree :: Login Page")
  (GET "/feed" request (t/page
                        "Poems Feed"
                        (friend/authorized?
                         #{::user}
                         (friend/identity request))
                        (t/view-feed (service/feed))))
  (GET "/fork" [id] "Create New")
  (GET "/fork/:id" [id] (service/fork id))

  (GET "/like/:id" [id] "NOT IMPLEMENTED")
  (GET "/likers/:id" [id] (service/likers id))
  login-logout-routes

  (route/resources "/")
  (route/not-found "TODO: ERROR PAGE"))

(def app
  (-> app-routes
      (friend/authenticate {:default-landing-uri "/"
                            :login-uri "/login"
                            :workflows [(twitter-auth-workflow)]
                            :credential-fn #(hash-map :identity % :roles #{::user})})
      (wrap-defaults site-defaults)
      wrap-keyword-params
      wrap-params
      wrap-session))

(defn start-server [app]
  (let [port (get-in app-config [:server :port])]
    (http-server/run-server app {:port port :join? false})
    (println "Server listening on port" port)))

(defstate server
  :start (start-server app)
  :stop (server))
