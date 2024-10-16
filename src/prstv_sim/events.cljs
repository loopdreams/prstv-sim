(ns prstv-sim.events
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.vote-counter :as counter]
   [prstv-sim.vote-generator :as votes]
   [prstv-sim.sample-configs :as v-configs]
   [prstv-sim.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            (-> db/default-db
                (assoc :inputs v-configs/input-default)
                (assoc :pre-config "Default"))))

(re-frame/reg-event-db
 ::update-inputs
 (fn [db [_ k val]]
   (assoc-in db [:inputs k] val)))

(re-frame/reg-event-db
 ::load-input-config
 (fn [db [_ data name]]
   (-> db
       (assoc :inputs data)
       (assoc :pre-config name))))

(re-frame/reg-event-db
 ::delete-inputs
 (fn [db [_ k id]]
   (update-in db [:inputs k] dissoc id)))

(re-frame/reg-event-db
 ::update-table-field
 (fn [db [_ table row field new-val party-id]]
   (if party-id
     (let [parties (-> db :inputs :party)
           pid (->> parties
                    (filter (fn [[_ {:keys [name]}]] (= name new-val)))
                    ffirst)]
       (assoc-in db [:inputs table row :party-id] pid))
     (assoc-in db [:inputs table row field] new-val))))

(re-frame/reg-event-db
 ::add-blank-table-row
 (fn [db [_ table]]
   (let [last-id  (-> db :inputs table keys sort last)]
     (println last-id)
     (if (= table :party)
       (assoc-in db [:inputs table (inc last-id)]
                 {:colour "White"})
       (assoc-in db [:inputs table (inc last-id)]
                 {})))))


(re-frame/reg-event-db
 ::activate-my-ballot
 (fn [db]
   (let [n-candidates (count (:candidate (:inputs db)))]
     (-> db
         (assoc :available-preferences (into #{} (map inc (range n-candidates))))
         (assoc :my-ballot? true)))))

(re-frame/reg-event-db
 ::update-my-ballot
 (fn [db [_ cand-name preference]]
   (let [updated-db-ballot  (assoc-in db [:my-ballot cand-name] preference)
         active-preferences (map parse-long (vals (:my-ballot updated-db-ballot)))
         n-candidates       (count (:candidate (:inputs db)))
         available-prefs    (apply (partial disj (into #{} (map inc (range n-candidates)))) active-preferences)]
     (-> updated-db-ballot
         (assoc :available-preferences available-prefs)))))

(re-frame/reg-fx
 :config-confirmation-msg
 (fn [_]
   (let [el (js/document.getElementById "config-added-confirmation")]
     (js/setTimeout
      (fn []
        (set! (.-display (.-style el)) "none"))
      2000)
     (set! (.-display (.-style el)) "block"))))

(re-frame/reg-event-fx
 ::add-vote-config-fx
 (fn [{db :db} [_ config]]
   {:db (-> db
            (assoc :vote-config config)
            (assoc :results nil)
            (assoc :processing-results nil)
            (assoc :my-ballot? nil)
            (assoc :my-ballot nil)
            (assoc :sankey-selector nil)
            (assoc :sankey-show? false)
            (assoc :results-page-link-show true))
    :config-confirmation-msg nil}))

(re-frame/reg-event-db
 ::calculate-results
 (fn [db [_ vote-config candidates my-ballot ballot-id seats]]
   (let [ballots (votes/prstv-vote-generator vote-config)
         [elected c-data] (counter/run-vote-counts candidates (merge ballots my-ballot) seats)
         first-prefs (:counts ((:counts c-data) 0))]
     (-> db
         (assoc-in [:results :elected] elected)
         (assoc-in [:results :first-prefs] first-prefs)
         (assoc-in [:results :c-data] c-data)
         (assoc :marked-ballot ballot-id)
         (assoc :processing-results :done)))))

(re-frame/reg-event-fx
 ::process-results
 (fn [{db :db} [_ vote-config candidates my-ballot ballot-id seats]]
   {:dispatch ^:flush-dom [::calculate-results vote-config candidates my-ballot ballot-id seats]
    :db (-> db
            (assoc :processing-results :loading)
            (assoc :sankey-show? false)
            (assoc :sankey-selector nil))}))


(re-frame/reg-event-db
 ::sankey-show-toggle
 (fn [db]
   (let [cur (:sankey-show? db)]
     (-> db
         (assoc :sankey-show? (if cur false true))
         (assoc :processing-sankey-chart :done)))))

(re-frame/reg-event-fx
 ::process-sankey-chart
 (fn [{db :db} _]
   {:dispatch ^:flush-dom [::sankey-show-toggle]
    :db (assoc db :processing-sankey-chart :loading)}))

(re-frame/reg-event-db
 ::sankey-selector
 (fn [db [_ cand]]
   (assoc db :sankey-selector cand)))

(re-frame/reg-event-db
 ::toggle-nav-menu
 (fn [db]
   (let [cur (:display-tabs? db)]
     (assoc db :display-tabs? (if cur false true)))))
