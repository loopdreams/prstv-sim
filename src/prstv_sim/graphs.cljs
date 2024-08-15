(ns prstv-sim.graphs
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [prstv-sim.subs :as subs]
            [prstv-sim.inputs :as inputs]
            [clojure.set :as set]
            ["chart.js" :refer (Chart)]
            ["chartjs-chart-sankey" :as sankey :refer (SankeyController Flow)]
            ["chartjs-plugin-annotation" :as annotationPlugin]
            [prstv-sim.styles :as styles]))

;; (. chartjs/Chart (register sankey))
;; (. chartjs/Chart (register annotationPlugin))

;; (def Chart (.-Chart (js/require "chart.js")))

;; (def SankeyController (.-SankeyController (js/require "chartjs-chart-sankey")))




;; ;; TODO make font size dynamic
;; (defn graph-spec-candidates-vega [vals colours scale]
;;   (println vals)
;;   {:$schema "https://vega.github.io/schema/vega-lite/v5.json"
;;    :data {:values vals}
;;    :width "container"
;;    :height 400
;;    :config {:axis {:titleFontSize scale
;;                    :labelFontSize scale}
;;             :legend {:titleFontSize scale
;;                      :labelFontSize scale}}
;;    :title nil
;;    :layer [{:mark {:type "bar"
;;                    :tooltip true}
;;             :encoding {:x
;;                        {:field :name
;;                         :type "nominal"
;;                         :title nil
;;                         :sort "-y"
;;                         :axis
;;                         {:labelAngle 0
;;                          :ticks false}}
;;                        :y
;;                        {:field :percent
;;                         :type "quantitative"
;;                         :title "Percentage %"}
;;                        :color
;;                        {:field :party
;;                         :type :nominal
;;                         :title "Party"
;;                         :scale {:range colours}}}}
;;            {:mark
;;             {:type "rule"
;;              :color "#14b8a6"
;;              :strokeWidth 4
;;              :strokeDash [8,8]}
;;             :encoding {:y
;;                        {:field :quota
;;                         :type :quantitative}}}]})

;; (defn graph-spec-parties-vega [vals colours]
;;   {:$schema "https://vega.github.io/schema/vega-lite/v5.json"
;;    :data {:values vals}
;;    :width "container"
;;    :config {:axis {:titleFontSize 14
;;                    :labelFontSize 14}
;;             :legend {:titleFontSize 14
;;                      :labelFontSize 14}}
;;    :title nil
;;    :mark {:type "bar"
;;           :tooltip true}
;;    :encoding {:x
;;               {:field :name
;;                :type "nominal"
;;                :title nil
;;                :axis
;;                {:labelAngle 0
;;                 :ticks false}}
;;               :y
;;               {:field :percent
;;                :type "quantitative"
;;                :title "Percentage %"}
;;               :color
;;               {:field :name
;;                :title nil
;;                :type :nominal
;;                :scale {:range colours}}}})

(defn vega-spec-data->chartjs [vega-data]
  (let [sorted  (reverse (sort-by :percent vega-data))
        vals    (map :percent sorted)
        labels  (map :name sorted)
        colours (map (comp styles/colour-styles :colour) sorted)]
    {:labels labels
     :datasets {:data vals
                :backgroundColor colours}}))

(defn graph-spec-candidates-chartjs [{:keys [labels datasets]}]
  {:type "bar"
   :data {:labels labels
          :datasets [{:data (:data datasets)
                      :backgroundColor (:backgroundColor datasets)
                      :borderWidth 1}]}
   :options
   {:plugins {:annotation
              {:annotations
               {:line1
                {:type "line"
                 :yMin 16
                 :yMax 16}}}}}})

(defn graph-spec-parties-chartjs [{:keys [labels datasets]}]
  {:type "bar"
   :data {:labels labels
          :datasets [{:data (:data datasets)
                      :backgroundColor (:backgroundColor datasets)
                      :borderWidth 1}]}
   :options {:plugins {:legend {:display false}}}})

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

