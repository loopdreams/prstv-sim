(ns prstv-sim.views
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.subs :as subs]
   [prstv-sim.events :as events]
   [prstv-sim.vote-counter :as counter]
   [prstv-sim.inputs :as inputs]
   [prstv-sim.vote-generator :as votes]
   [prstv-sim.results-display :as results]
   [reagent.core :as reagent]))

(defn inputs-panel []
  [:div
   [inputs/party-input-table]
   [inputs/candidate-input-table]
   [inputs/set-vote-params]
   [inputs/preconfig-options-selector]])


(defn results-header [id]
  (reagent/create-class
   {:component-did-mount
    #(.scrollIntoView (.getElementById js/document id)
                      (js-obj "behavior" "smooth"))
    :reagent-render
    (fn [id]
      [:h1.title {:id id} "Results"])}))

(defn results-panel []
  (let [results @(re-frame/subscribe [::subs/results])]
    [:div
     [inputs/inputs->vote-config->results]
     (when results
       (let [canidate-first-prefs (:first-prefs results)
             elected              (:elected results)
             quota                (:quota (:c-data results))]
         [:div.box
          ;; [results-header "results"]
          [results/elected-display elected]
          ;; [results/first-prefs-table canidate-first-prefs]
          [results/party-first-prefs-table canidate-first-prefs]
          [:h2 [:span.has-text-weight-bold "Quota: "] quota]
          [results/vote-counts-table results]]))]))


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
          :component [results-panel]}
         {:key :about
          :label "About"
          :component [:div "About Page"]})])



;; TODO separate out header section
(defn main-panel []
  [:div {:style {:background-color (inputs/colour-styles "Purple")}}
   [:section.hero {:class "has-background-primary-white"}
    [:div.hero-body {:style {:background-color (inputs/colour-styles "Purple")}}
     [:p.title {:style {:color (inputs/colour-styles "White")}} "Single Transferrable Vote Simulator"]
     [:p.subtitle
      [:a {:href "about.html"
           :style {:text-decoration "none"
                   :color (inputs/colour-styles "White")}
           :target "_blank"} "About"]]]]
   [tab-pages]])
