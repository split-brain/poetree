(ns poetree.twitter
  (:use [twitter.api.restful])
  (:require [twitter.oauth :as twitter-oauth]))

(defn make-oauth-creds [consumer user-access-token]
  (twitter-oauth/->OauthCredentials
    consumer
    (:oauth_token user-access-token)
    (:oauth_token_secret user-access-token)))

(defn get-user
  [oauth-creds screen-name]
  (users-show :oauth-creds oauth-creds :params {:screen-name screen-name}))

(defn get-user-image
  [oauth-creds screen-name]
  (-> (get-user oauth-creds screen-name)
      (get-in [:body :profile_image_url_https])))
