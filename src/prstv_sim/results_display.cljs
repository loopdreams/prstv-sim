(ns prstv-sim.results-display
  (:require [prstv-sim.subs :as subs]
            [prstv-sim.events :as events]
            [prstv-sim.inputs :as inputs]
            [prstv-sim.styles :as styles]
            [re-frame.core :as re-frame]))

;; Generate Results
(defn convert-my-ballot [preferences]
  (let [id (str (random-uuid))
        ballot (reduce (fn [b [name pref]]
                         (assoc b (inputs/name->keyword name) (parse-long pref)))
                       {}
                       preferences)]
    (zipmap [id] [ballot])))

(defn generate-results-button []
  (let [vote-config      @(re-frame/subscribe [::subs/vote-config])
        my-ballot        @(re-frame/subscribe [::subs/my-ballot])
        results-loading? @(re-frame/subscribe [::subs/results-loading?])
        my-ballot        (if my-ballot (convert-my-ballot my-ballot) {})
        ballot-id        (when (seq my-ballot) (ffirst my-ballot))
        seats            (:n-seats vote-config)
        candidates       (:candidates vote-config)]
    [:div {:class "flex flex-col align-center text-center"}
     [:div {:class "m-auto"}
      [:button
       {:class styles/special-button
        :on-click #(re-frame/dispatch [::events/process-results vote-config candidates my-ballot ballot-id seats])
        :disabled (or (not vote-config) (= results-loading? :loading))}
       "Generate Results"]]
     (when (not vote-config)
       [:div {:class "text-xs md:text-sm text-slate-800 dark:text-slate-300"} "You must add a vote configuration before generating the results."])]))

;; Components

(defn elected-candidate-display [candidate {:keys [party-names candidate-party party-colours]}]
  (let [party-id (-> candidate candidate-party)
        party-name (-> party-id party-names)]
    [:div (merge (styles/get-colour-style (party-colours party-id))
                 {:class "py-2 px-4 rounded-lg text-bold text-sm md:text-lg"})
     (str (inputs/keyword->name candidate) " (" party-name ")")]))

(defn elected-display [elected]
  (let [vote-config @(re-frame/subscribe [::subs/vote-config])]
    [:div
     [:h2 {:class styles/default-h2} "Elected:"]
     (into
      [:div {:class "px-2.5 md:px-5 flex flex-row flex-wrap gap-2 md:gap-4 content-between"}]
      (for [[idx elec] (map-indexed vector elected)]
        ^{:key idx}
        (elected-candidate-display elec vote-config)))]))


(defn vote-counts-header-row [count-ns]
  [:thead {:class styles/table-head}
   [:tr
    [:th {:class "px-2 py-4"} "Seat"]
    [:th {:class "px-2"} "Party"]
    [:th {:class "px-2"} "Candidate"]
    [:th {:class "px-2"}"Share"]
    (for [n count-ns]
      ^{:key (inc n)}
      [:th {:class "px-2"}
       (str "Count " (inc n))])]])

(def elected-colours "font-bold bg-teal-100 text-teal-500 p-0.5 rounded")
(def eliminated-colours "font-bold bg-red-100 text-red-500 p-0.5 rounded")
(def my-ballot-icon "fas fa-scroll")

(defn format-count-table-cell [{:keys [count count-change elected eliminated marked-ballot candidate nth-col]}]
  [:td
   {:class (str "px-2 "
                (cond
                  elected elected-colours
                  eliminated eliminated-colours
                  :else ""))
    :on-click #(re-frame/dispatch [::events/sankey-selector candidate])}
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
       {:nth-col nth-col
        :candidate candidate
        :count count
        :count-change count-change
        :elected (when (= exit-at (inc nth-col)) (elected candidate))
        :eliminated (and (= exit-at (inc nth-col)) (not (elected candidate)))
        :marked-ballot marked-ballot?}])))

(defn party-icon [colour]
  [:span {:class "fas fa-circle pr-1"
          :style {:color (styles/colour-styles colour)}}])

(defn vote-counts-data-row [candidate counts-data table-data vote-config candidate-shares seats elected]
  (let [{:keys [position]}                                  (candidate table-data)
        {:keys [candidate-party party-colours party-names]} vote-config
        party-id                                            (candidate candidate-party)
        party-name                                          (party-names party-id)
        party-colour                                        (party-colours party-id)]
    [:tr
     [:th {:class "px-2 py-2"} (when (some #{position} (range seats)) (inc position))]
     ;; [:td (merge {:class "px-2 py-2"} (styles/get-colour-style party-colour)) party-name]
     [:td (party-icon party-colour) (str " " party-name)]
     [:th (merge {:class "px-2 py-2 cursor-pointer"
                  :on-click #(re-frame/dispatch [::events/sankey-selector candidate])})
      (inputs/keyword->name candidate)]
     [:td {:class "px-2 py-2"} (str (candidate candidate-shares) " %")]
     (doall
      (for [n (keys counts-data)]
        ^{:key n}
        (count-n-data-row candidate n counts-data table-data elected)))]))


(defn vote-counts-table [{:keys [c-data first-prefs elected]}]
  (let [{:keys [counts seats table-data quota]} c-data
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
         "This table shows how the ballots were distributed for each count. Vote flows for the counts are available in a chart below. You can also see the vote flows for a specific candidate by clicking on their name in the table."]
        [:p [:span {:class styles/table-caption-p} "Quota: "] quota]]


       [vote-counts-header-row count-ns]

       (into
        [:tbody {:class styles/table-body}]
        (for [[idx candidate] (map-indexed vector candidate-order)]
          ^{:key idx}
          (vote-counts-data-row candidate counts table-data vote-config vote-shares seats elected)))])

     [:div {:class "text-xs md:text-sm px-4 py-6"}
      (when my-ballot?
        [:p {:class "dark:text-slate-100"} [:span {:class my-ballot-icon}] " = My Ballot"])
      [:p [:span {:class elected-colours} "Elected"]]
      [:p [:span {:class eliminated-colours} "Eliminated"]]]]))
