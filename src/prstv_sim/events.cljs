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
            (assoc
             db/default-db
             :inputs v-configs/input-default)))

(re-frame/reg-event-db
 ::update-inputs
 (fn [db [_ k val]]
   (assoc-in db [:inputs k] val)))

(re-frame/reg-event-db
 ::load-input-config
 (fn [db [_ data]]
   (assoc db :inputs data)))

(re-frame/reg-event-db
 ::delete-inputs
 (fn [db [_ k id]]
   (update-in db [:inputs k] dissoc id)))

(re-frame/reg-event-db
 ::toggle-popularity-field-state
 (fn [db [_ id]]
   (let [state (get-in db [:inputs :popularity-field-state id])]
     (if state
       (update-in db [:inputs :popularity-field-state] dissoc id)
       (assoc-in db [:inputs :popularity-field-state id] :editing)))))

(re-frame/reg-event-db
 ::update-form
 (fn [db [_ form k val]]
   (assoc-in db [form k] val)))

(re-frame/reg-event-db
 ::update-popularity-input
 (fn [db [_ type id val]]
   (assoc-in db [:inputs type id :popularity] val)))

(re-frame/reg-event-db
 ::add-form
 (fn [db [_ form type party-id]]
   (let [form-data    (form db)
         form-data    (if party-id (assoc form-data :party-id party-id)
                          form-data)
         current-data (type (:inputs db))
         id           (if current-data
                        (-> (keys current-data)
                            sort last inc)
                        1)]
     (-> db
         (assoc-in [:inputs type id] form-data)
         (dissoc form)))))

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

(re-frame/reg-event-db
 ::add-vote-config
 (fn [db [_ config]]
   (-> db
       (assoc :vote-config config)
       (assoc :results nil)
       (assoc :processing-results nil))))

(re-frame/reg-event-db
 ::calculate-results
 (fn [db [_ vote-config candidates my-ballot ballot-id seats]]
   (let [ballots (votes/prstv-vote-generator vote-config)
         [elected counts first-prefs c-data] (counter/run-vote-counts candidates (merge ballots my-ballot) seats)]
     (-> db
         (assoc-in [:results :elected] elected)
         (assoc-in [:results :counts] counts)
         (assoc-in [:results :first-prefs] first-prefs)
         (assoc-in [:results :c-data] c-data)
         (assoc :marked-ballot ballot-id)
         (assoc :processing-results :done)))))

(re-frame/reg-event-fx
 ::process-results
 (fn [{db :db} [_ vote-config candidates my-ballot ballot-id seats]]
   {:dispatch ^:flush-dom [::calculate-results vote-config candidates my-ballot ballot-id seats]
    :db (assoc db :processing-results :loading)}))
