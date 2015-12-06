(ns poetree.templates
  (:require [ring.util.codec :as codec]
            [hiccup.form :refer :all])
  (:use [hiccup.page :only (html5 include-css include-js)]
        [ring.util.anti-forgery :only [anti-forgery-field]]))

(defn landing []
  (html5
   [:head
    [:title "Poetree"]
    (include-css "css/poetree.css")
    (include-css "https://fonts.googleapis.com/css?family=Alegreya")]
   [:body {:class "landing"}
    [:div {:class "landing-content"}
     [:div {:class "landing-title"}
      [:img {:src "images/logo.png"}]]
     [:div {:class "landing-desc"} "colaborative poems"]
     
     [:div {:class "landing-button"
            :onclick "location.href='/feed'"
            :style "cursor:pointer;"} "Show me!"]]
    
    ])
  
  )

(defn page
  ([title content]
   (page title content nil))
  ([title content authentication]
   (html5
    [:head
     [:title title]
     (include-css "css/poetree.css")
     (include-css "https://fonts.googleapis.com/css?family=Alegreya")
     ]
    [:body {:class "mainbody"}
     [:div {:class "header"}

      ;; User auth
      [:div {:class "header_button"}
       (if authentication
         (seq
          [(get-in authentication [:identity :screen_name])
           [:a {:href "/logout"} "Logout"]]) ;; shuld be icon)
         [:a {:href "/login"} "Login"])]

      ;; Create New
      [:div {:class "header_button"
             :onclick "location.href='/fork'"
             :style "cursor:pointer;"} 
       "+"
       ]

      [:div {:class "header_button"
             :onclick "location.href='/random'"
             :style "cursor:pointer;"} 
       "Random"
       ]

      ]

     [:div {:class "container"} content ]])))

(defn tweet-button [link text]
  [:div {:class "tweet-button"}
   [:a
    {:class "twitter-share-button"
     :href (str "https://twitter.com/intent/tweet?text=" (codec/url-encode (str text " " link)))
     :target "_blank"}
    "Tweet"]])


(defn view-feed [feed]
  [:div
   (for [f feed
         :let [lines (:lines f)]]
     ;;
     [:div {:class "poem"}
      (for [line lines :let [author (:author line)]]
        [:div {:class "line"}
         (:content line)
         " "
         [:a {:href (:link author)}
          [:img {:src "images/poetree_user.png"
                 :alt (:name author)
                 :width "24" :height "24"}]]
         " "
         [:a {:href (format "/feed/%s" (:id line))}
          [:img {:src "images/poetree_view.png"
                 :alt "View"
                 :width "24" :height "18"}]]
         " "
         [:a {:href (format "/fork/%s" (:id line))}
          [:img {:src "images/poetree_fork.png"
                 :alt "Fork"
                 :width "18" :height "24"}]]
         " "
         [:a {:href (format "/likers/%s" (:id line))}
          [:img {:src "images/poetree_like.png"
                 :alt "Like"
                 :width "24" :height "24"}]]
         ]
        )
      (tweet-button "http://todo-view-link.com" "Check out this poem at Poetree!")
      ]


     )]
  )

(defn view-poem [poem]
  "TODO")

(defn fork-view [poem authentication]
  [:div
   (for [line (:lines poem)]
     [:div
      (text-field
       {:readonly true
        :size 50}
       "line"
       (:content line))])
   (form-to
    [:post (str "/fork/" (:id (last (:lines poem))))]
    (anti-forgery-field)
    [:div
     (text-field {:size 50} "content")]
    (if authentication
      [:div (submit-button "Add poem")]
      [:div (submit-button "Add poem anonymously")]))
   (when-not authentication
     [:div (form-to [:get "/login"] (submit-button "Add with twitter"))])])
