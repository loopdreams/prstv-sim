(ns prstv-sim.graphs
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [prstv-sim.subs :as subs]
            [prstv-sim.inputs :as inputs]
            [clojure.set :as set]))


(defn graph-spec-candidates [vals]
  {:$schema "https://vega.github.io/schema/vega-lite/v5.json"
   :data {:values vals}
   :mark "bar"
   :title "First Preference Votes"
   :width 600
   :height 400
   :encoding {:y {:field :name
                  :type "nominal"
                  :title "Candidate"
                  :sort "-x"}
              :x {:field :percent
                  :type "quantitative"
                  :title "Percentage"}
              :color {:field :party
                      :type :nominal
                      :title "Party"}}})




(defn graph-create-candidate-vals [{:keys [party-colours party-names candidate-party]} {:keys [first-prefs elected]}]
  (println first-prefs)
  (let [total-votes (reduce + (vals first-prefs))]
    (reduce (fn [results cand]
              (let [percent (int (* 100 (/ (cand first-prefs) total-votes)))
                    party (party-names (cand candidate-party))
                    colour (party-colours ((set/map-invert party-names) party))
                    name (inputs/keyword->name cand)
                    name (if (elected cand) (str  "** " name " **") name)]
                (conj results {:name name
                               :percent percent
                               :party party
                               :colour colour})))
            []
            (keys first-prefs))))



(defn chart-renderer [spec]
  (reagent/create-class
   {:reagent-render (fn [] [:div#vis])
    :component-did-mount (fn [_]
                           (js/vegaEmbed "#vis" (clj->js spec)))
    :component-did-update (fn [comp] (js/vegaEmbed "#vis" (clj->js (reagent/props comp))))}))

(defn chart-candidates []
  (let [results (re-frame/subscribe [::subs/results])
        config (re-frame/subscribe [::subs/vote-config])]
    (fn []
      (let [data (-> (graph-create-candidate-vals @config @results)
                     graph-spec-candidates)]
        [chart-renderer data]))))
