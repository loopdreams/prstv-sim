(ns prstv-sim.views
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.subs :as subs]
   [prstv-sim.graphs :as graphs]
   [prstv-sim.inputs :as inputs]
   [prstv-sim.results-display :as results]
   [reagent.core :as reagent]
   [prstv-sim.styles :as styles]))



(defn inputs-panel []
  [:div
   [inputs/party-table-form]
   [inputs/candidate-table-form]
   [inputs/set-vote-params]
   [inputs/preconfig-options-selector]
   [inputs/inputs->vote-config]])

(defn main-views-wrapper [comp]
  [:div {:class "py-6"} comp])

(defn results-display []
  [:div
   [results/generate-results-button]

   (let [loading? @(re-frame/subscribe [::subs/results-loading?])]
     (case loading?
       :loading [styles/spinner]
       :done (let [{:keys [elected c-data] :as results} @(re-frame/subscribe [::subs/results])]
               [:div
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
  (let [active-tab (reagent/atom (:key (first tab-list)))]
    (fn []
      [:div
       [:div {:class menu-class}
        (into [:ul {:class "flex flex-wrap -mb-px"}]
              (map (fn [{:keys [key label]}]
                     [:li {:class (if (= @active-tab key) styles/active-tab styles/inactive-tab)
                           :aria-current (when (= @active-tab key) "page")
                           :on-click #(reset! active-tab key)}
                      [:a
                       label]])
                   tab-list))]
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
          :component [main-views-wrapper [results-display]]}
         {:key :about
          :label "About"
          :component [main-views-wrapper [:div "About Page TODO"]]})])

(defn header-panel []
  [:div
   [:h2 {:class "mb-4 text-4xl tracking-tight font-sans font-bold text-gray-900 dark:text-white"}
    "Single Transferrable Vote Simulator"]
   [:p {:class "mb-4 font-light"} "Subtitle..."]])

;; TODO separate out header section
(defn main-panel []
  [:section {:class "bg-white dark:bg-gray-900"}
   [:div {:class "py-8 px-4 mx-auto max-w-screen-xl lg:py-16 lg:px-6"}
    [:div {:class "max-w-screen-lg text-gray-500 sm:text-lg dark:text-gray-400"}]
    ;; :style {:background-color (inputs/colour-styles "Purple")}}
    [header-panel]
    [tab-pages]]])
