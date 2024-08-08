(ns prstv-sim.views
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.subs :as subs]
   [prstv-sim.graphs :as graphs]
   [prstv-sim.inputs :as inputs]
   [prstv-sim.results-display :as results]
   [reagent.core :as reagent]))



(defn inputs-panel []
  [:div {:class "pt-5"}
   [inputs/inputs->vote-config]
   [inputs/party-table-form]
   [inputs/candidate-table-form]
   [inputs/set-vote-params]
   [inputs/preconfig-options-selector]])


(defn spinner []
  [:div#spinner.lds-ring [:div] [:div] [:div] [:div]])

(defn results-display []
  [:div
   [inputs/generate-results-button]
   [graphs/chart-candidates]
   (let [loading? @(re-frame/subscribe [::subs/results-loading?])]
     (case loading?
       :loading [:div.box [spinner]]
       :done (let [{:keys [first-prefs elected c-data] :as results} @(re-frame/subscribe [::subs/results])]
               [:div.box
                [results/elected-display elected]
                [results/party-first-prefs-table first-prefs]
                [:h2 [:span.has-text-weight-bold "Quota: "] (:quota c-data)]
                [results/vote-counts-table results]])
       [:div]))])


;; pattern taken from this useful guide - https://medium.com/@kirill.ishanov/using-containers-with-reagent-and-re-frame-ba88c481335d
(defn nav-tabs [menu-class tab-list]
  (let [active-tab (reagent/atom (:key (first tab-list)))]
    (fn []
      [:div.section.has-background-white
       [:div {:class menu-class}
        (into [:ul {:class "flex flex-wrap -mb-px"}]
              (map (fn [{:keys [key label]}]
                     [:li {:class "inline-block p-4 border-b-2 border-transparent rounded-t-lg hover:text-gray-600 hover:border-gray-300 dark:hover:text-gray-300"
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
          :component [inputs-panel]}
         {:key :my-ballot
          :label "My Ballot"
          :component [inputs/user-ballot-form]}
         {:key :results
          :label "Results"
          :component [results-display]}
         {:key :about
          :label "About"
          :component [:div "About Page"]})])

(defn header-panel []
  [:div
   [:h2 {:class "mb-4 text-4xl tracking-tight font-bold text-gray-900 dark:text-white"}
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
