(ns poetree.handler
  (:require [poetree.service :as service]
            [poetree.templates :as t]
            [poetree.oauth :as poetree-oauth]
            [poetree.db :as db]

            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]

            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.util.response :refer [redirect]]

            ;;[mount.core :refer [defstate]]

            [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [hiccup.def :refer :all]
            [hiccup.page :refer :all]

            [cemerick.friend :as friend]
            [cemerick.friend.workflows :as friend-workflows]

            [oauth.client :as oauth-client]
            ))

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
            (if (db/get-user-by-name (:screen_name access-token))
              (db/update-user (:screen_name access-token)
                              (:oauth_token access-token)
                              (:oauth_token_secret access-token))
              (db/add-user (:screen_name access-token)
                           (:oauth_token access-token)
                           (:oauth_token_secret access-token)))
            (friend/merge-authentication
             (redirect (or
                        (get-in req [:session :referer])
                        "/"))
             (friend-workflows/make-auth
              (credential-fn access-token)))))))))

(defroutes login-logout-routes
  (GET "/login" request
    (let [request-token (oauth-client/request-token
                         consumer
                         "http://localhost:8080/logged")
          approval-uri (oauth-client/user-approval-uri consumer (:oauth_token
                                                                 request-token))
          resp (redirect approval-uri)]
      (update-in resp [:session] assoc
                 :oauth-request-token request-token
                 :referer (get (:headers request) "referer"))))
  (friend/logout (ANY "/logout" []
                   (redirect "/"))))

(defroutes app-routes
  (GET "/" [] (t/landing))
  (GET "/feed" request
    (t/page
     "Poems Feed"
     (t/view-feed (service/feed))
     (friend/current-authentication request)))
  (GET "/feed/:id" [id :as request]
    (t/page
     "Poems Feed"
     (t/view-feed (service/poem (Long/parseLong id)))
     (friend/current-authentication request)))
  (POST "/post" [] "Post works")
  (GET "/fork" [] (t/page
                   "Create Poem"
                   (t/fork-view
                    {}
                    (friend/current-authentication))))
  (GET "/fork/:id" [id :as request]
    (t/page
     "Edit Poem"
     (t/fork-view
      (first (service/poem (Long/parseLong id)))
      (friend/current-authentication request))
     (friend/current-authentication request)))
  (POST "/fork" [content0 content1 content2 :as request]
    (let [lines (filter identity [content0 content1 content2])
          new-line-id (service/add-poem-with-lines lines
                                                   nil
                                                   (service/user-id-by-name
                                                    (get-in
                                                     (friend/current-authentication
                                                      request)
                                                     [:identity :screen_name])))]
      (redirect (str "/feed/" new-line-id))))
  (POST "/fork/:id" [id content0 content1 content2 :as request]
    (let [lines (filter identity [content0 content1 content2])
          new-line-id (service/add-poem-with-lines lines
                                                   (Long/parseLong id)
                                                   (service/user-id-by-name
                                                    (get-in
                                                     (friend/current-authentication
                                                      request)
                                                     [:identity :screen_name])))]
      (redirect (str "/feed/" new-line-id))))
  (GET "/users" [] (service/users))
  (GET "/random" []
    ;; rewrite link
    "TODO: Implement")

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
