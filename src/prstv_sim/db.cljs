(ns prstv-sim.db)

(def default-db
  {:marked-ballot 1
   :chart-data {:type "bar"
                :data {:labels ["Red" "Blue" "Yellow"]
                       :datasets [{:label "# of Votes"
                                   :data [12 19 3]
                                   :borderWidth 1}]}
                :options {:scales {:y {:beginAtZero true}}}}
   :inputs {:n-votes "1000"
            :n-seats "4"
            :volatility "15"
            :preference-depth "mid"
            :party {1 {:name "Party A"
                       :popularity "32"
                       :colour "dark blue"}
                    2 {:name "Party B"
                       :popularity "28"
                       :colour "dark green"}
                    3 {:name "Party C"
                       :popularity "36"
                       :colour "red"}
                    4 {:name "Independant"
                       :popularity "10"
                       :colour "default"}}
            :candidate {1 {:name "Captain Hook"
                           :popularity "3"
                           :party-id 4}
                        2 {:name "Peter Pan"
                           :popularity "28"
                           :party-id 1}
                        3 {:name "Cruella"
                           :popularity "12"
                           :party-id 4}
                        4 {:name "Snow White"
                           :popularity "24"
                           :party-id 2}
                        5 {:name "Cinderella"
                           :popularity "26"
                           :party-id 2}
                        6 {:name "Queen Bee"
                           :popularity "38"
                           :party-id 4}
                        7 {:name "Micky Mouse"
                           :popularity "18"
                           :party-id 3}
                        8 {:name "Minnie Mouse"
                           :popularity "19"
                           :party-id 3}}}})
