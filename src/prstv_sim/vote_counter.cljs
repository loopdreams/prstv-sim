(ns prstv-sim.vote-counter
  (:require [prstv-sim.subs :as subs]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [prstv-sim.vote-generator :as votes]
            [clojure.set :as set]))

(defn quota [n-votes n-seats]
  (-> n-votes
      (/ (inc n-seats))
      (+ 1)))

;; TODO discount 'no preference' here?
(defn counts->proportions [counts]
  (let [total (reduce + (vals counts))]
    (reduce (fn [result candidate]
              (assoc result candidate (double (/ (candidate counts) total))))
            {}
            (keys counts))))

(defn share-distributor
  "Takes a map of counts and an integer (shares) to be distributed. Returns a map"
  [counts shares]
  (let [proportions (counts->proportions (dissoc counts :no-preference))]
    (loop [shares        shares
           need          (reduce #(assoc %1 %2 (- (%2 proportions))) {} (keys proportions))
           shares-p      (reduce #(assoc %1 %2 0) {} (keys proportions))
           distributions shares-p]
      (if (zero? shares) distributions
          (let [target          (ffirst (sort-by val need))
                dist-update     (update distributions target inc)
                shares-p-update (counts->proportions dist-update)
                shares-p-diff   (zipmap (keys (sort shares-p))
                                        (map (fn [a b] (- a b))
                                             (vals (sort shares-p))
                                             (vals (sort shares-p-update))))
                need-update     (reduce #(assoc %1 %2 (- (%2 need) (or (%2 shares-p-diff) 0))) {} (keys need))]
            (recur
             (dec shares)
             need-update
             shares-p-update
             dist-update))))))

(defn share-distributor-ratio [counts surplus]
  (let [valid-votes (dissoc counts :no-preference)
        transferrables (reduce + (vals valid-votes))
        ratio (/ surplus transferrables)]
    (reduce #(assoc %1 %2 (Math/round (double (* (%2 counts) ratio)))) {} (keys valid-votes))))

(comment
  ;; In this case E gets 4 votes, while B and G get 3 ... what is a fairer way to do this?
  (share-distributor {:E 7, :F 4, :G 7, :no-preference 1, :B 7, :D 2, :C 3} 10))

;; According to the guidelines, a surplus can be redistributed if:
;; - It can elect the highest continuing candidate
;; - Can bring the lowest continuing candidate level with or above the second lowest continuing candidate
;; - Can qualify the lowest continuing candidate for recoupment of their election expenses or deposit (if applicable)
;; Ignoring the last part
;; I'm also assuming that if the last two candidates are equal, you can also distribute the surplus (since it
;; changes the picture at the bottom)

(defn redistribute-surplus? [quota candidate-counts surplus]
  (let [vs (vals candidate-counts)
        plus-surplus (map #(+ % surplus) vs)
        diff-lowest (->> (sort vs)
                         (take 2)
                         reverse
                         (apply -))
        above-quota (filter #(>= % quota) plus-surplus)]
    (or (seq above-quota)
        (<= diff-lowest surplus))))

(comment
  (redistribute-surplus? 10 {:A 3 :B 6 :C 8} 1)
  (redistribute-surplus? 201 {:peter-pan 187, :snow-white 159, :cinderella 158, :micky-mouse 130, :minnie-mouse 143} 13))


;; Counts

(defn vote-counts [piles]
  (reduce (fn [res [c vs]]
            (assoc res c (count vs)))
          {}
          piles))

(defn count-changes [old-counts new-counts]
  (reduce #(assoc %1 %2 (- (%2 new-counts) (%2 old-counts))) {} (keys new-counts)))

(defn get-counts-eliminated [counts]
  (->> (into [] counts)
       (group-by second)
       sort
       first
       second
       (map first)))

;; Sort

(defn sort-votes-by-next-preference
  [active-candidates t-votes & voteids]
  (reduce (fn [piles [id ballot]]
            (let [target (->> (select-keys ballot active-candidates)
                              (sort-by val)
                              ffirst)]
              (update piles (or target :no-preference) (fnil conj []) id)))
          {}
          (if voteids (select-keys t-votes (first voteids)) t-votes)))


(defn vote-preferences
  "Returns a list of preferences in pile. Used to determine if count is first count."
  [piles t-votes candidate]
  (let [voteids (candidate piles)]
    (map #(candidate (t-votes %)) voteids)))

;; Distribution

(defn elimination-distribute [piles t-votes eliminated-candidate active-candidates]
  (let [voteids (eliminated-candidate piles)
        votes-d (sort-votes-by-next-preference active-candidates t-votes voteids)
        vs (dissoc votes-d :no-preference)]
    (->
     (reduce (fn [p [c vs]]
               (update p c concat vs))
             piles
             vs)
     (assoc (keyword (str (name eliminated-candidate) "-non-transferrable")) (:no-preference votes-d))
     (assoc eliminated-candidate []))))

;; TODO handle when distributable is less than surplus
(defn surplus-distribute [piles t-votes elected-candidate active-candidates quota count-changes]
  (let [voteids    (elected-candidate piles)
        cand-count (count voteids)
        surplus    (- cand-count quota)]
    (if-not (pos? surplus)
      piles
      (let [first-pref-votes  (vote-preferences piles t-votes elected-candidate)
            first-prefs-only? (apply = first-pref-votes)
            votes-d           (if first-prefs-only?
                                (sort-votes-by-next-preference active-candidates t-votes voteids)
                                (sort-votes-by-next-preference
                                 active-candidates
                                 t-votes
                                 (take (or (elected-candidate count-changes) 0)
                                       (reverse voteids))))
            vs                (dissoc votes-d :no-preference)
            counts            (vote-counts vs)
            shares            (share-distributor (counts->proportions counts) surplus)
            piles             (assoc piles elected-candidate
                                     (if first-prefs-only?  []
                                         (drop (elected-candidate count-changes)
                                               (reverse voteids))))]
        (->
         (reduce (fn [p [c vs]]
                   (let [share (c shares)
                         dist  (take share vs)
                         keep  (drop share vs)]
                     (-> p
                         (update c concat dist)
                         (update elected-candidate concat keep))))
                 piles
                 vs)
         (update elected-candidate concat (:no-preference votes-d)))))))



;; Run Count


;; For final output table
(defn sort-candidate-positions [counts cands]
  (->> (for [c cands
             :let [count (c counts)]]
         [c count])
       (sort-by second)
       reverse
       (map first)))

(defn assign-candidate-position
  "Status is :elected or :eliminated - indicates to take from start or end of positions"
  [count-states positions candidates counts count-n status]
  (if (or (empty? candidates) (not candidates)) count-states
      (loop [[c & cs] (sort-candidate-positions counts candidates)
             res count-states
             ps positions]
        (if-not c
          res
          (recur cs
                 (-> res
                     (assoc-in [:table-data c :position]
                               (first (if (= status :elected) ps (reverse ps))))
                     (assoc-in [:table-data c :exit] count-n))
                 (if (= status :elected) (rest ps) (drop-last ps)))))))



;; TODO rename inner 'counts' key (since there is also an outer 'counts' key)
(defn run-vote-counts [candidates t-votes number-of-seats]
  (let [piles           (sort-votes-by-next-preference candidates t-votes)
        quota           (-> (quota (count t-votes) number-of-seats)
                            float
                            js/Math.round) ;; TODO Check if this should be round or floor
        first-pref-votes (vote-counts piles)]
    (loop [p            piles
           cnt-changes  {}
           elected      #{}
           eliminated   #{}
           active       candidates
           count-n      1
           positions    (range (count candidates))
           count-states (-> {:quota quota
                             :seats number-of-seats
                             :table-data nil}
                            (assoc-in [:counts 0] {:piles piles :counts first-pref-votes}))]
      (cond
        (= (count elected) number-of-seats) [elected
                                             (vote-counts p)
                                             first-pref-votes
                                             (-> count-states

                                                 (assign-candidate-position
                                                  positions
                                                  active
                                                  (select-keys (vote-counts p) active)
                                                  count-n
                                                  :eliminated))]
        (= (count active) (- number-of-seats (count elected))) [(into elected active)
                                                                (vote-counts p)
                                                                first-pref-votes
                                                                (-> count-states

                                                                    (assign-candidate-position
                                                                     positions
                                                                     active
                                                                     (select-keys (vote-counts p) active)
                                                                     count-n
                                                                     :elected))]
        :else (let [counts        (select-keys (vote-counts p) active)
                    elec          (filter #(>= (or (% counts) 0) quota) active)
                    surpluses     (reduce #(assoc %1 %2 (- (%2 counts) quota)) {} elec)
                    total-surplus (reduce + (vals surpluses))
                    counts-for-surplus-calc (select-keys counts (apply (partial disj active) elec))]
                (if (and (seq elec) (redistribute-surplus? quota counts-for-surplus-calc total-surplus))
                  (let [active     (apply (partial disj active) elec)
                        new-piles  (reduce (fn [p elected]
                                             (surplus-distribute p t-votes elected active quota cnt-changes))
                                           p elec)
                        new-counts (select-keys (vote-counts new-piles) active)
                        c-changes  (count-changes counts new-counts)]
                    (recur new-piles
                           c-changes
                           (into elected elec)
                           eliminated
                           active
                           (inc count-n)
                           (drop (count elec) positions)
                           (-> count-states
                               (assoc-in [:counts count-n]
                                         {:piles         new-piles
                                          :count-changes c-changes
                                          :elected       (into elected elec)
                                          :surpluses     surpluses
                                          :eliminated    eliminated
                                          :active        active
                                          :counts        new-counts})
                               (assign-candidate-position positions elec counts count-n :elected))))
                  (let [elim       (get-counts-eliminated counts)
                        active     (apply (partial disj active) elim)
                        active     (if (seq elec) (apply (partial disj active) elec)
                                       active)
                        new-piles  (reduce (fn [p el]
                                             (elimination-distribute p t-votes el active))
                                           p elim)
                        new-counts (select-keys (vote-counts new-piles) active)
                        c-changes  (count-changes counts new-counts)]
                    (recur new-piles c-changes
                           (if (seq elec) (into elected elec) elected)
                           (into eliminated elim)
                           active
                           (inc count-n)
                           (if (seq elec)
                             (->> (drop (count elec) positions)
                                  reverse
                                  (drop (count elim))
                                  reverse)
                             (-> (drop (count elim) (reverse positions)) reverse))
                           (-> count-states
                               (assoc-in [:counts count-n]
                                         {:piles         new-piles
                                          :count-changes c-changes
                                          :elected       (into elected elec)
                                          :eliminated    (into eliminated elim)
                                          :active        active
                                          :counts        new-counts})
                               (assign-candidate-position positions elim counts count-n :eliminated)
                               (assign-candidate-position positions elec counts count-n :elected))))))))))


(comment
  (last (run-vote-counts (:candidates votes/test-config) (votes/prstv-vote-generator votes/test-config) 3))
  (sort-votes-by-next-preference #{:A :B} (votes/prstv-vote-generator votes/test-config)))
