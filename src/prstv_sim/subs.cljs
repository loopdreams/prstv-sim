(ns prstv-sim.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::inputs
 (fn [db [_ k id]]
   (cond
     id    (get-in db [:inputs k id])
     k     (get-in db [:inputs k])
     :else (get db :inputs))))


(re-frame/reg-sub
 ::active-party-ids
 :<- [::inputs]
 (fn [all-inputs]
   (let [cs (:candidate all-inputs)]
     (->> cs
          vals
          (map :party-id)
          (into #{})))))


(re-frame/reg-sub
 ::active-party-names
 :<- [::inputs]
 (fn [all-inputs]
   (map (fn [[_ {:keys [name]}]] name) (:party all-inputs))))


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
 ::all-ballots
 (fn [db]
   (:all-ballots db)))

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
 ::results
 (fn [db]
   (:results db)))

(re-frame/reg-sub
 ::sankey-selector
 (fn [db]
   (:sankey-selector db)))

(re-frame/reg-sub
 ::sankey-show?
 (fn [db]
   (:sankey-show? db)))

(re-frame/reg-sub
 ::processing-sankey-chart
 (fn [db]
   (:processing-sankey-chart db)))

(re-frame/reg-sub
 ::display-tabs?
 (fn [db]
   (:display-tabs? db)))

(re-frame/reg-sub
 ::active-preconfig
 (fn [db]
   (:pre-config db)))
