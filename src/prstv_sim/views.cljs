(ns prstv-sim.views
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.subs :as subs]
   [prstv-sim.events :as events]
   [prstv-sim.vote-counter :as counter]
   [prstv-sim.inputs :as inputs]
   [prstv-sim.vote-generator :as votes]
   [prstv-sim.results-display :as results]))


#_(defn input-form-top-row []
    [:div.level {:class "box"}
     [inputs/set-number-of-votes]
     [inputs/set-preference-depth]
     [inputs/set-volatility]])

(defn input-form-mid-row []
  [:div.columns
   [:div.box
    [inputs/party-input-table]]
   [:div.box
    [inputs/candidate-input-table]]
   [:div.box
    [inputs/set-number-of-votes]
    [inputs/set-preference-depth]
    [inputs/set-volatility]
    [inputs/set-number-of-seats]]])

(defn save-ballots []
  (let [vote-config @(re-frame/subscribe [::subs/vote-config])
        ballots (votes/prstv-vote-generator vote-config)]
    (when vote-config
      [:div
       [:button.button
        {:on-click
         #(re-frame/dispatch [::events/save-votes ballots])}
        "Save Ballots"]])))

(defn calculate-results []
  (let [vote-config @(re-frame/subscribe [::subs/vote-config])
        ballots     @(re-frame/subscribe [::subs/total-votes])
        seats       (:n-seats vote-config)
        candidates  (:candidates vote-config)]
    (when ballots
      (let [[elected counts first-prefs] (counter/run-vote-counts candidates ballots seats)]
        [:div
         [:button.button
          {:on-click #(re-frame/dispatch [::events/add-results elected counts first-prefs])}
          "Calculate Results"]]))))

(defn generate-ballots-and-calculate-results []
  (let [vote-config @(re-frame/subscribe [::subs/vote-config])]
    (when vote-config
      (let [ballots (votes/prstv-vote-generator vote-config)
            seats   (:n-seats vote-config)
            candidates (:candidates vote-config)
            [elected counts first-prefs c-data] (counter/run-vote-counts candidates ballots seats)]
        [:div
         [:button.button
          {:on-click #(re-frame/dispatch [::events/add-results elected counts first-prefs c-data])}
          "Calculate Results"]]))))

(defn ballots-make []
  (let [vote-config @(re-frame/subscribe [::subs/vote-config])
        ballots (votes/prstv-vote-generator vote-config)]
    (when vote-config
      (let [[_ sample] (first ballots)]
        [:ul
         (map (fn [[cand pref]] [:li (str cand ": " pref)]) sample)]))))



(defn main-panel []
  (let [name    (re-frame/subscribe [::subs/name])
        results @(re-frame/subscribe [::subs/results])]
    [:div
     [:div.section {:class "has-background-primary-white"}
      [:h1 {:class "title has-text-light"}
       "Single Transferrable Vote Simulator"]]
     [:div.section
      [input-form-mid-row]
      [:div.box
       [inputs/inputs->vote-config]
       [generate-ballots-and-calculate-results]]
      (when results
        (let [canidate-first-prefs (:first-prefs results)
              elected              (:elected results)
              quota                (:quota (:c-data results))]
          [:div.box
           [:h1.title "Results"]
           [results/elected-display elected]
           [results/first-prefs-table canidate-first-prefs]
           [results/party-first-prefs-table canidate-first-prefs]
           [:h2 [:span.has-text-weight-bold "Quota: "] quota]
           [results/vote-counts-table results]]))]]))
