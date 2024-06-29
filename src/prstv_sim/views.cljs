(ns prstv-sim.views
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.subs :as subs]
   [prstv-sim.events :as events]
   [prstv-sim.inputs :as inputs]))





(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div.section
     [:div {:class "has-background-primary-white"}
      [:h1 {:class "title has-text-light"}
       "Hello you, from " @name]]
     [:div
      [inputs/set-number-of-votes]
      [inputs/party-input-table]
      [inputs/candidate-input-table]]]))
