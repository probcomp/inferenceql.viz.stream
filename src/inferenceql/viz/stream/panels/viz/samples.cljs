(ns inferenceql.viz.stream.panels.viz.samples
  "Defs for properly tagging samples for use in vega-lite specs."
  (:require [inferenceql.viz.stream.store :refer [rows schema samples]]
            [inferenceql.viz.config :refer [config]]))


;;; Observed samples.

(def num-rows-required
  "Number of new rows incorporated at each model iteration."
  (let [num-rows-at-iter (get-in config [:transitions :num-rows-at-iter])]
    (map -
         num-rows-at-iter
         (concat [0] num-rows-at-iter))))

(defn add-null-columns [row]
  (let [columns (keys schema)
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
