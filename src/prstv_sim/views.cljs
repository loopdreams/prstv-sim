(ns prstv-sim.views
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.subs :as subs]
   [prstv-sim.inputs :as inputs]
   [prstv-sim.results-display :as results]
   [reagent.core :as reagent]))

(defn inputs-panel []
  [:div
   [inputs/inputs->vote-config]
   [inputs/party-input-table]
   [inputs/candidate-input-table]
   [inputs/set-vote-params]
   [inputs/preconfig-options-selector]])


(defn spinner []
  [:div#spinner.lds-ring [:div] [:div] [:div] [:div]])

(defn results-display []
  [:div
   [inputs/generate-results-button]
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
        (into [:ul]
              (map (fn [{:keys [key label]}]
                     [:li {:class (when (= @active-tab key) "is-active")
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
   "tabs is-boxed"
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

(defn header-component []
  [:section.hero {:class "has-background-primary-white"}
    [:div.hero-body {:style {:background-color (inputs/colour-styles "Purple")}}
     [:p.title {:style {:color (inputs/colour-styles "White")}} "Single Transferrable Vote Simulator"]
     [:p.subtitle {:style {:color (inputs/colour-styles "White")}} "Subtitle..."]]])

;; TODO separate out header section
(defn main-panel []
  [:div {:style {:background-color (inputs/colour-styles "Purple")}}
   [header-component]
   [tab-pages]])
