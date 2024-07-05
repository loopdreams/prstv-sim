(ns prstv-sim.events
  (:require
   [re-frame.core :as re-frame]
   [prstv-sim.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))


(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))


(re-frame/reg-event-db
 ::update-inputs
 (fn [db [_ k val]]
   (assoc-in db [:inputs k] val)))

(re-frame/reg-event-db
 ::delete-inputs
 (fn [db [_ k id]]
   (update-in db [:inputs k] dissoc id)))

(re-frame/reg-event-db
 ::update-form
 (fn [db [_ form k val]]
   (assoc-in db [form k] val)))


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
 ::add-vote-config
 (fn [db [_ config]]
   (assoc db :vote-config config)))

(re-frame/reg-event-db
 ::save-votes
 (fn [db [_ votes]]
   (assoc db :total-votes votes)))

(re-frame/reg-event-db
 ::add-results
 (fn [db [_ elected counts first-prefs counts-data]]
   (-> db
       (assoc-in [:results :elected] elected)
       (assoc-in [:results :counts] counts)
       (assoc-in [:results :first-prefs] first-prefs)
       (assoc-in [:results :c-data] counts-data))))
