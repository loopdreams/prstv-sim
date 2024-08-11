(ns prstv-sim.results-display
  (:require [prstv-sim.subs :as subs]
            [prstv-sim.events :as events]
            [prstv-sim.inputs :as inputs]
            [prstv-sim.styles :as styles]
            [re-frame.core :as re-frame]
            [clojure.set :as set]))


(defn first-prefs-row-display [data]
  (let [{:keys [name colour votes percent]} data
        colour-display (inputs/get-colour-style colour)]
    [:tr
     [:th colour-display (inputs/keyword->name name)]
     [:td votes]
     [:td percent]]))

(defn first-prefs-table [data]
  (let [total-votes (reduce + (vals data))
        {:keys [party-names candidate-party party-colours]}
        @(re-frame/subscribe [::subs/vote-config])
        table-data  (reduce
                     (fn [result name]
                       (let [votes (name data)
                             colour-id (or ((set/map-invert party-names) (inputs/keyword->name name))
                                           (-> name candidate-party))
                             colour (party-colours colour-id)]
                         (conj result
                               (-> {}
                                   (assoc :name name)
                                   (assoc :colour colour)
                                   (assoc :votes votes)
                                   (assoc :percent (str
                                                    (int (* 100 (/ votes total-votes)))
                                                    "%"))))))
                     []
                     (keys data))]
    [:table.table
     [:thead
      [:tr
       [:th "Name"]
       [:th "First Preference Votes"]
       [:th "Percentage"]]]
     (into [:tbody]
           (map first-prefs-row-display (reverse (sort-by :votes table-data))))]))


;; TODO Add colours
(defn party-first-prefs-table [candidate-first-prefs]
  (let [{:keys [party-names candidate-party]} @(re-frame/subscribe [::subs/vote-config])
        table-data (reduce (fn [result candidate]
                             (let [c-votes (candidate candidate-first-prefs)
                                   c-party (-> candidate candidate-party party-names)]
                               (update result (inputs/name->keyword c-party)
                                       (fnil + 0) c-votes)))
                           {}
                           (keys candidate-first-prefs))]
    (first-prefs-table table-data)))

(defn elected-candidate-display [candidate {:keys [party-names candidate-party party-colours]}]
  (let [party-id (-> candidate candidate-party)
        party-name (-> party-id party-names)]
    [:div.box (inputs/get-colour-style (party-colours party-id))
     (str (inputs/keyword->name candidate) " (" party-name ")")]))

(defn elected-display [elected]
  (let [vote-config @(re-frame/subscribe [::subs/vote-config])]
    [:div.notification
     [:h2.subtitle "Elected:"]
     [:div
      (map #(elected-candidate-display % vote-config) elected)]]))


(defn vote-counts-header-row [count-ns]
  [:thead {:class styles/table-head}
   [:tr
    [:th {:class "px-2 py-4"} "Seat"]
    [:th {:class "px-2"} "Party"]
    [:th {:class "px-2"} "Candidate"]
    [:th {:class "px-2"}"Share"]
    (map (fn [n] [:th {:class "px-2"} (str "Count " (inc n))]) count-ns)]])

;; TODO proper formatting for marked ballot
(defn colour-candidate-exit [candidate exit-at nth-col elected data marked-ballot]
  [:span
   (when (= exit-at (inc nth-col))
     (if (elected candidate)
       {:class "has-text-sucess has-background-success-light"}
       {:class "has-text-danger has-background-danger-light"}))
   (str data (when marked-ballot "•••"))])

(defn format-count-cell [candidate exit-at nth-col elected data marked-ballot]
  [:td
   (when (= exit-at (inc nth-col))
     (if (elected candidate)
       {:class "bg-emerald-200"}
       {:class "bg-red-400"}))
   (str data (when marked-ballot "..."))])

(def elected-colours "font-bold bg-teal-100 text-teal-500")
(def eliminated-colours "font-bold bg-red-100 text-red-500")
(def my-ballot-icon "fas fa-scroll")

(defn format-count-table-cell [{:keys [count count-change elected eliminated marked-ballot]}]
  [:td {:class (str "px-2 "
                    (cond
                      elected elected-colours
                      eliminated eliminated-colours
                      :else ""))}
   count
   (when count-change
     [:span {:class "text-stone-400"} (str " (+ " count-change ")")])
   (when marked-ballot [:span {:class my-ballot-icon} " "])])


(defn count-n-data-row [candidate nth-col counts-data table-data elected]
  (let [exit-at        (-> candidate table-data :exit)
        count          (-> (counts-data nth-col) :counts candidate)
        count-change   (-> (counts-data nth-col) :count-changes candidate)
        cand-vote-ids  (-> (counts-data nth-col) :piles candidate)
        target-ballot  @(re-frame/subscribe [::subs/marked-ballot])
        marked-ballot? (some #{target-ballot} cand-vote-ids)]
    (if (not count)
      [:td ""]
      [format-count-table-cell
       {:count count
        :count-change count-change
        :elected (when (= exit-at (inc nth-col)) (elected candidate))
        :eliminated (and (= exit-at (inc nth-col)) (not (elected candidate)))
        :marked-ballot marked-ballot?}])))

(defn vote-counts-data-row [candidate counts-data table-data vote-config candidate-shares seats elected]
  (let [{:keys [position]}                                  (candidate table-data)
        {:keys [candidate-party party-colours party-names]} vote-config
        party-id                                            (candidate candidate-party)
        party-name                                          (party-names party-id)
        party-colour                                        (party-colours party-id)]
    [:tr
     [:th {:class "px-2 py-2"} (when (some #{position} (range seats)) (inc position))]
     [:td (merge {:class "px-2 py-2"} (inputs/get-colour-style party-colour)) party-name]
     [:th (merge {:class "px-2 py-2"}) (inputs/keyword->name candidate)]
     [:td {:class "px-2 py-2"} (str (candidate candidate-shares) " %")]
     (map (fn [n] (count-n-data-row candidate n counts-data table-data elected)) (keys counts-data))]))

(defn vote-counts-table [{:keys [c-data first-prefs elected]}]
  (let [{:keys [counts seats table-data]} c-data
        {:keys [candidates] :as vote-config} @(re-frame/subscribe [::subs/vote-config])
        my-ballot? @(re-frame/subscribe [::subs/my-ballot])
        count-ns (-> counts keys sort)
        candidate-order (keys (sort-by #(get-in (val %) [:position]) table-data))
        total-votes (reduce + (vals first-prefs))
        vote-shares (reduce (fn [shares cand]
                              (assoc shares
                                     cand
                                     (int (* 100 (/ (cand first-prefs) total-votes)))))
                            {}
                            candidates)]
    [:div {:class "overflow-x-auto"}
     (into
      [:table {:class styles/table-el}]

      [[:caption {:class styles/table-caption}
        "Vote Count Results"
        [:p {:class styles/table-caption-p}
         "Some more text here ... TODO"]]

       [vote-counts-header-row count-ns]

       (into
        [:tbody {:class styles/table-body}]
        (map #(vote-counts-data-row % counts table-data vote-config vote-shares seats elected) candidate-order))])

     [:div {:class "text-sm px-4 py-6"}
      (when my-ballot?
        [:p [:span {:class my-ballot-icon}] " = My Ballot"])
      [:p [:span {:class elected-colours} "Elected"]]
      [:p [:span {:class eliminated-colours} "Eliminated"]]]]))
