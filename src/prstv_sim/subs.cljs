(ns prstv-sim.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))


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
          (if (= type :party-form) (:colour form) true)
          (or (not popularity)
              (and
               (every? #(re-find #"\d" %) popularity)
               (< popularity 100)))))))

(re-frame/reg-sub
 ::inputs
 (fn [db [_ k id]]
   (get-in db (if id [:inputs k id] [:inputs k]))))

(re-frame/reg-sub
 ::party-list
 (fn [db]
   (let [parties (-> db :inputs :party)]
     (for [p parties
           :let [[_ {:keys [name]}] p]]
       name))))
