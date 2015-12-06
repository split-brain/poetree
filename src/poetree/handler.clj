(ns poetree.handler
  (:require [poetree.service :as service]
            [poetree.templates :as t]
            [poetree.oauth :as poetree-oauth]
            [poetree.twitter :as tw]
            [poetree.db :as db]
            [poetree.utils :refer [get-hostname]]

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

            [clojure.string :as str]
            [clojure.core.match :refer [match]]
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

              (let [oauth-creds (tw/make-oauth-creds consumer access-token)
                    profile-img-url (tw/get-user-image oauth-creds (:screen_name access-token))]
                (db/add-user (:screen_name access-token)
                             profile-img-url
                             (:oauth_token access-token)
                             (:oauth_token_secret access-token))))
            (friend/merge-authentication
             (redirect (or
                        (get-in req [:session :referer])
                        "/"))
             (friend-workflows/make-auth
              ; FIXME: we shouldn't query db again to get user object
              (credential-fn (db/get-user-by-name (:screen_name access-token)))))))))))

(defn callback-url
  []
  (let [host (get-hostname)
        port 8080]
    ;FIXME: DON'T HARDCODE THE PORT
    (format "http://%s:%d/logged" host port)))

(defroutes login-logout-routes
  (GET "/login" request
    (let [request-token (oauth-client/request-token
                         consumer
                         (callback-url))
          approval-uri (oauth-client/user-approval-uri consumer (:oauth_token
                                                                 request-token))
          resp (redirect approval-uri)]
      (update-in resp [:session] assoc
                 :oauth-request-token request-token
                 :referer (get (:headers request) "referer"))))
  (friend/logout (ANY "/logout" []
                   (redirect "/"))))

(defn valid-lines? [lines]
  (match (vec (map #(if (str/blank? %) :empty :not-empty) lines))
         [:empty]                          false
         [:empty      _]                   false
         [:not-empty :empty :not-empty]    false
         [:not-empty :empty :empty]        true
         :else                             true))

(defn fork-poem [owner-id lines request]
  (if (valid-lines? lines)
    (let [new-line-id (service/add-poem-with-lines
                       (remove str/blank? lines)
                       owner-id
                       (service/user-id-by-name
                        (get-in
                         (friend/current-authentication
                          request)
                         [:identity :name])))]
      (redirect (str "/feed/" new-line-id)))
    (redirect "/error")))

(defroutes app-routes
  (GET "/" [] (t/landing))
  (GET "/feed" request
    (t/page
     "Poems Feed"
     (t/view-feed (service/feed))
     (friend/current-authentication request)))
  (GET "/feed/:id" [id :as request]
    (t/page
     "Poem Feed"
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
    (fork-poem nil
               (filter identity
                       [content0 content1 content2])
               request))
  (POST "/fork/:id" [id content0 content1 content2 :as request]
    (fork-poem (Long/parseLong id)
               (filter identity
                       [content0 content1 content2])
               request))
  (GET "/users" [] (service/users))
  (GET "/random" request
    (t/page
     "Poem Feed"
     (t/view-feed (service/random-finished-poem))
     (friend/current-authentication request)))
  (ANY "/error" []
    "Error happened")

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
