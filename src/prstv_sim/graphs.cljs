(ns prstv-sim.graphs
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [prstv-sim.subs :as subs]
            [prstv-sim.inputs :as inputs]
            [clojure.set :as set]
            [prstv-sim.styles :as styles]))


;; TODO make font size dynamic
;; TODO add total votes into tooltip
(defn graph-spec-candidates [vals colours]
  {:$schema "https://vega.github.io/schema/vega-lite/v5.json"
   :data {:values vals}
   :width "container"
   :config {:axis {:titleFontSize 14
                   :labelFontSize 14}
            :legend {:titleFontSize 14
                     :labelFontSize 14}}
   :title nil
   :layer [{:mark {:type "bar"
                   :tooltip true}
            :encoding {:x
                       {:field :name
                        :type "nominal"
                        :title nil
                        :sort "-y"
                        :axis
                        {:labelAngle 0
                         :ticks false}}
                       :y
                       {:field :percent
                        :type "quantitative"
                        :title "Percentage %"}
                       :color
                       {:field :party
                        :type :nominal
                        :title "Party"
                        :scale {:range colours}}}}
           {:mark
            {:type "rule"
             :color "#14b8a6"
             :strokeWidth 4
             :strokeDash [8,8]}
            :encoding {:y
                       {:field :quota
                        :type :quantitative}}}]})

(defn graph-spec-parties [vals colours]
  {:$schema "https://vega.github.io/schema/vega-lite/v5.json"
   :data {:values vals}
   :width "container"
   :config {:axis {:titleFontSize 14
                   :labelFontSize 14}
            :legend {:titleFontSize 14
                     :labelFontSize 14}}
   :title nil
   :mark {:type "bar"
          :tooltip true}
   :encoding {:x
               {:field :name
                :type "nominal"
                :title nil
                :axis
                {:labelAngle 0
                 :ticks false}}
               :y
               {:field :percent
                :type "quantitative"
                :title "Percentage %"}
               :color
               {:field :name
                :title nil
                :type :nominal
                :scale {:range colours}}}})


(defn graph-create-candidate-vals [{:keys [party-colours party-names candidate-party]} {:keys [first-prefs elected c-data]}]
  (let [quota (:quota c-data)
        total-votes (reduce + (vals first-prefs))
        quota-percent (* 100 (/ quota total-votes))]
    (reduce (fn [results cand]
              (let [percent (* 100 (/ (cand first-prefs) total-votes))
                    party (party-names (cand candidate-party))
                    colour (party-colours ((set/map-invert party-names) party))
                    name (inputs/keyword->name cand)
                    name (if (elected cand) (str  "★ " name) name)]
                (conj results {:name name
                               :percent percent
                               :party party
                               :colour colour
                               :quota quota-percent})))
            []
            (keys first-prefs))))

(defn graph-create-party-vals [{:keys [party-names candidate-party]} {:keys [first-prefs]}]
  (let [total-votes (reduce + (vals first-prefs))
        party-counts (reduce (fn [counts candidate]
                               (let [c-votes (candidate first-prefs)
                                     c-party (-> candidate candidate-party party-names)]
                                 (update counts c-party
                                         (fnil + 0) c-votes)))
                             {}
                             (keys first-prefs))]
    (reduce (fn [spec [party votes]]
              (let [percent (* 100 (/ votes total-votes))]
                (conj spec {:name party
                            :percent percent})))
            []
            party-counts)))


(defn candidate-chart-legend []
  [:div {:class "flex justify-center items-center pt-5"}
   [:div
    [:p "★ Elected"]
    [:p [:span {:class "font-extrabold text-teal-500"} "- - - -"] " Quota"]]])


(defn chart-renderer-parties [spec]
  (reagent/create-class
   {:reagent-render (fn [] [:div
                            [:h2 {:class styles/table-caption} "Parties - First Preference Votes"]
                            [:div#visParties {:class "overflow-x-auto w-full"}]])
    :component-did-mount (fn [_]
                           (js/vegaEmbed "#visParties" (clj->js spec)))
    :component-did-update (fn [comp] (js/vegaEmbed "#visParties" (clj->js (reagent/props comp))))}))

(defn chart-renderer-candidates [spec]
  (reagent/create-class
   {:reagent-render (fn [] [:div
                            [:h2 {:class styles/table-caption} "Candidates - First Preference Votes"]
                            [:div#visCand {:class "overflow-x-auto w-full"}]
                            [candidate-chart-legend]])
    :component-did-mount (fn [_]
                           (js/vegaEmbed "#visCand" (clj->js spec)))
    :component-did-update (fn [comp] (js/vegaEmbed "#visCand" (clj->js (reagent/props comp))))}))


(defn order-party-colours-for-chart [{:keys [party-names party-colours]}]
  (let [order (keys (sort-by val party-names))]
    (for [id order]
      (inputs/colour-styles (party-colours id)))))

(defn chart-candidates []
  (let [results (re-frame/subscribe [::subs/results])
        config (re-frame/subscribe [::subs/vote-config])
        results-state @(re-frame/subscribe [::subs/results-loading?])
        graph-colours (order-party-colours-for-chart @config)]
    (if (= results-state :done)
      (fn []
        (let [data (-> (graph-create-candidate-vals @config @results)
                       (graph-spec-candidates graph-colours))]
          [chart-renderer-candidates data]))
      [:div])))

(defn chart-parties []
  (let [results (re-frame/subscribe [::subs/results])
        config (re-frame/subscribe [::subs/vote-config])
        results-state @(re-frame/subscribe [::subs/results-loading?])
        graph-colours (order-party-colours-for-chart @config)]
    (if (= results-state :done)
      (let [data (-> (graph-create-party-vals @config @results)
                     (graph-spec-parties graph-colours))]
        (println data)
        [chart-renderer-parties data])
      [:div])))
