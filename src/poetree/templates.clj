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
     [:div {:class "landing-title"} "Poetree"]
     [:div {:class "landing-desc"} "colaborative poems"]
     
     [:div {:class "landing-button"
            :onclick "location.href='/feed'"
            :style "cursor:pointer;"} "Show me"]]
    
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
         [:a {:href (:link author)
              :alt (:name author)}
          [:img {:src "images/poetree_user.png"
                 :width "24" :height "24"}]]
         " "
         [:a {:href (format "/feed/%s" (:id line))
              :alt "View"}
          [:img {:src "images/poetree_view.png"
                 :width "24" :height "20"}]]
         " "
         [:a {:href (format "/fork/%s" (:id line))
              :alt "Fork"}
          [:img {:src "images/poetree_fork.png"
                 :width "20" :height "24"}]]
         " "
         [:a {:href (format "/likers/%s" (:id line))
              :alt "Like"}
          [:img {:src "images/poetree_like.png"
                 :width "24" :height "24"}]]
         ]
        )
      (tweet-button "http://todo-view-link.com" "Check out this poem at Poetree!")
      ]


     )]
  )

(defn view-poem [poem]
  "TODO")


;; Move somewhere else
(defn max-lines-number-by-type [type]
  (cond
    (= type "HAIKU") 3
    (= type "POROX") 4
    :else 3))

(defn max-lines-number-for-poem [poem]
  (let [[line & _] (:lines poem)]
    (max-lines-number-by-type (:type line))))

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
    [:post (str "/fork" (if-let [id (:id (last (:lines poem)))]
                          (str "/" id) ""))]
    (anti-forgery-field)
    (for [new-line-number (range (- (max-lines-number-for-poem
                                     poem)
                                    (let [lines (:lines poem)]
                                      (if lines (count lines) 0))))]
      [:div
       (text-field {:size 50} (str "content" new-line-number))])
    (if authentication
      [:div (submit-button "Add poem")]
      [:div (submit-button "Add poem anonymously")]))
   (when-not authentication
     [:div (form-to [:get "/login"] (submit-button "Add with twitter"))])])
