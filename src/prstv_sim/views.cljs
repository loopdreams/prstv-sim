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

(defn main-views-wrapper [comp]
  [:div {:class "py-6"} comp])

(defn inputs-panel []
  [:div
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
               [:div {:class "flex flex-col space-y-14"}
                [results/elected-display elected]
                [:div {:class "flex flex-row flex-wrap"}
                 [graphs/chart-parties-wrapper]
                 [graphs/chart-candidates-wrapper]]
                [results/vote-counts-table results]
                [graphs/candidate-sankey]
                [graphs/all-candidates-sankey-toggle]
                [graphs/all-candidates-sankey-chart]])
       [:div]))])


;; pattern taken from this useful guide - https://medium.com/@kirill.ishanov/using-containers-with-reagent-and-re-frame-ba88c481335d
(defn nav-tabs [menu-class tab-list]
  (let [active-tab (reagent/atom (:key (first tab-list)))
        display-tabs? (re-frame/subscribe [::subs/display-tabs?])]
    (fn []
      [:div
       [:div
        (if @display-tabs?
          [:div {:class menu-class}
           [:div {:class "flex text-lg md:hidden"}
            [:button {:class "fas fa-times"
                      :on-click #(re-frame/dispatch [::events/toggle-nav-menu])}]]
           (into [:ul {:class "flex flex-col md:flex-row -mb-px text-xs md:text-sm"}]
                 (map (fn [{:keys [key label]}]
                        [:li {:class (if (= @active-tab key) styles/active-tab styles/inactive-tab)
                              :aria-current (when (= @active-tab key) "page")
                              :on-click #(reset! active-tab key)}
                         [:a
                          label]])
                      tab-list))]
          [:div {:class "dark:text-white text-2xl"}
           [:button {:class "fas fa-bars"
                     :on-click #(re-frame/dispatch [::events/toggle-nav-menu])}]])]
       ^{:key @active-tab}
       [:div (->> tab-list
                  (filter #(= @active-tab (:key %)))
                  first
                  :component)]])))

(defn tab-pages []
  [nav-tabs
   "text-sm font-medium text-center text-gray-500 border-b border-gray-200 dark:text-gray-400 dark:border-gray-700"
   (list {:key :inputs
          :label "Configure Vote"
          :component [main-views-wrapper [inputs-panel]]}
         {:key :my-ballot
          :label "My Ballot"
          :component [main-views-wrapper [inputs/user-ballot-form]]}
         {:key :results
          :label "Results"
          :component [main-views-wrapper [results-panel]]}
         {:key :about
          :label "About"
          :component [main-views-wrapper [about/about]]})])

(defn header-panel []
  [:div
   [:h2 {:class "mb-4 text-xl md:text-4xl tracking-tight font-sans font-bold text-gray-900 dark:text-white"}
    "Single Transferrable Vote Simulator"]
   [:p {:class "mb-4 text-xs md:text-lg font-light dark:text-gray-100"} "A simulator for STV vote generation and counting"]])

;; TODO separate out header section
(defn main-panel []
  [:div {:class " py-8 px-4 mx-auto max-w-screen-xl lg:py-16 lg:px-6"}
    [header-panel]
    [tab-pages]])
