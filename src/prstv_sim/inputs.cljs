(ns prstv-sim.inputs
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.subs :as subs]
   [prstv-sim.events :as events]
   [clojure.pprint :as p]))


(def n-votes-limit 900000)

(def party-colours-list ["default"
                         "dark blue"
                         "light blue"
                         "dark green"
                         "light green"
                         "yellow"
                         "red"])

(defn get-bulma-style [party-colour]
  (case party-colour
    "default" "has-background-text has-text-white"
    "dark blue" "has-background-link has-text-link-invert"
    "light blue" "has-background-info has-text-info-invert"
    "dark green" "has-background-success has-text-success-invert"
    "light green" "has-background-primary has-text-primary-invert"
    "yellow" "has-background-warning has-text-warning-invert"
    "red" "has-background-danger has-text-danger-invert"))


;; TODO perhaps allow commas in input
(defn valid-number-of-votes? []
  (let [value @(re-frame/subscribe [::subs/inputs :n-votes])]
    (when (every? #(re-find #"\d" %) value)
      (< 0 (parse-long value) n-votes-limit))))

(defn set-number-of-votes []
  (let [value (re-frame/subscribe [::subs/inputs :n-votes])]
    [:div.section
     [:div.field
      [:label.label "Number of Votes"]
      [:input.input
       {:type "text"
        :value @value
        :placeholder "Enter number of votes"
        :on-change
        #(re-frame/dispatch [::events/update-inputs :n-votes (-> % .-target .-value)])}]
      (when (and (seq @value) (not (valid-number-of-votes?)))
        [:div.has-text-danger
         (p/cl-format nil "Total number of votes must be a NUMBER less than ~:d." n-votes-limit)])]]))


;; Parties

(defn party-row-display [id]
  (let [{:keys [name popularity colour]} @(re-frame/subscribe [::subs/inputs :party id])]
    [:tr
     [:th {:class (get-bulma-style colour)} name]
     [:td (when popularity (str popularity " %"))]
     [:td [:button.delete {:on-click #(re-frame/dispatch [::events/delete-inputs :party id])}]]]))


(defn party-row-add []
  (let [value @(re-frame/subscribe [::subs/party-form])
        name (get value :name "")
        popularity (get value :popularity "")
        colour (get value :colour "")
        is-valid? @(re-frame/subscribe [::subs/form-valid? :party-form])]
    [:div
     [:form.box
      [:div.field
       [:label.label "Party Name"]
       [:div.control
        [:input.input
         {:type "text" :placeholder "Party Name" :value name
          :on-change #(re-frame/dispatch [::events/update-form :party-form :name (-> % .-target .-value)])}]]]
      [:div.field
       [:label.label "Popularity"]
       [:div.control
        [:input.input
         {:type "text" :placeholder "Popularity % - between 0 and 100" :value popularity
          :on-change #(re-frame/dispatch [::events/update-form :party-form :popularity (-> % .-target .-value)])}]]]
      [:div.field
       [:label.label "Colour"]
       [:div.control
        [:div.select
         [:select {:value colour
                   :on-change #(re-frame/dispatch [::events/update-form :party-form :colour (-> % .-target .-value)])}
          [:option "Select colour"]
          (map (fn [col] [:option {:key col :val col} col]) party-colours-list)]]]]]
     [:button.button
      {:disabled (not is-valid?)
       :class "is-primary"
       :on-click #(re-frame/dispatch [::events/add-form :party-form :party])}
      "Add Party"]]))



;; TODO 'colour selector for parties'
(defn party-input-table []
  (let [data (re-frame/subscribe [::subs/inputs :party])
        row-ids (keys @data)]
    [:div.section
     [:h2.title "Parties"]
     (when row-ids
       [:table.table
        [:thead
         [:tr
          [:th "Name"]
          [:th "Popularity"]
          [:th ""]]
         [:tbody]
         (map party-row-display row-ids)]])
     [party-row-add]]))

;; Candidates

(defn candidate-row-add []
  (let [parties @(re-frame/subscribe [::subs/party-list])
        value @(re-frame/subscribe [::subs/candidate-form])
        name (get value :name "")
        popularity (get value :popularity "")
        party (get value :party "")
        is-valid? @(re-frame/subscribe [::subs/form-valid? :candidate-form])]
    [:div
     [:form.box
      [:div.field
       [:label.label "Candidate Name"]
       [:div.control
        [:input.input
         {:type "text" :placeholder "Candidate Name" :value name
          :on-change #(re-frame/dispatch [::events/update-form :candidate-form :name (-> % .-target .-value)])}]]]
      [:div.field
       [:label.label "Popularity"]
       [:div.control
        [:input.input
         {:type "text" :placeholder "Popularity % - between 0 and 100" :value popularity
          :on-change #(re-frame/dispatch [::events/update-form :candidate-form :popularity (-> % .-target .-value)])}]]]
      [:div.field
       [:label.label "Party"]
       [:div.control
        [:div.select
         [:select {:value party
                   :on-change #(re-frame/dispatch [::events/update-form :candidate-form :party (-> % .-target .-value)])}
          [:option "Select party"]
          (map (fn [p] [:option {:key p :val p} p]) parties)]]]]]
     [:button.button
      {:disabled (not is-valid?)
       :class "is-primary"
       :on-click #(re-frame/dispatch [::events/add-form :candidate-form :candidate])}
      "Add candidate"]]))


(defn candidate-row-display [id]
  (let [{:keys [name popularity party]} @(re-frame/subscribe [::subs/inputs :candidate id])
        {:keys [colour]} @(re-frame/subscribe [::subs/inputs :party id])]
    [:tr
     [:th name]
     [:td {:class (get-bulma-style colour)} party]
     [:td (when popularity (str popularity " %"))]
     [:td [:button.delete {:on-click #(re-frame/dispatch [::events/delete-inputs :candidate id])}]]]))


(defn candidate-input-table []
  (let [data (re-frame/subscribe [::subs/inputs :candidate])
        row-ids (keys @data)]
    [:div.section
     [:h2.title "Candidates"]
     (when row-ids
       [:table.table
        [:thead
         [:tr
          [:th "Name"]
          [:th "Party"]
          [:th "Popularity"]
          [:th ""]]
         [:tbody]
         (map candidate-row-display row-ids)]])
     [candidate-row-add]]))
