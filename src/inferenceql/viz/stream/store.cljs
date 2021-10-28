(ns inferenceql.viz.stream.store
  "Main static data-store for the app.
  Contains defs for model iterations and samples to be used in visualizations."
  (:require [clojure.set]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.csv :refer [clean-csv-maps]]
            [inferenceql.viz.util :refer [keywordize-kv]]
            [inferenceql.inference.gpm :as gpm]
            [inferenceql.inference.gpm.crosscat :as crosscat]
            [inferenceql.viz.stream.model.xcat-util :refer [columns-in-model sample-xcat]]))

;;; Compiled-in elements from config.

(def schema
  ;; Coerce schema to contain columns names and datatyptes as keywords.
  (keywordize-kv (:schema config)))

(def rows (clean-csv-maps schema (:data config)))

;; Data obtained from the global js namespace, placed there by scripts tags in index.html.

;TODO : Try using ->clj
(def mutual-info (js->clj js/mutual_info :keywordize-keys true))
;TODO : Try using ->clj
(def xcat-models
  "Sequence of xcat models for each iteration."
  (gpm/read-string (js->clj js/transitions)))

;;; Model iterations

(def mmix-models
  "Sequence of mmix models for each iteration."
  ;; Using doall so models are fully evaled. Scrubbing through iterations will be smooth.
  (doall (map crosscat/xcat->mmix xcat-models)))

;;; Secondary defs built off of xcat model iterations.

(def col-ordering
  "Ordering of columns as they appear in the sequence of model iterations."
  (reduce (fn [ordering xcat]
            (let [new-columns (clojure.set/difference (set (columns-in-model xcat))
                                                      (set ordering))]
              (concat ordering new-columns)))
          []
          xcat-models))

(def num-rows-at-iter
  "Number of rows used at each model iteration."
  (map (fn [xcat]
         (let [[_view-1-name view-1] (first (get xcat :views))]
           ;; Count the number of row to cluster assignments.
           (count (get-in view-1 [:latents :y]))))
       xcat-models))

(def num-rows-required
  "Number of new rows incorporated at this model iteration."
  (map - num-rows-at-iter (conj num-rows-at-iter 0)))

;;; Settings up samples.

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
  (->> rows
       (map #(assoc % :collection "observed"))
       (map add-null-columns)
       (map merge iteration-tags)))

(def virtual-samples
  (->> (mapcat sample-xcat xcat-models num-rows-required (repeat {:remove-neg true}))
       (map #(assoc % :collection "virtual"))
       (map add-null-columns)
       (map merge iteration-tags)))

(def all-samples (concat observed-samples virtual-samples))
