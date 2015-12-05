(ns poetree.templates
  (:use [hiccup.page :only (html5 include-css include-js)]))

(defn page [title & content]
  (html5 
   [:head
    [:title title]
    [:body
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
      ]
     
     
     )]
  )
