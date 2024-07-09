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




(defn results-header [id]
  (reagent/create-class
   {:component-did-mount
    #(.scrollIntoView (.getElementById js/document id)
                      (js-obj "behavior" "smooth"))
    :reagent-render
    (fn [id]
      [:h1.title {:id id} "Results"])}))

(defn main-panel []
  (let [results @(re-frame/subscribe [::subs/results])]
    [:div {:style {:background-color (inputs/colour-styles "Purple")}}
     [:section.hero {:class "has-background-primary-white"}
      [:div.hero-body {:style {:background-color (inputs/colour-styles "Purple")}}
       [:p.title {:style {:color (inputs/colour-styles "White")}} "Single Transferrable Vote Simulator"]
       [:p.subtitle
        [:a {:href "about.html"
             :style {:text-decoration "none"
                     :color (inputs/colour-styles "White")}
             :target "_blank"} "About"]]]]
     [:div.section
      [inputs/party-input-table]
      [inputs/candidate-input-table]
      [inputs/set-vote-params]
      [inputs/user-ballot-form]
      [:div.box
       [inputs/inputs->vote-config->results]]
      (when results
        (let [canidate-first-prefs (:first-prefs results)
              elected              (:elected results)
              quota                (:quota (:c-data results))]
          [:div.box
           [results-header "results"]
           [results/elected-display elected]
           ;; [results/first-prefs-table canidate-first-prefs]
           [results/party-first-prefs-table canidate-first-prefs]
           [:h2 [:span.has-text-weight-bold "Quota: "] quota]
           [results/vote-counts-table results]]))]]))
