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

(defn read-transit-string [string]
  (t/read transit-reader string))

;;; Compiled-in elements from config.

(def schema
  ;; Coerce schema to contain columns names and datatyptes as keywords.
  (keywordize-kv (:schema config)))

(def rows (clean-csv-maps schema (:data config)))

;; Data obtained from the global js namespace, placed there by scripts tags in index.html.

(def transitions-samples (->clj js/transitions_samples))

(def mutual-info (->clj js/mutual_info))

(def transitions (->clj js/transitions))

(def xcat-models
  (mapv (fn [transit-strings]
          (mapv read-transit-string (take 1 transit-strings)))
        transitions))

;;; Model iterations

(def mmix-model
  (let [store (atom {})]
    (fn [model-num i]
      (let [k [model-num i]
            hit-maybe (get @store k)]
        (if hit-maybe
          hit-maybe
          (let [new-val (xcat/xcat->mmix (get-in xcat-models [i model-num]))]
            (swap! store assoc k new-val)
            new-val))))))


;;; Secondary defs built off of xcat model iterations.

(def num-transitions
  (count transitions))

(def first-stream-transitions
  (map first xcat-models))

(def starting-cols
  (-> first-stream-transitions first :latents :z keys set))

(def col-ordering
  "Ordering of columns as they appear in the sequence of model iterations."
  (reduce (fn [ordering xcat]
            (let [new-columns (clojure.set/difference (set (columns-in-model xcat))
                                                      (set ordering))]
              (concat ordering new-columns)))
          []
          first-stream-transitions))

(def num-rows-at-iter
  "Number of rows used at each model iteration."
  (map (fn [xcat]
         (let [[_view-1-name view-1] (first (get xcat :views))]
           ;; Count the number of row to cluster assignments.
           (count (get-in view-1 [:latents :y]))))
       first-stream-transitions))

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

;; NOTE: this samples from the model. It is too slow, however.
#_(defn virtual-samples [iteration]
    (->> (sample-xcat (nth xcat-models iteration) 1000)
         (map #(assoc % :collection "virtual"))
         (map add-null-columns)
         (map #(assoc % :iter 0))))

;; Force the reading of transit strings for samples at each iteration.
(def transitions-samples-reified
  ;; TODO: are this being read in as a simple js objects. Should I surround in ->clj?
  (mapv read-transit-string transitions-samples))

(defn virtual-samples [iteration]
  (->> (nth transitions-samples-reified iteration)
       ;; TODO: move this stuff to pre-process stage.
       (map #(assoc % :collection "virtual"))
       (map add-null-columns)
       (map #(assoc % :iter 0))))
