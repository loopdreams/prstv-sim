(ns prstv-sim.vote-generator)



(def same-party-buff 40)
(def party-popularity-weighting 15)

(def test-config
  {:n-votes              500
   :candidates           #{:A :B :C :D :E :F :G}
   :candidate-popularity {:A 25
                          :B 24
                          :C 11
                          :D nil
                          :E 10
                          :F 10
                          :G 10}
   :candidate-party      {:A 1
                          :B 2
                          :C 1
                          :D 3
                          :E 3
                          :F 2
                          :G 5}
   :party-popularity     {1 10
                          2 10
                          3 10
                          4 10
                          5 10}
   :preference-depth     :mid
   :volatility           1
   :volatility-pp        1})


(defn- weighted-randomizer
  "Returns a weighted random number"
  [values weights]
  (if (apply = weights)
    (rand-nth values)
    (loop [[i & rest] (range (count values))
           rnd (rand-int (apply + weights))]
      (when i
        (if (< rnd (nth weights i))
            (nth values i)
            (recur rest (- rnd (nth weights i))))))))

(defn- ballot-depth
  "Returns the number of candidates to assign preferences to."
  [{:keys [candidates preference-depth]}]
  (let [n-candidates   (count candidates)
        partition-size (Math/round (double (/ n-candidates 3)))
        c-batches      (partition-all partition-size (range 1 (inc n-candidates)))
        weights        (case preference-depth
                         :deep    [15 20 40]
                         :mid     [10 40 10]
                         :shallow [40 20 15]
                         [(rand-int 10) (rand-int 10) (rand-int 10)])]
    (-> c-batches
        (weighted-randomizer weights)
        (rand-nth))))

(comment
  (ballot-depth test-config))

(defn- volatility-adjust
  "Adds or subtracts % of value, based on volatility level (0-100)"
  [val volatility]
  (let [adjust (rand-nth [+ -])]
    (if (and (> volatility 100) (= adjust -))
      0
      (adjust val (* (/ volatility 100) val)))))

(defn- adjust-popularity [val vol]
  (volatility-adjust val vol))

(defn- adjust-popularity-party [val vol party-popularity-val]
  (+ val (* (volatility-adjust party-popularity-val vol)
            (/ party-popularity-weighting 100))))

(defn- party-preference?
  "If another candidate from the same party has already been selected, return true."
  [party candidate-party voted-candidates]
  (when (seq voted-candidates)
    (some #{party} (map #(% candidate-party) voted-candidates))))

(defn- calc-candidate-weighting [{:keys [candidate-popularity party-popularity volatility candidate-party volatility-pp]}
                                 candidate
                                 voted-candidates]
  (let [popularity          (candidate candidate-popularity)
        party               (candidate candidate-party)
        adjusted-popularity (-> popularity
                                (adjust-popularity volatility)
                                (adjust-popularity-party volatility (get party-popularity party)))
        adjusted-popularity (if volatility-pp
                              ((rand-nth [- +]) adjusted-popularity (rand-nth (range volatility-pp)))
                              adjusted-popularity)
        adjusted-popularity (if (< adjusted-popularity 0) 0 adjusted-popularity)]
    (int
     (if (party-preference? party candidate-party voted-candidates)
       (+ adjusted-popularity same-party-buff)
       adjusted-popularity))))

(defn- determine-vote [{:keys [candidates] :as vote} voted-candidates]
  (let [candidates (sort (into [] candidates))
        weightings (for [c candidates]
                     (calc-candidate-weighting vote c voted-candidates))]
    (weighted-randomizer candidates weightings)))

(defn- mark-ballot [vote]
  (let [n-marks (ballot-depth vote)]
    (loop [[i & rest] (range 1 (inc n-marks))
           ballot {}
           voted-candidates []
           v vote]
      (if-not i
        ballot
        (let [vote-for (determine-vote v voted-candidates)
              remove-candidate (disj (:candidates v) vote-for)]
          (recur rest
                 (assoc ballot vote-for i)
                 (conj voted-candidates vote-for)
                 (assoc v :candidates remove-candidate)))))))

;; TODO optimize
(defn prstv-vote-generator [vote-config]
  (into {}
        (for [n (range 1 (inc (:n-votes vote-config)))
              :let [ballot (mark-ballot vote-config)]]
          [n ballot])))


(comment
  (prstv-vote-generator test-config))
