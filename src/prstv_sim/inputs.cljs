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

(def preference-depth-options ["Shallow" "Mid" "Deep" "Random"])

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

(defn hoverable-info-icon [info-text]
  [:div {:class "grow pl-2 dark:text-slate-200"}
   [styles/tooltip
    info-text
    [:span.icon
     [:i {:class "fas fa-info-circle"}]]]])

;; Vote Configs

(defn set-number-of-votes []
  (let [value (re-frame/subscribe [::subs/inputs :n-votes])
        field-ok (and (seq @value) (valid-number-of-votes?))
        field-warning (> @value n-votes-warning)]
    [:div {:class "py-1.5"}
     [:div {:class "flex flex-row"}
      [:label {:class (if-not field-ok styles/warning-label styles/default-label)} "Number of Votes"]
      [hoverable-info-icon "Set how many total votes will be cast. More votes take much longer to simiulate."]]
     [:input
      {:class (str "mb-4 " (if-not field-ok styles/warning-input-field styles/default-input-field))
       :type "number"
       :value @value
       :placeholder "Enter number of votes"
       :on-change #(re-frame/dispatch [::events/update-inputs :n-votes (-> % .-target .-value)])}]
     (cond
       (not field-ok) [:div {:class styles/warning-text}
                       (str "Number of votes should be less than " n-votes-limit)]
       field-warning [:div {:class styles/caution-text}
                      "It will take several seconds to generate results when vote counts are large, due to the time it takes to generate the ballots."]
       :else nil)]))

(defn set-number-of-seats []
  (let [value (re-frame/subscribe [::subs/inputs :n-seats])
        field-ok (and (seq @value) (valid-number-of-seats?))]
    [:div {:class "py-1.5"}
     [:div {:class "flex flex-row"}
      [:label {:class (if-not field-ok styles/warning-label styles/default-label)}
       "Number of Seats"]
      [hoverable-info-icon "Set how many seats are available in the area. Should be less than the number of candidates."]]
     [:div {:class "mb-4"}
      [:input
       {:class (str (if-not field-ok styles/warning-input-field styles/default-input-field))
        :type "number"
        :value @value
        :placeholder "Enter number of seats"
        :on-change #(re-frame/dispatch [::events/update-inputs :n-seats (-> % .-target .-value)])}]
      (when-not field-ok
        [:div {:class styles/warning-text}
         "Number of seats has to be a number greater than 0 and less than the total number of candidates."])]]))

(defn set-volatility []
  (let [value (re-frame/subscribe [::subs/inputs :volatility])]
    [:div {:class "py-1.5"}
     [:div {:class "flex flex-row"}
      [:label {:class styles/default-label}"Volatility"]
      [hoverable-info-icon "Set the degree of randomness when generating the ballots"]]
     [:div
      [:input
       {:class (str "mt-2 " styles/default-input-field)
        :type "number"
        :value @value
        :placeholder "(Optional) Enter number between 1 and 100"
        :on-change
        #(re-frame/dispatch [::events/update-inputs :volatility (-> % .-target .-value)])}]]]))

;; TODO set default radio setting
(defn set-preference-depth []
  [:div {:class "py-1.5"}
   [:div {:class "flex flex-row"}
    [:label {:class styles/default-label} "Preference Depth"]
    [hoverable-info-icon "Set the weighting for how many preferences people will choose on the ballot. 'Deeper' preferences leads to more possible transfers."]]
   (into
    [:div {:class "mt-2"}]
    (for [pref preference-depth-options]
      [:div {:class "flex items-center mb-4"}

       [:input {:name "preference-depth"
                :type "radio"
                :value pref
                :id pref
                :class "w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600"
                :on-change #(when (-> % .-target .-checked)
                              (re-frame/dispatch [::events/update-inputs :preference-depth (keyword (str/lower-case pref))]))}]
       [:label {:class "ms-2 text-xs md:text-sm font-medium text-gray-900 dark:text-gray-300"} pref]]))])



(defn set-vote-params []
  [:div {:class (str styles/inputs-dark-border " shadow-md border-2 p-6")}
   [:h2 {:class styles/default-h2} "Vote Parameters"]
   [:form
    [set-number-of-votes]
    [set-number-of-seats]
    [set-preference-depth]
    [set-volatility]]])


;; Tables


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

(def delete-icon
  [:span {:class "fas fa-times"}])

(def delete-icon-disabled
  [:span {:class "fas fa-times text-slate-200 dark:text-slate-600"}])

(defn table-field-input-el [table id column val type & colour]
  [:td {:class styles/table-cell}
   [:input (merge {:type type
                   :class styles/default-input-field
                   :placeholder "Enter Name"
                   :value val
                   :on-change #(re-frame/dispatch [::events/update-table-field table id column (-> % .-target .-value)])}
                  (when colour (styles/get-colour-style (first colour))))]])

(defn table-field-select-el [table id column val options & party-id]
  [:td {:class "px-2 py-1"}
   [:form {:class "max-w-sm mx-auto"}
    (into
     [:select {:class styles/drop-down-select
               :value val
               :on-change #(re-frame/dispatch [::events/update-table-field table id column (-> % .-target .-value) party-id])}]
     [[:option "Select Party"]
      (for [[idx o] (map-indexed vector options)]
        ^{:key idx} [:option o])])]])

(defn party-table-form-row [[id {:keys [name popularity colour]}]]
  (let [active-party-ids @(re-frame/subscribe [::subs/active-party-ids])]
    [:tr {:class styles/table-body}
     [table-field-input-el :party id :name name "text" colour]
     [table-field-select-el :party id :colour colour styles/party-colours-list]
     [table-field-input-el :party id :popularity popularity "number"]
     [:td
      [:button {:disabled (when (some #{id} active-party-ids) true)
                :on-click #(re-frame/dispatch [::events/delete-inputs :party id])}
       (if (some #{id} active-party-ids) delete-icon-disabled delete-icon)]]]))

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
                [:th {:class (str "px-2 md:px-6 py-1 md:py-3" (when (= name "Popularity") " w-10"))} name]))]
       [:tbody
        (for [[idx r] (map-indexed vector row-data)]
          (if (= form-type :party)
            ^{:key idx} [party-table-form-row r]
            ^{:key idx} [candidate-table-form-row r]))]])])

(defn party-table-form []
  (let [rows @(re-frame/subscribe [::subs/inputs :party])]
    [:div
     [table-form-component
      ["Name" "Colour" "Popularity"]
      rows
      "Parties"
      "Set the parties that will be running in the election. Set the party name, how popular the party is, and a colour for the party."
      :party]
     [:div {:class "pt-5"}
      [:button
       {:class styles/table-add-button
        :on-click #(re-frame/dispatch [::events/add-blank-table-row :party])}
       "Add Party"]]]))

(defn candidate-table-form []
  (let [rows @(re-frame/subscribe [::subs/inputs :candidate])]
    [:div {:class "mt-2"}
     [table-form-component
      ["Name" "Party" "Popularity"]
      rows
      "Candidates"
      "Select the candidates that will be in the election. Set the party for each candidate and optionally set the popularity rating."]
     [:div.pt-5
      [:button {:class styles/table-add-button
                :on-click #(re-frame/dispatch [::events/add-blank-table-row :candidate])}
       "Add Candidate"]]]))


;; My Ballot

(defn ballot-form-row [candidate {:keys [candidate-party party-names party-colours]}]
  (let [val                   (or (-> @(re-frame/subscribe [::subs/my-ballot]) (get candidate)) "Select Preference")
        available-preferences @(re-frame/subscribe [::subs/available-preferences])
        available-preferences (if (not= val "Select Preference") (conj available-preferences val) available-preferences)
        c-party               (-> candidate-party candidate)
        party-name            (party-names c-party)
        party-color           (party-colours c-party)]
    [:div {:class "mb-6"}
     [:label {:class styles/default-label}
      [styles/party-icon party-color] (str (keyword->name candidate) " (" party-name ")")
      (into
       [:select {:class     (str "mt-2 " styles/drop-down-select)
                 :value     val
                 :on-change #(re-frame/dispatch [::events/update-my-ballot candidate (-> % .-target .-value)])}]
       (cons [:option "Select Preference"]
             (map (fn [p] [:option p]) available-preferences)))]]))


(defn user-ballot-form []
  (let [my-ballot? @(re-frame/subscribe [::subs/my-ballot?])
        vote-config @(re-frame/subscribe [::subs/vote-config])
        candidates (:candidates vote-config)]
    (if my-ballot?
      [:div {:class "md:m-auto md:w-3/5"}
       [:h2 {:class styles/default-h2 } "My Ballot"]
       (into [:div]
             (map #(ballot-form-row % vote-config) candidates))]
      [:div {:class "flex flex-col align-center text-center"}
       [:div {:class "m-auto"}
        [:button {:class styles/special-button
                  :on-click #(re-frame/dispatch [::events/activate-my-ballot])
                  :disabled (not vote-config)}
         "Create and track your own ballot (Optional)"]]
       (when (not vote-config)
         [:div {:class "text-xs md:text-sm text-slate-800 dark:text-slate-400"}
          "Configure the vote before adding your ballot"])])))



;; Pre-Configs

(defn preconfig-selector-button [[_ {:keys [name values]}] active-preconfig]
  [:button
   {:class (if (= active-preconfig name) styles/config-profile-button-active styles/config-profile-button)
    :on-click #(re-frame/dispatch [::events/load-input-config values name])}
   name])

(defn preconfig-options-selector []
  (let [active-preconifg @(re-frame/subscribe [::subs/active-preconfig])]
    [:div {:class (str styles/inputs-dark-border " border shadow-md p-6")}
     [:h2 {:class styles/default-h2} "Configuration Profiles"]
     (conj
      (into [:div]
            (map #(preconfig-selector-button % active-preconifg) v-configs/sample-config-options-list))
      [preconfig-selector-button [nil {:name "Clear All" :values nil}]])]))



;; Convert Inputs to Vote Map

(defn valid-inputs? [n-seats n-votes candidates]
  (and
    (< n-seats (count candidates))
    (< n-votes n-votes-limit)
    (every? #(contains? % :name) (vals candidates))
    (every? #(contains? % :party-id) (vals candidates))))

(defn inputs->vote-config []
  (let [{:keys [preference-depth
                n-votes
                n-seats
                party
                candidate
                volatility]} @(re-frame/subscribe [::subs/inputs])
        my-ballot? @(re-frame/subscribe [::subs/my-ballot?])
        results? @(re-frame/subscribe [::subs/results])]
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

        [:div {:class "flex flex-col justify-center"}
         [:div {:class "m-auto pt-4"}
          [:button
           {:class styles/special-button
            :disabled (not (valid-inputs? n-seats n-votes candidate))
            :on-click #(re-frame/dispatch [::events/add-vote-config-fx vote-config])}
           "Add Vote Config"]]
         (when (or (not (every? #(contains? % :name) (vals candidate)))
                   (not (every? #(contains? % :party-id) (vals candidate))))
           [:div {:class (str styles/warning-text " text-center")}
            "Incomplete information for a Candidate in the Candidate Table"])
         (when (or my-ballot? results?)
           [:div {:class "text-slate-400 text-center text-sm"}
            (str "Note: Adding a new vote config will clear results and 'My Ballot'")])
         [:div {:class "h-6"}
          [:span {:id "config-added-confirmation"
                  :class "text-center text-teal-600 text-xs md:text-sm"
                  :style {:display "none"}}
           "Vote Config Added Successfully!"]]]))))
