(ns poetree.templates
  (:require [ring.util.codec :as codec]
            [hiccup.form :refer :all])
  (:use [hiccup.page :only (html5 include-css include-js)]
        [ring.util.anti-forgery :only [anti-forgery-field]]))


(defn landing []
  (html5
   [:head
    [:title "Poetree"]
    [:link {:rel "shortcut icon" :href "/images/favicon.ico"}]
    (include-css "/css/poetree.css")
    (include-css "https://fonts.googleapis.com/css?family=Alegreya")]
   [:body {:class "landing"}
    [:div {:class "landing-content"}
     [:div {:class "landing-title"}
      [:img {:src "/images/logo.png"
             :width "500"}]]
     #_[:div {:class "landing-desc"} "colaborative poems"]
     
     [:div {:class "landing-button"
            :onclick "location.href='/feed'"
            :style "cursor:pointer;"} "Show me!"]]
    
    ])
  
  )

(defn header [auth]
  [:div {:class "header"}
   [:div {:class "header_button"
          :onclick "location.href='/feed'"
          :style "cursor:pointer;"}
    [:img {:src "/images/poetree_logo_small.png"
           :height "38"}]
    ]
   
   ;; User auth
   [:div {:class "header_button"}
    (if auth
      (let [user (get-in auth [:identity])]
        (seq
          [[:img {:src    (:profile_image_url user)
                  :height "30"}]
           " "
           (:name user)
           " "
           [:a {:href "/logout"}
            [:span {:style "padding-left:5px"}
             [:img {:src    "/images/poetree_logout.png"
                    :height "20"}]]]]))
      [:a {:href "/login"} "Sign In"])]
   
   ;; Create New
   [:div {:class "header_button"
          :onclick "location.href='/fork'"
          :style "cursor:pointer;"} 
    [:span "New Poem"]
    ]

   [:div {:class "header_button"
          :onclick "location.href='/random'"
          :style "cursor:pointer;"} 
    "Random"
    ]

   ])

(defn page
  ([title content]
   (page title content nil))
  ([title content authentication]
   (html5
    [:head
     [:title title]
     [:link {:rel "shortcut icon" :href "/images/favicon.ico"}]
     (include-css "/css/poetree.css")
     (include-css "https://fonts.googleapis.com/css?family=Alegreya")
     ]
    [:body {:class "mainbody"}
     (header authentication)

     [:div {:class "container"} content ]])))

(defn tweet-button [link text]
  [:div {:class "tweet-button"}
   [:a
    {:class "twitter-share-button"
     :href (str "https://twitter.com/intent/tweet?text=" (codec/url-encode (str text " " link)))
     :target "_blank"}
    "Tweet"]])




(defn line-icons [line]
  (let [author (:username line)]
    [:div {:class "line_icons"}
     " "
     [:a {:href (format "https://twitter.com/%s" author)
          :alt author}
      [:img {:src (:profile_image_url line)
             :width "24" :height "24"}]]
     " "
     [:a {:href (format "/feed/%s" (:id line))}
      [:img {:src "/images/poetree_view.png"
             :alt "View"
             :width "24" :height "18"}]]
     " "
     [:a {:href (format "/fork/%s" (:id line))}
      [:img {:src "/images/poetree_fork.png"
             :alt "Fork"
             :width "18" :height "24"}]]
     " "
     [:a {:href (format "/likers/%s" (:id line))}
      [:img {:src "/images/poetree_like.png"
             :alt "Like"
             :width "24" :height "24"}]]
     ]))

(defn view-feed [feed request]
  [:div
   (for [f feed
         :let [lines (:lines f)]]
     ;;
     [:div {:class "poem"}
      (for [line lines :let [author (:username line)]]
        [:div {:class "line"}
         [:span {:class "line_content"} (:content line)]
         (line-icons line)
         
         ]
        )
      (when (= (count lines) 3)
        (tweet-button
         (format "http://%s/feed/%d" (get-in request [:header "host"]) (:id (last lines)))
         "Check out this poem at Poetree!"))
      ]


     )]
  )

(defn view-poem [poem]
  "TODO")


;; Move somewhere else
(defn max-lines-number-by-type [type]
  (cond
    (= type "HAIKU") 3
    (= type "POROX") 3 ;; should be 4, but left as is for now
    :else 3))

(defn max-lines-number-for-poem [poem]
  (let [[line & _] (:lines poem)]
    (max-lines-number-by-type (:type line))))

(defn fork-view [poem authentication]
  [:div {:class "poem"}
   (for [line (:lines poem)]
     [:div {:class "line"}
      [:span {:class "line_content"} (:content line)]
      (line-icons line)
      ])
   (form-to
    [:post (str "/fork" (if-let [id (:id (last (:lines poem)))]
                          (str "/" id) ""))]
    (anti-forgery-field)
    (for [new-line-number (range (- (max-lines-number-for-poem
                                     poem)
                                    (let [lines (:lines poem)]
                                      (if lines (count lines) 0))))]
      [:div (text-area {:size 100
                        :rows 1
                        :class "line-edit"}
                      (str "content" new-line-number))])
    (let [add-label (if authentication "Add poem" "Add poem anonymously")]
      [:div [:input {:class "submit-button" :type "submit" :value add-label}]])
    )])


(defn error-page []
  [:div {:class "poem"}
   [:div {:class "line"}
    [:span {:class "line_content"}
     "Unfortunately something goes bad"]
    ]
   [:div {:class "line"}
    [:span {:class "line_content"}
     "All that we can say:"]
    ]
   [:div {:class "line"}
    [:span [:a {:href "/"} "Keep calm and start from the beginning"]]
    ]])
