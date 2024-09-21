(ns prstv-sim.views
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.subs :as subs]
   [prstv-sim.graphs :as graphs]
   [prstv-sim.inputs :as inputs]
   [prstv-sim.about :as about]
   [prstv-sim.events :as events]
   [prstv-sim.results-display :as results]
   [reagent.core :as reagent]
   [prstv-sim.styles :as styles]))


(defn inputs-panel []
  [:div {:class "md:max-w-prose md:m-auto px-4 md:px-0"}
   [inputs/party-table-form]
   [inputs/candidate-table-form]
   [inputs/set-vote-params]
   [inputs/preconfig-options-selector]
   [inputs/inputs->vote-config]])

(defn results-panel []
  [:div
   [results/generate-results-button]

   (let [loading? @(re-frame/subscribe [::subs/results-loading?])]
     (case loading?
       :loading [styles/spinner]
       :done (let [{:keys [elected] :as results} @(re-frame/subscribe [::subs/results])]
               [:div {:class "flex flex-col space-y-14 md:px-32"}
                [:div {:class "flex flex-row flex-wrap"}
                 [graphs/chart-parties-wrapper]
                 [graphs/chart-candidates-wrapper]]
                [results/elected-display elected]
                [results/vote-counts-table results]
                [graphs/candidate-sankey]
                [graphs/all-candidates-sankey-toggle]
                [graphs/all-candidates-sankey-chart]])
       [:div]))])

;; pattern taken from this useful guide - https://medium.com/@kirill.ishanov/using-containers-with-reagent-and-re-frame-ba88c481335d
(defn nav-tabs [menu-class tab-list]
  (let [active-tab (reagent/atom (:key (first tab-list)))
        link-to-results-page (re-frame/subscribe [::subs/results-page-link-show])]
    (fn []
      [:div
       [:div {:class "bg-stone-100 dark:bg-slate-800"}
        [:div {:class "max-w-prose m-auto"}
         [:div {:class menu-class}
          (into [:ul {:class "flex flex-col md:flex-row -mb-px text-xs md:text-sm"}]
                (map (fn [{:keys [key label]}]
                       [:li {:class (if (= @active-tab key) styles/active-tab styles/inactive-tab)
                             :aria-current (when (= @active-tab key) "page")
                             :on-click #(reset! active-tab key)}
                        [:a label]])
                     tab-list))]]]
       ^{:key @active-tab}
       [:div {:class "py-6"}
        (->> tab-list
             (filter #(= @active-tab (:key %)))
             first
             :component)
        (when (and @link-to-results-page (= @active-tab :inputs))
          [:div {:class "flex justify-center"}
           [:button {:class styles/default-button
                     :on-click #(reset! active-tab :results)} "Go to Results Page"]])]])))

(defn tab-pages []
  [nav-tabs
   styles/tab-menu
   (list {:key :inputs
          :label "Configure Vote"
          :component [inputs-panel]}
         {:key :my-ballot
          :label "My Ballot"
          :component [inputs/user-ballot-form]}
         {:key :results
          :label "Results"
          :component [results-panel]}
         {:key :about
          :label "About"
          :component [about/about]})])

(defn header-panel []
  [:div {:class "md:max-w-prose md:m-auto bg-stone-100 dark:bg-slate-800 border-b-2 md:border-none"}
   [:h2 {:class "mb-4 text-3xl md:text-4xl tracking-tight font-raleway font-bold text-gray-900 bg-gradient-to-r from-teal-600 via-teal-800 to-teal-600 dark:from-slate-200 dark:to-gray-300 inline-block text-transparent bg-clip-text"}
    "Single Transferrable Vote Simulator"]
   [:p {:class "pb-4 text-xs md:text-sm font-light dark:text-gray-100"} "A simulator for STV vote generation and counting"]])

(defn main-panel []
  [:div
   [:div {:class "bg-stone-100 dark:bg-slate-800 pt-8"}
    [header-panel]]
   [tab-pages]])