(defn graph-create-party-vals [{:keys [party-names candidate-party party-colours]} {:keys [first-prefs]}]
  (let [total-votes (reduce + (vals first-prefs))
        party-counts (reduce (fn [counts candidate]
                               (let [c-votes (candidate first-prefs)
                                     c-party (-> candidate candidate-party party-names)]
                                 (update counts c-party
                                         (fnil + 0) c-votes)))
                             {}
                             (keys first-prefs))]
    (reduce (fn [spec [party votes]]
              (let [percent (* 100 (/ votes total-votes))
                    colour (party-colours ((set/map-invert party-names) party))]
                (conj spec {:name party
                            :percent percent
                            :colour colour})))
            []
            party-counts)))


(defn candidate-chart-legend []
  [:div {:class "flex justify-center items-center pt-5"}
   [:div
    [:p "★ Elected"]
    [:p [:span {:class "font-extrabold text-teal-500"} "- - - -"] " Quota"]]])


#_(defn chart-renderer-parties [spec]
    (reagent/create-class
     {:reagent-render (fn [] [:div
                              [:h2 {:class styles/table-caption} "Parties - First Preference Votes"]
                              [:div#visParties {:class "overflow-x-auto w-full"}]])
      :component-did-mount (fn [_]
                             (js/vegaEmbed "#visParties" (clj->js spec)))
      :component-did-update (fn [comp] (js/vegaEmbed "#visParties" (clj->js (reagent/props comp))))}))

#_(defn chart-renderer-candidates [spec]
    (reagent/create-class
     {:reagent-render (fn [] [:div
                              [:h2 {:class styles/table-caption} "Candidates - First Preference Votes"]
                              [:div#visCand {:class "overflow-x-auto w-full"}]
                              [candidate-chart-legend]])
      :component-did-mount (fn [_]
                             (js/vegaEmbed "#visCand" (clj->js spec)))
      :component-did-update (fn [comp] (js/vegaEmbed "#visCand" (clj->js (reagent/props comp))))}))


(defn reset-canvas! [id container-id]
  (let [target (.getElementById js/document id)
        target-container (.getElementById js/document container-id)]
    (do
      (. target remove)
      (.appendChild target-container
                    (doto (.createElement js/document "canvas")
                      (-> (.setAttribute "id" id)))))))


(defn chart-js-candidates [spec]
  (let [canvas-id "chart-candidates"
        container-id "chart-candidates-container"]
    (reagent/create-class
     {:reagent-render (fn [] [:div {:id container-id} [:canvas {:id canvas-id}]])
      :component-did-mount (fn [_]
                             (let [ctx (.getContext (.getElementById js/document canvas-id) "2d")]
                               (Chart. ctx (clj->js spec))))
      :component-did-update
      (fn [comp]
        (reset-canvas! canvas-id container-id)
        (let [ctx (.getContext (.getElementById js/document canvas-id) "2d")]
          (Chart. ctx (clj->js (reagent/props comp)))))})))

(defn chart-js-parties [spec]
  (let [canvas-id "chart-parties"
        container-id "chart-parties-container"]
    (reagent/create-class
     {:reagent-render (fn [] [:div {:id container-id} [:canvas {:id canvas-id}]])
      :component-did-mount (fn [_]
                             (let [ctx (.getContext (.getElementById js/document canvas-id) "2d")]
                               (Chart. ctx (clj->js spec))))
      :component-did-update
      (fn [comp]
        (reset-canvas! canvas-id container-id)
        (let [ctx (.getContext (.getElementById js/document canvas-id) "2d")]
          (Chart. ctx (clj->js (reagent/props comp)))))})))


(defn chart-js-candidates-wrapper []
  (let [results (re-frame/subscribe [::subs/results])
        config (re-frame/subscribe [::subs/vote-config])
        results-state @(re-frame/subscribe [::subs/results-loading?])]
    (if (= results-state :done)
      (fn []
        (let [data (-> (graph-create-candidate-vals @config @results)
                       (vega-spec-data->chartjs)
                       (graph-spec-candidates-chartjs))]
          [chart-js-candidates data]))
      [:div])))

(defn chart-js-parties-wrapper []
  (let [results (re-frame/subscribe [::subs/results])
        config (re-frame/subscribe [::subs/vote-config])
        results-state @(re-frame/subscribe [::subs/results-loading?])]
    (if (= results-state :done)
      (fn []
        (let [data (-> (graph-create-party-vals @config @results)
                       (vega-spec-data->chartjs)
                       (graph-spec-parties-chartjs))]
          [chart-js-parties data]))
      [:div])))



(defn order-party-colours-for-chart [{:keys [party-names party-colours]}]
  (let [order (keys (sort-by val party-names))]
    (for [id order]
      (styles/colour-styles (party-colours id)))))

#_(defn chart-candidates []
    (let [results (re-frame/subscribe [::subs/results])
          config (re-frame/subscribe [::subs/vote-config])
          results-state @(re-frame/subscribe [::subs/results-loading?])
          graph-colours (order-party-colours-for-chart @config)]
      (if (= results-state :done)
        (fn []
          (let [scale (if (< (.. js/document -documentElement -clientWidth) 800)
                        8 14)
                data (-> (graph-create-candidate-vals @config @results)
                         (graph-spec-candidates-vega graph-colours scale))]
            [chart-renderer-candidates data]))
        [:div])))


#_(defn chart-parties []
    (let [results (re-frame/subscribe [::subs/results])
          config (re-frame/subscribe [::subs/vote-config])
          results-state @(re-frame/subscribe [::subs/results-loading?])
          graph-colours (order-party-colours-for-chart @config)]
      (if (= results-state :done)
        (let [data (-> (graph-create-party-vals @config @results)
                       (graph-spec-parties-vega graph-colours))]
          [chart-renderer-parties data])
        [:div])))


