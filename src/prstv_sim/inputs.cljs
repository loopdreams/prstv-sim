(ns prstv-sim.inputs
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.subs :as subs]
   [prstv-sim.events :as events]
   [prstv-sim.sample-configs :as v-configs]
   [prstv-sim.styles :as styles]
   [clojure.string :as str]))

(def n-votes-limit 900000)
(def n-votes-warning 90000)

(def preference-depth-options ["Shallow" "Mid" "Deep"])

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

;; Vote Configs

(defn set-number-of-votes []
  (let [value (re-frame/subscribe [::subs/inputs :n-votes])
        field-ok (and (seq @value) (valid-number-of-votes?))
        field-warning (> @value n-votes-warning)]
    [:div {:class "mb-6"}
     [:label {:class (if-not field-ok
                       styles/warning-label
                       styles/default-label)} "Number of Votes" [hoverable-info-icon "TODO"]]
     [:div
      [:input
       {:class (if-not field-ok styles/warning-input-field styles/default-input-field)
        :type "number"
        :value @value
        :placeholder "Enter number of votes"
        :on-change #(re-frame/dispatch [::events/update-inputs :n-votes (-> % .-target .-value)])}]
      (cond
        (not field-ok) [:div {:class styles/warning-text}
                        (str "Number of votes should be less than " n-votes-limit)]
        field-warning [:div {:class styles/caution-text}
                       "It will take several seconds to generate results when vote counts are large, due to the time it takes to generate the ballots."]
        :else nil)]]))

(defn set-number-of-seats []
  (let [value (re-frame/subscribe [::subs/inputs :n-seats])
        field-ok (and (seq @value) (valid-number-of-seats?))]
    [:div {:class "mb-6"}
     [:label {:class (if-not field-ok
                       styles/warning-label
                       styles/default-label)} "Number of Seats" [hoverable-info-icon "TODO"]]
     [:div
      [:input
       {:class (if-not field-ok styles/warning-input-field styles/default-input-field)
        :type "number"
        :value @value
        :placeholder "Enter number of seats"
        :on-change #(re-frame/dispatch [::events/update-inputs :n-seats (-> % .-target .-value)])}]
      (when-not field-ok
        [:div {:class styles/warning-text}
         "Number of seats has to be a number greater than 0 and less than the total number of candidates."])]]))

(defn set-volatility []
  (let [value (re-frame/subscribe [::subs/inputs :volatility])]
    [:div {:class "mb-6"}
     [:label {:class styles/default-label}"Volatility" [hoverable-info-icon "TODO"]]
     [:div
      [:input
       {:class styles/default-input-field
        :type "number"
        :value @value
        :placeholder "(Optional) Enter number between 1 and 100"
        :on-change
        #(re-frame/dispatch [::events/update-inputs :volatility (-> % .-target .-value)])}]
      (when (and (seq @value) (not (valid-number-volatility?)))
        [:div.has-text-danger
         "Use a number between 1 and 100"])]]))

;; TODO set default radio setting
(defn set-preference-depth []
  [:div {:class "mb-6"}
   [:label {:class styles/default-label} "Preference Depth" [hoverable-info-icon "TODO"]]
   (into
    [:div]
    (for [p preference-depth-options]
      [:div {:class "flex items-center mb-4"}
       [:input {:name "preference-depth"
                :type "radio"
                :value p
                :id p
                :class "w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600"
                :on-change #(when (-> % .-target .-checked)
                              (re-frame/dispatch [::events/update-inputs
                                                  :preference-depth
                                                  (keyword (str/lower-case p))]))}]
       [:label {:class "ms-2 text-sm font-medium text-gray-900 dark:text-gray-300"} p]]))])


(defn set-vote-params []
  [:div
   [:h2 {:class styles/default-h2} "Vote Parameters"]
   [:form
    [set-number-of-votes]
    [set-number-of-seats]
    [set-preference-depth]
    [set-volatility]]])


;; Tables

(def delete-icon
  [:span {:class "fas fa-times"}])

(defn table-field-input-el [table id column val type & colour]
  [:td {:class "px-6 py-4"}
   [:input (merge {:type type
                   :class styles/default-input-field
                   :placeholder "Enter Name"
                   :value val
                   :on-change #(re-frame/dispatch [::events/update-table-field table id column (-> % .-target .-value)])}
                  (when colour (styles/get-colour-style (first colour))))]])

(defn table-field-select-el [table id column val options & party-id]
  [:td {:class "px-6 py-4"}
   [:form {:class "max-w-sm mx-auto"}
    (into
     [:select {:class styles/drop-down-select
               :value val
               :on-change #(re-frame/dispatch [::events/update-table-field table id column (-> % .-target .-value) party-id])}]
     [[:option "Select Party"]
      (for [o options]
        [:option o])])]])

(defn party-table-form-row [[id {:keys [name popularity colour]}]]
  (let [active-party-ids @(re-frame/subscribe [::subs/active-party-ids])]
    [:tr {:class styles/table-body}
     [table-field-input-el :party id :name name "text" colour]
     [table-field-input-el :party id :popularity popularity "number"]
     [table-field-select-el :party id :colour colour styles/party-colours-list]
     [:td
      [:button {:disabled (when (some #{id} active-party-ids) true)
                :on-click #(re-frame/dispatch [::events/delete-inputs :party id])} delete-icon]]]))

(defn candidate-table-form-row [[id {:keys [name popularity party-id]}]]
  (let [party-data @(re-frame/subscribe [::subs/inputs :party party-id])
        active-parties @(re-frame/subscribe [::subs/active-party-names])
        colour (or (:colour party-data) "White")
        p-name (:name party-data)]
    [:tr
     [table-field-input-el :candidate id :name name "text" colour]
     [table-field-select-el :candidate id :party p-name active-parties :party-id]
     [table-field-input-el :candidate id :popularity popularity "number"]
     [:td
      [:button {:on-click #(re-frame/dispatch [::events/delete-inputs :candidate id])} delete-icon]]]))


(defn table-form-component [col-headings row-data caption-title caption-text form-type]
  [:div {:class styles/table-outer-div}
     (into
      [:table {:class styles/table-el}]
      [[:caption {:class styles/table-caption}
        caption-title
        [:p {:class styles/table-caption-p} caption-text]]
       [:thead {:class styles/table-head}
        (into [:tr]
              (for [name col-headings]
                [:th {:class (str "px-6 py-3" (when (= name "Popularity") " sm:w-10"))} name]))]
       [:tbody
        (for [r row-data]
          (if (= form-type :party)
            [party-table-form-row r]
            [candidate-table-form-row r]))]])])

(defn party-table-form []
  (let [rows @(re-frame/subscribe [::subs/inputs :party])]
    [:div
     [table-form-component
      ["Name" "Popularity" "Colour"]
      rows
      "Parties"
      "Set the parties that will be running in the election. Set the party name, how popular the party is, and a colour for the party."
      :party]
     [:div.pt-5
      [:button
       {:class styles/default-button
        :on-click #(re-frame/dispatch [::events/add-blank-table-row :party])}
       "Add Party"]]]))

(defn candidate-table-form []
  (let [rows @(re-frame/subscribe [::subs/inputs :candidate])]
    [:div
     [table-form-component
      ["Name" "Party" "Popularity"]
      rows
      "Candidates"
      "Select the candidates ..."]
     [:div.pt-5
      [:button {:class styles/default-button
                :on-click #(re-frame/dispatch [::events/add-blank-table-row :candidate])}
       "Add Candidate"]]]))


;; My Ballot

(defn ballot-form-row [cand-name]
  (let [val (or (-> @(re-frame/subscribe [::subs/my-ballot]) (get cand-name)) "Select Preference")
        available-preferences @(re-frame/subscribe [::subs/available-preferences])
        available-preferences (if (not= val "Select Preference") (conj available-preferences val) available-preferences)]
    [:div {:class "mb-6"}
     [:label {:class styles/default-label} cand-name]
     (into
      [:select {:class styles/drop-down-select
                :value val
                :on-change #(re-frame/dispatch [::events/update-my-ballot cand-name (-> % .-target .-value)])}]
      (cons [:option "Select Preference"]
            (map (fn [p] [:option p]) available-preferences)))]))


(defn user-ballot-form []
  (let [candidates @(re-frame/subscribe [::subs/inputs :candidate])
        my-ballot? @(re-frame/subscribe [::subs/my-ballot?])
        vote-config @(re-frame/subscribe [::subs/vote-config])
        c-names (->> (vals candidates) (map :name))]
    (if my-ballot?
      [:div
       [:h2 {:class styles/default-h2 } "My Ballot"]
       (into [:div]
             (map ballot-form-row c-names))]
      [:div
       [:button {:class styles/default-button
                 :on-click #(re-frame/dispatch [::events/activate-my-ballot])
                 :disabled (not vote-config)}
        "Create and track your own ballot"]
       (when (not vote-config)
         [:p "Configure the vote before adding your ballot"])])))



;; Pre-Configs

;; TODO different button style
(defn preconfig-selector-button [[_ {:keys [name values]}]]
  [:button.button.is-link.is-outlined
   {:class styles/default-button
    :on-click #(re-frame/dispatch [::events/load-input-config values])}
   name])

(defn preconfig-options-selector []
  [:div
   [:h2 {:class styles/default-h2} "Configuration Profiles"]
   (conj
    (into [:div]
          (map preconfig-selector-button v-configs/sample-config-options-list))
    [preconfig-selector-button [nil {:name "Clear All" :values nil}]])])



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




;; MAYBE Ignore blank candidates
;; TODO each candidate entry must have all keys
;; TODO Number of seats less than no. candidates
;; TODO number votes less than x

(defn valid-inputs? [n-seats n-votes candidates]
  (and
    (< n-seats (count candidates))
    (< n-votes n-votes-limit)
    (every? #(contains? % :name) (vals candidates))
    (every? #(contains? % :party-id) (vals candidates))))



;; TODO proper validation here
;; TODO User feedback when config is added successfully
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
            vote-config  {:n-votes              (parse-long n-votes)
                          :n-seats              (parse-long n-seats)
                          :candidates           c-names
                          :party-names          p-names
                          :party-colours        p-colours
                          :candidate-popularity c-popularity
                          :candidate-party      c-party
                          :party-popularity     p-popularity
                          :preference-depth     (keyword preference-depth)
                          :volatility           (parse-long volatility)
                          :volatility-pp        5}] ;; TODO set this somewhere else...

          [:div
           [:button
            {:class styles/default-button
             :disabled (not (valid-inputs? n-seats n-votes candidate))
             :on-click #(re-frame/dispatch [::events/add-vote-config vote-config])}
            "Add Vote Config"]
           (when (or (not (every? #(contains? % :name) (vals candidate)))
                     (not (every? #(contains? % :party-id) (vals candidate))))
             [:div {:class styles/warning-text}
              "Incomplete information for a Candidate in the Candidate Table"])]))))
