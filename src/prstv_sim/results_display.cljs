(ns prstv-sim.results-display
  (:require [prstv-sim.subs :as subs]
            [prstv-sim.events :as events]
            [prstv-sim.inputs :as inputs]
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
  [:thead
   [:tr
    [:th "Seat"]
    [:th "Party"]
    [:th "Candidate"]
    [:th "Share"]
    (map (fn [n] [:th (str "Count " (inc n))]) count-ns)]])

;; TODO 20210328T000009--kropotkin2021mutual__philosophy_nodate.orgproper formatting for marked ballot
(defn colour-candidate-exit [candidate exit-at nth-col elected data marked-ballot]
  [:span
   (when (= exit-at (inc nth-col))
     (if (elected candidate)
       {:class "has-text-sucess has-background-success-light"}
       {:class "has-text-danger has-background-danger-light"}))

   (str data (when marked-ballot "•••"))])

(defn count-n-data-row [candidate nth-col counts-data table-data elected]
  (let [exit-at        (-> candidate table-data :exit)
        count          (-> (counts-data nth-col) :counts candidate)
        count-change   (-> (counts-data nth-col) :count-changes candidate)
        count-display  (fn [c b] [colour-candidate-exit candidate exit-at nth-col elected c b])
        cand-vote-ids  (-> (counts-data nth-col) :piles candidate)
        target-ballot  @(re-frame/subscribe [::subs/marked-ballot])
        marked-ballot? (some #{target-ballot} cand-vote-ids)]
    (cond
      (not count)                    [:td ""]
      (or (= nth-col 0)
          (not (pos? count-change))) [:td [count-display count marked-ballot?]]
      :else                          [:td [count-display count marked-ballot?]
                                      [:span {:class "has-text-grey"} (str " (+ " count-change ")")]])))

(defn vote-counts-data-row [candidate counts-data table-data vote-config candidate-shares seats elected]
  (let [{:keys [position]}                                  (candidate table-data)
        {:keys [candidate-party party-colours party-names]} vote-config
        party-id                                            (candidate candidate-party)
        party-name                                          (party-names party-id)
        party-colour                                        (party-colours party-id)]
    [:tr
     [:th (when (some #{position} (range seats)) (inc position))]
     [:td (inputs/get-colour-style party-colour) party-name]
     [:td (inputs/keyword->name candidate)]
     [:td (str (candidate candidate-shares) " %")]
     (map (fn [n] (count-n-data-row candidate n counts-data table-data elected)) (keys counts-data))]))

(defn vote-counts-table [{:keys [c-data first-prefs elected]}]
  (let [{:keys [counts seats table-data]} c-data
        {:keys [candidates] :as vote-config} @(re-frame/subscribe [::subs/vote-config])
        count-ns (-> counts keys sort)
        candidate-order (keys (sort-by #(get-in (val %) [:position]) table-data))
        total-votes (reduce + (vals first-prefs))
        vote-shares (reduce (fn [shares cand]
                              (assoc shares
                                     cand
                                     (int (* 100 (/ (cand first-prefs) total-votes)))))
                            {}
                            candidates)]
    [:table.table
     [vote-counts-header-row count-ns]
     [:tbody
      (map #(vote-counts-data-row % counts table-data vote-config vote-shares seats elected) candidate-order)]]))