(defn append-count-keyword [kw count]
  (keyword (str (name kw) "-" count)))

(defn sankey-data-from [to-candidate from-data]
  (map (fn [[name vs]] {:from name :to to-candidate :flow vs}) from-data))

(defn sankey-data-to [from-candidate to-data]
  (map (fn [[name vs]] {:from from-candidate :to name :flow vs}) to-data))

(defn sankey-chart-structure [data]
  {:type "sankey"
   :data {:datasets [{:data data}]}})


(defn sankey-chart-renderer [spec]
  (reagent/create-class
   {:reagent-render (fn [] [:div {:id "sankey-chart-container"} [:canvas {:id "sankey-chart"}]])
    :component-did-mount (fn [_]
                           (let [ctx (.getContext (.getElementById js/document "sankey-chart") "2d")]
                             (Chart. ctx (clj->js spec))))
    :component-did-update
    (fn [comp]
      (reset-canvas! "sankey-chart" "sankey-chart-container")
      (let [ctx (.getContext (.getElementById js/document "sankey-chart") "2d")]
        (Chart. ctx (clj->js (reagent/props comp)))))}))




(defn chartjs-sankey []
  (let [results         (re-frame/subscribe [::subs/results])
        sankey-selector (re-frame/subscribe [::subs/sankey-selector])]
    (when @sankey-selector
      (fn []
        (let [[candidate count] @sankey-selector
              c-data              (:c-data @results)
              get-piles           (fn [n] (-> ((:counts c-data) n) :piles))
              cur-count           (get-piles count)
              next-count          (get-piles (inc count))
              prev-count          (get-piles (dec count))
              tracked-ballots     (cur-count candidate)
              gather-count-data   (fn [piles count]
                                    (reduce (fn [result ballot-id]
                                              (let [cand (-> (filter (fn [[_ ids]] (some #{ballot-id} ids)) piles)
                                                             ffirst
                                                             (append-count-keyword count))]
                                                (update result cand (fnil inc 0))))
                                            {} tracked-ballots))
                prev-count-tracked  (sankey-data-from (append-count-keyword candidate count) (gather-count-data prev-count (dec count)))
                next-count-tracked  (sankey-data-to (append-count-keyword candidate count) (gather-count-data next-count (inc count)))
                spec                (sankey-chart-structure (concat prev-count-tracked next-count-tracked))]
            (if candidate
              [sankey-chart-renderer spec]
              [:div]))))))


