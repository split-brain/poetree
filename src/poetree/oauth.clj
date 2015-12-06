(ns poetree.oauth
  (:require [oauth.client :as oauth-client]
            [twitter.oauth :as twitter-oauth]))

(def app-key "QwUxdswMIqCpUvUJzjPreOU8p")
(def app-key-secret "LLgt1CbpPyleBlOF1WlNrS8b6A5AqNNTRCciiZBUTXiVnfB2zS")

(defn make-app-consumer []
  (oauth-client/make-consumer app-key
                              app-key-secret
                              "https://api.twitter.com/oauth/request_token"
                              "https://api.twitter.com/oauth/access_token"
                              "https://api.twitter.com/oauth/authenticate"
                              :hmac-sha1))

