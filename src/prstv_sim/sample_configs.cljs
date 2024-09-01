(ns prstv-sim.sample-configs)


(def input-default
  {:n-votes          "1000"
   :n-seats          "4"
   :volatility       "10"
   :preference-depth :mid
   :party            {1 {:name       "Party A"
                         :popularity "23"
                         :colour     "Yellow"}
                      2 {:name       "Party B"
                         :popularity "21"
                         :colour     "Red"}
                      3 {:name       "Party C"
                         :popularity "18"
                         :colour     "Blue"}
                      4 {:name       "Independant"
                         :popularity "15"
                         :colour     "Purple"}}
   :candidate        {1 {:name       "Captain Hook"
                         :popularity "9"
                         :party-id   4}
                      2 {:name       "Peter Pan"
                         :popularity "23"
                         :party-id   1}
                      3 {:name       "Cruella"
                         :popularity "12"
                         :party-id   4}
                      4 {:name       "Snow White"
                         :popularity "24"
                         :party-id   2}
                      5 {:name       "Cinderella"
                         :popularity "26"
                         :party-id   2}
                      6 {:name       "Queen Bee"
                         :popularity "30"
                         :party-id   4}
                      7 {:name       "Micky Mouse"
                         :popularity "18"
                         :party-id   3}
                      8 {:name       "Minnie Mouse"
                         :popularity "19"
                         :party-id   3}
                      9 {:name       "Lost Boy"
                         :popularity "15"
                         :party-id   1}}})

;; In this case, the high popularity of "Queen Bee" can sometimes help one of the party's lower candidates get over the line,
;; even though they have much lower vote shares.
(def input-senario-a
  {:n-votes          "1000"
   :n-seats          "3"
   :volatility       "10"
   :preference-depth :mid
   :party            {1 {:name       "Party A"
                         :popularity "23"
                         :colour     "Yellow"}
                      2 {:name       "Party B"
                         :popularity "21"
                         :colour     "Red"}
                      3 {:name       "Independant"
                         :popularity "38"
                         :colour     "Purple"}}
   :candidate        {1 {:name       "Captain Hook"
                         :popularity "9"
                         :party-id   3}
                      2 {:name       "Peter Pan"
                         :popularity "23"
                         :party-id   1}
                      3 {:name       "Cruella"
                         :popularity "12"
                         :party-id   3}
                      4 {:name       "Snow White"
                         :popularity "21"
                         :party-id   2}
                      5 {:name       "Cinderella"
                         :popularity "18"
                         :party-id   2}
                      6 {:name       "Queen Bee"
                         :popularity "52"
                         :party-id   3}
                      9 {:name       "Lost Boy"
                         :popularity "15"
                         :party-id   1}}})

(def highly-random
  {:n-votes "1000"
   :n-seats "3"
   :volatility "90"
   :preference-depth :mid
   :party {1 {:name "NoParty"
              :colour "Purple"}}
   :candidate {1 {:name "Candidate A" :party-id 1}
               2 {:name "Candidate B" :party-id 1}
               3 {:name "Candidate C" :party-id 1}
               4 {:name "Candidate D" :party-id 1}
               5 {:name "Candidate E" :party-id 1}
               6 {:name "Candidate F" :party-id 1}
               7 {:name "Candidate G" :party-id 1}
               8 {:name "Candidate H" :party-id 1}
               9 {:name "Candidate I" :party-id 1}
               10 {:name "Candidate J" :party-id 1}
               11 {:name "Candidate K" :party-id 1}
               12 {:name "Candidate L" :party-id 1}}})

;; Running more candiates than other parties
(def philosophers
  {:n-votes          "500"
   :n-seats          "4"
   :volatility       "5"
   :preference-depth :deep
   :party            {1 {:name       "Pre-socratics"
                         :colour     "Purple"
                         :popularity "30"}
                      2 {:name       "Idealists"
                         :colour     "Yellow"
                         :popularity "15"}
                      3 {:name       "Empiricists"
                         :colour     "Green"
                         :popularity "10"}
                      4 {:name       "Hemeneuts"
                         :colour     "Red"
                         :popularity "19"}
                      5 {:name       "Post-structuralists"
                         :colour     "Blue"
                         :popularity "11"}}
   :candidate        {1  {:name "Heraclitus" :party-id 1 :popularity "20"}
                      2  {:name "Parmenides" :party-id 1 :popularity "55"}
                      3  {:name "Pythagoras" :party-id 1 :popularity "21"}
                      4  {:name "Hegel" :party-id 2 :popularity "20"}
                      5  {:name "Kant" :party-id 2 :popularity "61"}
                      6  {:name "Locke" :party-id 3 :popularity "14"}
                      7  {:name "Berkeley" :party-id 3 :popularity "7"}
                      8  {:name "Heidegger" :party-id 4 :popularity "20"}
                      9  {:name "Gadamer" :party-id 4 :popularity "13"}
                      10 {:name "Ricoeur" :party-id 4 :popularity "26"}
                      11 {:name "Derrida" :party-id 5 :popularity "7"}
                      12 {:name "Deleuze" :party-id 5 :popularity "21"}
                      13 {:name "Lyotard" :party-id 5 :popularity "19"}
                      14 {:name "Foucault" :party-id 5 :popularity "29"}
                      15 {:name "Nancy" :party-id 5 :popularity "15"}}})

(def two-seater
  {:n-votes          "1000"
   :n-seats          "2"
   :preference-depth :deep
   :volatility "10"
   :party            {1 {:name       "Fruits"
                         :colour     "Red"
                         :popularity "65"}
                      2 {:name       "Vegetables"
                         :colour     "Green"
                         :popularity "15"}}
   :candidate        {1 {:name "Apple" :party-id 1 :popularity "21"}
                      2 {:name "Banana" :party-id 1 :popularity "16"}
                      3 {:name "Pear" :party-id 1 :popularity "12"}
                      4 {:name "Broccoli" :party-id 2 :popularity "11"}
                      5 {:name "String Bean" :party-id 2 :popularity "35"}
                      6 {:name "Cabbage" :party-id 2 :popularity "20"}}})


(def sample-config-options-list
  {:input-default    {:values input-default
                      :name   "Default"}
   :input-scenario-a {:values input-senario-a
                      :name   "Highly Popular Candidate"}
   :highly-random    {:values highly-random
                      :name   "Highly Random"}
   :philosophers     {:values philosophers
                      :name   "Philosophers"}
   :two-seater       {:values two-seater
                      :name   "Two-seater"}})
