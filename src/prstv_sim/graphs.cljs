(ns prstv-sim.graphs
  (:require
   ["chart.js" :refer [Chart]]
   [clojure.set :as set]
   [clojure.string :as str]
   [prstv-sim.inputs :as inputs]
   [prstv-sim.styles :as styles]
   [prstv-sim.subs :as subs]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]))

;; Processing functions

(defn vega-spec-data->chartjs [vega-data]
  (let [sorted  (reverse (sort-by :percent vega-data))
        vals    (map :percent sorted)
        labels  (map :name sorted)
        colours (map (comp styles/colour-styles :colour) sorted)
        quota   (:quota (first vega-data))]
    {:labels labels
     :datasets {:data vals
                :backgroundColor colours}
     :quota quota}))


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


(defn sankey-get-color [cfg key]
  (let [{:keys [party-colours candidate-party]} cfg
        og-keyword (-> (str/split key #":") first inputs/name->keyword)
        colour-name (-> og-keyword candidate-party party-colours)
        colour (or (styles/colour-styles colour-name) "#FFF")]
    colour))

;; Chart specs

(defn graph-spec-candidates [{:keys [labels datasets quota]}]
  {:type "bar"
   :data {:labels labels
          :datasets [{:data (:data datasets)
                      :backgroundColor (:backgroundColor datasets)
                      :borderWidth 1}]}
   :options
   {:plugins {:legend {:display false}
              :annotation
              {:annotations
               {:line1
                {:type "line"
                 :yMin quota
                 :yMax quota
                 :borderWidth 3
                 :borderColor "#0d9488"
                 :borderDash [10, 10]}
                :label1
                {:type "label"
                 :xValue (dec (count (:data datasets)))
                 :yValue (inc quota)
                 :content ["Quota"]
                 :color "#0d9488"}}}}
    :scales {:y
             {:title
              {:display true
               :text "Percentage (%)"}}}}})

(defn graph-spec-parties [{:keys [labels datasets]}]
  {:type "bar"
   :data {:labels labels
          :datasets [{:data (:data datasets)
                      :backgroundColor (:backgroundColor datasets)
                      :borderWidth 1}]}
   :options {:plugins {:legend {:display false}}
             :scales {:y
                      {:title
                       {:display true
                        :text "Percentage (%)"}}}}})


(defn graph-spec-sankey [cfg data]
  {:type "sankey"
   :data {:datasets [{:data data
                      :colorFrom (map (comp (partial sankey-get-color cfg) :from) data)
                      :colorTo (map (comp (partial sankey-get-color cfg) :to) data)}]}})




(defn reset-canvas! [id container-id]
  (let [target (.getElementById js/document id)
        target-container (.getElementById js/document container-id)]
    (do
      (. target remove)
      (.appendChild target-container
                    (doto (.createElement js/document "canvas")
                      (-> (.setAttribute "id" id)))))))

;; Candidates

(defn chart-candidates [spec]
  (let [canvas-id    "chart-candidates"
        container-id "chart-candidates-container"]
    (reagent/create-class
     {:reagent-render      (fn [] [:div {:id container-id} [:canvas {:id canvas-id}]])
      :component-did-mount (fn [_]
                             (let [ctx (.getContext (.getElementById js/document canvas-id) "2d")]
                               (Chart. ctx (clj->js spec))))
      :component-did-update
      (fn [comp]
        (reset-canvas! canvas-id container-id)
        (let [ctx (.getContext (.getElementById js/document canvas-id) "2d")]
          (Chart. ctx (clj->js (reagent/props comp)))))})))


(defn chart-candidates-wrapper []
  (let [results       (re-frame/subscribe [::subs/results])
        config        (re-frame/subscribe [::subs/vote-config])
        results-state @(re-frame/subscribe [::subs/results-loading?])]
    (if (= results-state :done)
      (fn []
        (let [data (-> (graph-create-candidate-vals @config @results)
                       (vega-spec-data->chartjs)
                       (graph-spec-candidates))]
          [:div
           [:h2 {:class "p-5 text-lg font-semibold text-left rtl:text-right text-gray-900 bg-white dark:text-white dark:bg-gray-800"} "Candidate First Preference Votes"]
           [chart-candidates data]]))
      [:div])))

;; Parties

(defn chart-parties [spec]
  (let [canvas-id    "chart-parties"
        container-id "chart-parties-container"]
    (reagent/create-class
     {:reagent-render      (fn [] [:div {:id container-id} [:canvas {:id canvas-id}]])
      :component-did-mount (fn [_]
                             (let [ctx (.getContext (.getElementById js/document canvas-id) "2d")]
                               (Chart. ctx (clj->js spec))))
      :component-did-update
      (fn [comp]
        (reset-canvas! canvas-id container-id)
        (let [ctx (.getContext (.getElementById js/document canvas-id) "2d")]
          (Chart. ctx (clj->js (reagent/props comp)))))})))



(defn chart-parties-wrapper []
  (let [results       (re-frame/subscribe [::subs/results])
        config        (re-frame/subscribe [::subs/vote-config])
        results-state @(re-frame/subscribe [::subs/results-loading?])]
    (if (= results-state :done)
      (fn []
        (let [data (-> (graph-create-party-vals @config @results)
                       (vega-spec-data->chartjs)
                       (graph-spec-parties))]
          [:div
           [:h2 {:class "p-5 text-lg font-semibold text-left rtl:text-right text-gray-900 bg-white dark:text-white dark:bg-gray-800"} "Party First Preference Votes"]
           [chart-parties data]]))
      [:div])))

;; Sankey Chart

(defn append-count-keyword [kw count]
  (str (inputs/keyword->name kw) ": Count " (inc count)))

(defn sankey-data-from [to-candidate from-data]
  (map (fn [[name vs]] {:from name :to to-candidate :flow vs}) from-data))

(defn sankey-data-to [from-candidate to-data]
  (map (fn [[name vs]] {:from from-candidate :to name :flow vs}) to-data))


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

#_(defn chart-sankey []
    (let [results         (re-frame/subscribe [::subs/results])
          vote-config     (re-frame/subscribe [::subs/vote-config])
          sankey-selector (re-frame/subscribe [::subs/sankey-selector])]
      (when @sankey-selector
        (fn []
          (let [[candidate count]  @sankey-selector
                c-data             (:c-data @results)
                cfg                @vote-config
                get-piles          (fn [n] (-> ((:counts c-data) n) :piles))
                cur-count          (get-piles count)
                next-count         (get-piles (inc count))
                prev-count         (get-piles (dec count))
                tracked-ballots    (cur-count candidate)
                gather-count-data  (fn [piles count]
                                     (reduce (fn [result ballot-id]
                                               (let [cand (-> (filter (fn [[_ ids]] (some #{ballot-id} ids)) piles)
                                                              ffirst
                                                              (append-count-keyword count))]
                                                 (update result cand (fnil inc 0))))
                                             {} tracked-ballots))
                prev-count-tracked (sankey-data-from (append-count-keyword candidate count) (gather-count-data prev-count (dec count)))
                next-count-tracked (sankey-data-to (append-count-keyword candidate count) (gather-count-data next-count (inc count)))
                spec               (graph-spec-sankey cfg (concat prev-count-tracked next-count-tracked))]
              (if candidate
                [sankey-chart-renderer spec]
                [:div]))))))

(defn chart-sankey []
  (let [results         (re-frame/subscribe [::subs/results])
        vote-config     (re-frame/subscribe [::subs/vote-config])
        sankey-selector (re-frame/subscribe [::subs/sankey-selector])]
    (when @sankey-selector
      (fn []
        (let [[candidate count] @sankey-selector
              c-data            (:c-data @results)
              last-count-n      (last (keys (:counts c-data)))
              cfg               @vote-config
              get-piles         (fn [n] (-> ((:counts c-data) n) :piles))
              cur-count         (get-piles count)
              tracked-ballots   (cur-count candidate)
              gather-count-data (fn [piles count]
                                  (reduce (fn [result ballot-id]
                                            (let [cand (-> (filter (fn [[_ ids]] (some #{ballot-id} ids)) piles)
                                                           ffirst
                                                           (append-count-keyword count))]
                                              (update result cand (fnil inc 0))))
                                          {} tracked-ballots))
              c-name            (append-count-keyword candidate count)
              spec              (->> (cond
                                       (zero? count)          (sankey-data-to c-name
                                                                              (gather-count-data
                                                                               (get-piles (inc count)) (inc count)))
                                       (= count last-count-n) (sankey-data-from c-name
                                                                                (gather-count-data
                                                                                 (get-piles (dec count)) (dec count)))
                                       :else
                                       (concat
                                        (sankey-data-to c-name (gather-count-data (get-piles (inc count)) (inc count)))
                                        (sankey-data-from c-name (gather-count-data (get-piles (dec count)) (dec count)))))
                                     (graph-spec-sankey cfg))]
          [sankey-chart-renderer spec])))))
