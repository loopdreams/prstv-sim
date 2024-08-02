(ns prstv-sim.subs
  (:require
   [re-frame.core :as re-frame]))


(re-frame/reg-sub
 ::party-form
 (fn [db]
   (:party-form db)))

(re-frame/reg-sub
 ::candidate-form
 (fn [db]
   (:candidate-form db)))

(re-frame/reg-sub
 ::form-valid?
 (fn [db [_ type]]
   (let [form                      (type db)
         {:keys [name popularity]} form]
     (and name
          (if (= type :party-form) (:colour form) (:party form))
          (or (not popularity)
              (and
               (every? #(re-find #"\d" %) popularity)
               (< popularity 101)))))))

(re-frame/reg-sub
 ::inputs
 (fn [db [_ k id]]
   (get-in db (if id [:inputs k id] [:inputs k]))))

(re-frame/reg-sub
 ::all-inputs
 (fn [db]
   (:inputs db)))

(re-frame/reg-sub
 ::active-party-ids
 :<- [::all-inputs]
 (fn [all-inputs]
   (let [cs (:candidate all-inputs)]
     (->> cs
          vals
          (map :party-id)
          (into #{})))))

(re-frame/reg-sub
 ::popularity-field-state
 (fn [db [_ id]]
   (get-in db [:inputs :popularity-field-state id])))

(re-frame/reg-sub
 ::vote-config
 (fn [db]
   (:vote-config db)))

(re-frame/reg-sub
 ::marked-ballot
 (fn [db]
   (:marked-ballot db)))

(re-frame/reg-sub
 ::results-loading?
 (fn [db]
   (:processing-results db)))

(re-frame/reg-sub
 ::my-ballot?
 (fn [db]
   (:my-ballot? db)))

(re-frame/reg-sub
 ::my-ballot
 (fn [db]
     (:my-ballot db)))

(re-frame/reg-sub
 ::available-preferences
 (fn [db]
   (:available-preferences db)))

(re-frame/reg-sub
 ::party-list
 (fn [db]
   (let [parties (-> db :inputs :party)]
     (for [p parties
           :let [[_ {:keys [name]}] p]]
       name))))



(re-frame/reg-sub
 ::party-id
 (fn [db [_ party-name]]
   (let [parties (-> db :inputs :party)]
     (-> (select-keys parties
                      (for [[k {:keys [name]}] parties :when (= name party-name)] k))
         ffirst))))

(re-frame/reg-sub
 ::total-votes
 (fn [db]
   (:total-votes db)))

(re-frame/reg-sub
 ::chart-data
 (fn [db]
   (:chart-data db)))

(re-frame/reg-sub
 ::results
 (fn [db]
   (:results db)))

(re-frame/reg-sub
 ::spinner
 (fn [db]
   (:spinner db)))
