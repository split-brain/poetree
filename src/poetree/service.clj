(ns poetree.service
  (:require [poetree.db :as db]
            [clojure.data.json :as json]))

(defn feed []
  ;; TODO transform to some structure
  #_(-> (db/get-poems)
      ;;(json/write-str)
        )
  [{
    :lines [{:id 1
             :poems_id nil
             :order_id 1
             :type "POROX"
             :content "спустись в каюту маяковский"
             :author {:name "Dima" :link "http://twitter.com/dima"}
             :likers [1 2 3]
             }

            {:id 2
             :poems_id 1
             :order_id 2
             :type "POROX"
             :content "кричат матросы скоро шторм"
             :author {:name "Oleg" :link "http://twitter.com/oleg"}
             :likers [1]
             }
            
            ]
    }]

  )

(defn fork [id]
  (throw (IllegalArgumentException. "Not Implemented Yet")))


(defn likers [id]
  (throw (IllegalArgumentException. "Not Implemented Yet")))
