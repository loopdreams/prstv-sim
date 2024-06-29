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
 (fn [db [_ form type]]
   (let [form-data    (form db)
         current-data (type (:inputs db))
         id           (if current-data
                        (-> (keys current-data)
                            sort last inc)
                        1)]
     (-> db
         (assoc-in [:inputs type id] form-data)
         (dissoc form)))))
