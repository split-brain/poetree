(ns poetree.templates
  (:require [ring.util.codec :as codec])
  (:use [hiccup.page :only (html5 include-css include-js)]))

(defn page [title authorized? & content]
  (html5
   [:head
    [:title title]
    [:body
     (if authorized?
       [:a {:href "/logout"} "Logout"]
       [:a {:href "/login"} "Login"])
     [:h1 title]
     [:div {:class "container"} content ]]]))

(defn view-feed [feed]
  [:div
   [:a {:href "/fork"} "Create New"]

   (for [f feed
         :let [lines (:lines f)]]
     ;;
     [:div {:class "poem"}
      (for [line lines :let [author (:author line)]]
        [:div {:class "line"}
         (:content line)
         " "
         [:a {:href (:link author)} (:name author)]
         " "
         [:a {:href (format "/fork/%s" (:id line))} "Fork"]
         " "
         [:a {:href (format "/likers/%s" (:id line))} "Likes: " (count (:likers line))]
         ]
        )
      (tweet-button "http://todo-view-link.com" "Check out this poem at Poetree!")
      ]


     )]
  )

(defn tweet-button [link text]
  [:a
   {:class "twitter-share-button"
    :href (str "https://twitter.com/intent/tweet?text=" (codec/url-encode (str text " " link)))}
   "Tweet"])
