(ns prstv-sim.inputs
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.subs :as subs]
   [prstv-sim.events :as events]
   [clojure.pprint :as p]
   [clojure.string :as str]))


(def n-votes-limit 900000)

(def preferce-depth-options ["Shallow" "Mid" "Deep"])

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
    "red" "has-background-danger has-text-danger-invert"
    "red"))

;; TODO perhaps allow commas in input
(defn valid-number-of-votes? []
  (let [value @(re-frame/subscribe [::subs/inputs :n-votes])]
    (when (every? #(re-find #"\d" %) value)
      (< 0 (parse-long value) n-votes-limit))))

(defn valid-number-of-seats? []
  (let [value @(re-frame/subscribe [::subs/inputs :n-seats])
        n-candidates (count @(re-frame/subscribe [::subs/inputs :candidate]))]
    (when (every? #(re-find #"\d" %) value)
      (< 0 (parse-long value) n-candidates))))

(defn valid-number-volatility? []
  (let [value @(re-frame/subscribe [::subs/inputs :volatility])]
    (when (every? #(re-find #"\d" %) value)
      (< 0 (parse-long value) 101))))

;; TODO tooltip functionality and content
(defn hoverable-info-icon [info-text]
  [:span.icon
   [:i {:class "fas fa-info-circle" :data-tooltip info-text}]])



(defn set-number-of-votes []
  (let [value (re-frame/subscribe [::subs/inputs :n-votes])]
    [:div.field
     [:label.label "Number of Votes" [hoverable-info-icon "TODO"]]
     [:input.input
      {:type "text"
       :value @value
       :placeholder "Enter number of votes"
       :on-change
       #(re-frame/dispatch [::events/update-inputs :n-votes (-> % .-target .-value)])}]
     (when (and (seq @value) (not (valid-number-of-votes?)))
       [:div.has-text-danger
        (p/cl-format nil "Total number of votes must be a NUMBER less than ~:d." n-votes-limit)])]))

(defn set-number-of-seats []
  (let [value (re-frame/subscribe [::subs/inputs :n-seats])]
    [:div.field
     [:label.label "Number of Seats" [hoverable-info-icon "TODO"]]
     [:input.input
      {:type "text"
       :value @value
       :placeholder "Enter number of seats"
       :on-change
       #(re-frame/dispatch [::events/update-inputs :n-seats (-> % .-target .-value)])}]
     (when (and (seq @value) (not (valid-number-of-seats?)))
       [:div.has-text-danger
        "Number of seats has to be a number greater than 0 and less than the total number of candidates."])]))




(defn set-preference-depth []
  [:div.field
   [:label.label "Preference Depth" [hoverable-info-icon "TODO"]]
   [:div.control
    (map (fn [text]
           [:label.radio
            [:input
             {:type "radio"
              :name "preference-depth"
              :on-change #(when (-> % .-target .-checked)
                            (re-frame/dispatch [::events/update-inputs
                                                :preference-depth
                                                (keyword (str/lower-case text))]))}]
            (str " " text)])
         preferce-depth-options)]])

(defn set-volatility []
  (let [value (re-frame/subscribe [::subs/inputs :volatility])]
    [:div.field
     [:label.label "Volatility" [hoverable-info-icon "TODO"]]
     [:input.input
      {:type "text"
       :value @value
       :placeholder "(Optional) Enter number between 1 and 100"
       :on-change
       #(re-frame/dispatch [::events/update-inputs :volatility (-> % .-target .-value)])}]
     (when (and (seq @value) (not (valid-number-volatility?)))
       [:div.has-text-danger
        "Use a number between 1 and 100"])]))



;; Parties & Candidates

(defn edit-button [id]
  (let [state @(re-frame/subscribe [::subs/popularity-field-state id])]
    [:span.icon.is-clickable {:on-click #(re-frame/dispatch [::events/toggle-popularity-field-state id])}
     [:i {:class (if (= state :editing) "fas fa-times" "fas fa-edit")}]]))

(defn table-popularity-field-display [id popularity key]
  (let [field-state @(re-frame/subscribe [::subs/popularity-field-state id])]
    (if (= field-state :editing)
      [:td [:input.input {:class "is-small"
                          :size 5
                          :type "text" :placeholder popularity
                          :on-change #(re-frame/dispatch [::events/update-popularity-input key id (-> % .-target .-value)])}]]
      [:td (str popularity "%  ")])))


(defn party-row-display [id active-party-ids]
  (let [{:keys [name popularity colour]} @(re-frame/subscribe [::subs/inputs :party id])
        editing? @(re-frame/subscribe [::subs/popularity-field-state id])]
    [:tr
     [:th {:class (get-bulma-style colour)} name]
     [table-popularity-field-display id popularity :party]
     [edit-button id]
     (when (and (not (active-party-ids id)) (not editing?))
       [:td [:button.delete {:on-click #(re-frame/dispatch [::events/delete-inputs :party id])}]])]))

(defn candidate-row-display [id]
  (let [{:keys [name popularity party-id]} @(re-frame/subscribe [::subs/inputs :candidate id])
        party-data @(re-frame/subscribe [::subs/inputs :party party-id])
        colour (:colour party-data)
        p-name (:name party-data)]
    [:tr
     [:th name]
     [:td {:class (get-bulma-style colour)} p-name]
     [table-popularity-field-display id popularity :candidate]
     [edit-button id]
     [:td [:button.delete {:on-click #(re-frame/dispatch [::events/delete-inputs :candidate id])}]]]))

(defn add-entries-form [{:keys [form-type form-name name-val pop-val col-party-name col-party-val select-list party-id valid]}]
  [:div
   [:form.box
    [:div.field
     [:label.label "Name"]
     [:div.control
      [:input.input {:type "text" :placeholder (str "Enter " (name form-name) " name") :value name-val
                     :on-change #(re-frame/dispatch [::events/update-form form-type :name (-> % .-target .-value)])}]]]
    [:div.field
     [:label.label "Popularity"]
     [:div.control
      [:input.input {:type "text" :placeholder "(Optional) Number between 1 and 100" :value pop-val
                     :on-change #(re-frame/dispatch [::events/update-form form-type :popularity (-> % .-target .-value)])}]]]
    [:div.field
     [:label.label (str/capitalize (name col-party-name))]
     [:div.control
      [:div.select
       [:select {:value col-party-val
                 :on-change #(re-frame/dispatch [::events/update-form form-type col-party-name (-> % .-target .-value)])}
        [:option (str "Select " (name col-party-name))]
        (map (fn [v] [:option {:key v :val v} v]) select-list)]]]]]
   [:button.button
    {:disabled (not valid)
     :class "is-primary"
     :on-click #(re-frame/dispatch [::events/add-form form-type form-name (when party-id party-id)])}
    (str "Add " (name form-name))]])

(defn party-row-add []
  (let [value      @(re-frame/subscribe [::subs/party-form])
        name       (get value :name "")
        popularity (get value :popularity "")
        colour     (get value :colour "")
        is-valid?  @(re-frame/subscribe [::subs/form-valid? :party-form])]
    [add-entries-form
     {:form-type      :party-form
      :form-name      :party
      :name-val       name
      :pop-val        popularity
      :col-party-name :colour
      :col-party-val  colour
      :select-list    party-colours-list
      :valid          is-valid?}]))

(defn candidate-row-add []
  (let [parties    @(re-frame/subscribe [::subs/party-list])
        value      @(re-frame/subscribe [::subs/candidate-form])
        name       (get value :name "")
        popularity (get value :popularity "")
        party      (get value :party "")
        party-id   @(re-frame/subscribe [::subs/party-id party])
        is-valid?  @(re-frame/subscribe [::subs/form-valid? :candidate-form])]
    [add-entries-form
     {:form-type      :candidate-form
      :form-name      :candidate
      :name-val       name
      :pop-val        popularity
      :col-party-name :party
      :col-party-val  party
      :select-list    parties
      :party-id       party-id
      :valid          is-valid?}]))

(defn party-input-table []
  (let [data @(re-frame/subscribe [::subs/inputs :party])
        row-ids (keys data)
        active-party-ids @(re-frame/subscribe [::subs/active-party-ids])]
    [:div.section
     [:h2.title "Parties"]
     (when row-ids
       [:table.table
        [:thead
         [:tr
          [:th "Name"]
          [:th "Popularity"]
          [:th ""]]]
        [:tbody
         (map #(party-row-display % active-party-ids) row-ids)]])
     [party-row-add]]))

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
          [:th ""]]]
        [:tbody
         (map candidate-row-display row-ids)]])
     [candidate-row-add]]))

;; Convert Inputs to Vote Map


(defn name->keyword [name]
  (when name
    (-> name
        str/lower-case
        (str/replace #" " "-")
        keyword)))

(defn keyword->name [kw]
  (when kw
    (->>
     (map str/capitalize (-> (name kw) (str/split #"-")))
     (str/join " "))))




;; TODO proper validation here
(defn inputs->vote-config []
  (let [{:keys [preference-depth
                n-votes
                n-seats
                party
                candidate
                volatility]} @(re-frame/subscribe [::subs/all-inputs])]
    (when (and preference-depth n-votes party candidate volatility)
      (let [c-names      (->> (map (fn [[_ {:keys [name]}]] (name->keyword name)) candidate)
                              (into #{}))
            c-popularity (->> (map (fn [[_ {:keys [name popularity]}]]
                                     [(name->keyword name) (when popularity (parse-long popularity))]) candidate)
                              (into {}))
            c-party      (->> (map (fn [[_ {:keys [name party-id]}]]
                                     [(name->keyword name) party-id]) candidate)
                              (into {}))
            p-names      (->> (map (fn [[id {:keys [name]}]] [id name]) party)
                              (into {}))
            p-colours    (->> (map (fn [[id {:keys [colour]}]] [id colour]) party)
                              (into {}))
            p-popularity (->> (map (fn [[id {:keys [popularity]}]] [id (when popularity (parse-long popularity))]) party)
                              (into {}))
            data         {:n-votes              (parse-long n-votes)
                          :n-seats              (parse-long n-seats)
                          :candidates           c-names
                          :party-names          p-names
                          :party-colours        p-colours
                          :candidate-popularity c-popularity
                          :candidate-party      c-party
                          :party-popularity     p-popularity
                          :preference-depth     (keyword preference-depth)
                          :volatility           (parse-long volatility)
                          :volatility-pp        1}]
        [:button.button
         {:on-click #(re-frame/dispatch [::events/add-vote-config data])}
         "Add Vote Config"]))))
