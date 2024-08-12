(ns prstv-sim.results-display
  (:require [prstv-sim.subs :as subs]
            [prstv-sim.events :as events]
            [prstv-sim.inputs :as inputs]
            [prstv-sim.styles :as styles]
            [re-frame.core :as re-frame]
            [clojure.set :as set]))




(defn elected-candidate-display [candidate {:keys [party-names candidate-party party-colours]}]
  (let [party-id (-> candidate candidate-party)
        party-name (-> party-id party-names)]
    [:div (merge (inputs/get-colour-style (party-colours party-id))
                 {:class "py-4 px-4 rounded-lg text-bold"})
     (str (inputs/keyword->name candidate) " (" party-name ")")]))

(defn elected-display [elected]
  (let [vote-config @(re-frame/subscribe [::subs/vote-config])]
    [:div
     [:h2 {:class "p-5 text-lg font-semibold text-left rtl:text-right text-gray-900 bg-white dark:text-white dark:bg-gray-800"} "Elected:"]
     (into
      [:div {:class "px-5 grid grid-cols-1 gap-4 content-between"}]
      (map #(elected-candidate-display % vote-config) elected))]))


(defn vote-counts-header-row [count-ns]
  [:thead {:class styles/table-head}
   [:tr
    [:th {:class "px-2 py-4"} "Seat"]
    [:th {:class "px-2"} "Party"]
    [:th {:class "px-2"} "Candidate"]
    [:th {:class "px-2"}"Share"]
    (map (fn [n] [:th {:class "px-2"} (str "Count " (inc n))]) count-ns)]])



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

(defn party-icon [colour]
  [:span {:class "fas fa-circle pr-1"
          :style {:color (inputs/colour-styles colour)}}])

(defn vote-counts-data-row [candidate counts-data table-data vote-config candidate-shares seats elected]
  (let [{:keys [position]}                                  (candidate table-data)
        {:keys [candidate-party party-colours party-names]} vote-config
        party-id                                            (candidate candidate-party)
        party-name                                          (party-names party-id)
        party-colour                                        (party-colours party-id)]
    [:tr
     [:th {:class "px-2 py-2"} (when (some #{position} (range seats)) (inc position))]
     ;; [:td (merge {:class "px-2 py-2"} (inputs/get-colour-style party-colour)) party-name]
     [:td (party-icon party-colour) (str " " party-name)]
     [:th (merge {:class "px-2 py-2"}) (inputs/keyword->name candidate)]
     [:td {:class "px-2 py-2"} (str (candidate candidate-shares) " %")]
     (map (fn [n] (count-n-data-row candidate n counts-data table-data elected)) (keys counts-data))]))

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
         "Some more text here ... TODO"]
        [:p [:span {:class styles/table-caption-p} "Quota: "] quota]]


       [vote-counts-header-row count-ns]

       (into
        [:tbody {:class styles/table-body}]
        (map #(vote-counts-data-row % counts table-data vote-config vote-shares seats elected) candidate-order))])

     [:div {:class "text-sm px-4 py-6"}
      (when my-ballot?
        [:p [:span {:class my-ballot-icon}] " = My Ballot"])
      [:p [:span {:class elected-colours} "Elected"]]
      [:p [:span {:class eliminated-colours} "Eliminated"]]]]))
