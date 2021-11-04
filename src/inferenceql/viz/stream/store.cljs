(ns inferenceql.viz.stream.store
  "Main static data-store for the app.
  Contains defs for model iterations and samples to be used in visualizations."
  (:require [clojure.set]
            [cljs-bean.core :refer [->clj]]
            [cognitect.transit :as t]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.csv :refer [clean-csv-maps]]
            [inferenceql.viz.util :refer [keywordize-kv]]
            [inferenceql.inference.gpm :as gpm]
            [inferenceql.inference.gpm.column :as column]
            [inferenceql.inference.gpm.compositional :as compositional]
            [inferenceql.inference.gpm.crosscat :as xcat]
            [inferenceql.inference.gpm.primitive-gpms.bernoulli :as bernoulli]
            [inferenceql.inference.gpm.primitive-gpms.categorical :as categorical]
            [inferenceql.inference.gpm.primitive-gpms.gaussian :as gaussian]
            [inferenceql.inference.gpm.view :as view]

            [inferenceql.viz.stream.model.xcat-util :refer [columns-in-model sample-xcat]]))

;; Stuff related to transit.
;; TODO: Move this to iql.inference or another ns

(def readers
  (let [class-names ["inferenceql.inference.gpm.column.Column"
                     "inferenceql.inference.gpm.compositional.Compositional"
                     "inferenceql.inference.gpm.crosscat.XCat"
                     "inferenceql.inference.gpm.primitive_gpms.bernoulli.Bernoulli"
                     "inferenceql.inference.gpm.primitive_gpms.categorical.Categorical"
                     "inferenceql.inference.gpm.primitive_gpms.gaussian.Gaussian"
                     "inferenceql.inference.gpm.view.View"]
        constructors [column/map->Column
                      compositional/map->Compositional
                      xcat/map->XCat
                      bernoulli/map->Bernoulli
                      categorical/map->Categorical
                      gaussian/map->Gaussian
                      view/map->View]
        read-handlers (map t/read-handler constructors)]
    (zipmap class-names read-handlers)))

(def transit-reader (t/reader :json {:handlers readers}))

;;; Compiled-in elements from config.

(def schema
  ;; Coerce schema to contain columns names and datatyptes as keywords.
  (keywordize-kv (:schema config)))

(def rows (clean-csv-maps schema (:data config)))

;; Data obtained from the global js namespace, placed there by scripts tags in index.html.

(def transitions-samples
  (t/read transit-reader js/transitions_samples))

(.log js/console :samples transitions-samples)

;; FIXME: Don't put mutual info in an array. Fix on auto-modeling side perhaps.
(def mutual-info [(->clj js/mutual_info)])

(def transitions
  (t/read transit-reader js/transitions))

(def xcat-models
  "Sequence of xcat models for each iteration."
  (first transitions))

;;; Model iterations

(defn mmix-model [i]
  (xcat/xcat->mmix (nth xcat-models i)))

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

;; TODO: give this a collection xcat records taken randomly from the ensemble.
(def virtual-samples
  (->> (mapcat sample-xcat xcat-models num-rows-required (repeat {:remove-neg true}))
       (map #(assoc % :collection "virtual"))
       (map add-null-columns)
       (map merge iteration-tags)))

(def all-samples (concat observed-samples virtual-samples))
