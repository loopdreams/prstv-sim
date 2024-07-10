(ns prstv-sim.db)

(def default-db
  {:marked-ballot 1
   :chart-data {:type "bar"
                :data {:labels ["Red" "Blue" "Yellow"]
                       :datasets [{:label "# of Votes"
                                   :data [12 19 3]
                                   :borderWidth 1}]}
                :options {:scales {:y {:beginAtZero true}}}}})
