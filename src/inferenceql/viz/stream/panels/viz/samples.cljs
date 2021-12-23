(ns inferenceql.viz.stream.panels.viz.samples
  "Defs for properly tagging samples for use in vega-lite specs."
  (:require [medley.core :as medley]
            [inferenceql.viz.stream.store :refer [rows samples]]
            [inferenceql.viz.config :refer [config]]))

;;; Observed samples.

(def num-rows-required
  "Number of new rows incorporated at each model iteration."
  (let [num-rows-at-iter (get-in config [:transitions :num-rows-at-iter])]
    (map -
         num-rows-at-iter
         (concat [0] num-rows-at-iter))))

(defn add-null-columns [row]
  (let [columns (get-in config [:transitions :column-ordering])
        null-kvs (zipmap columns (repeat nil))]
    (merge null-kvs row)))

(def iteration-tags
  (mapcat (fn [iter count]
            (repeat count {:iter iter}))
          (range)
          num-rows-required))

(def observed-samples
  "Gets all observed samples with iteration tags."
  (->> rows
    (map #(assoc % :collection "observed"))
    (map add-null-columns)
    (map merge iteration-tags)))

;;; Virtual samples.

(defn virtual-samples
  "Gets samples at `iterations` and adds dummy iteration tags,
  so these samples are always displayed."
  [iteration]
  (->> (nth samples iteration)
    (map #(assoc % :collection "virtual"
                   :iter 0))))

;;; Various settings and facts about categorical columns.

(def ranges
  (get-in config [:settings :numerical_ranges]))

(def options-count
  "Map of nominal column name to number of options"
  (->> (get-in config [:transitions :options])
    (medley/map-vals count)))

(def top-options
  "Map of nominal column name to options for that columns sort by
  frequency in observed data."
  (medley/map-kv-vals (fn [col options]
                        (let [starting-freq (zipmap options (repeat 0))
                              actual-freqs (-> (map col observed-samples)
                                               (frequencies))
                              final-freqs (merge starting-freq actual-freqs)
                              ordered-pairs (sort-by second > final-freqs)]
                          (remove nil? (map first ordered-pairs))))
                      (get-in config [:transitions :options])))
