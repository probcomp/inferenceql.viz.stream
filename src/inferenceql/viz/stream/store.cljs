(ns inferenceql.viz.stream.store
  "Main static data-store for the app.
  Contains defs for model iterations and samples to be used in visualizations."
  (:require [clojure.set]
            [clojure.spec.alpha :as s]
            [cljs-bean.core :refer [->clj]]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.events.interceptors :refer [check-and-throw]]
            [inferenceql.viz.csv :refer [clean-csv-maps]]
            [inferenceql.viz.util :refer [keywordize-kv]]
            [inferenceql.viz.stream.transit :refer [read-transit-string]]
            [inferenceql.viz.stream.model.xcat-util :refer [columns-in-model sample-xcat]]
            [inferenceql.inference.gpm.crosscat :as xcat]))

;;; Compiled-in elements from config.

(def schema
  ;; Coerce schema to contain columns names and datatyptes as keywords.
  (keywordize-kv (:schema config)))

(def rows
  (clean-csv-maps schema (:data config)))

;; Data obtained from the global js namespace, placed there by script tags in index.html.

(def samples
  "Collection of samples at each iteration. Needs to be decompressed, then read in as a
  large transit string. Produces a CLJS collection."
  (read-transit-string (.decompress js/LZString js/transitions_samples)))

(def mutual-info
  "Mutual info between columns at every iteration. Present in js namespace as a JS object.
  Needs use of ->clj to behave like a CLJS object."
  (->clj js/mutual_info))

(def xcat-models
  "Collection of ensembles one for each iteration. Each ensemble consists of three
  transit-encoded strings. Present in js namespace as a JS object.
  Needs use of ->clj to behave like a CLJS object."
  (->clj js/transitions))

(def js-programs
  "Collection of js-program strings representing the ensemble at each iteration.
  Each ensemble consists of three strings. Present in js namespace as a JS object.
  Needs use of ->clj to behave like a CLJS object."
  (->clj js/js_program_transitions))

;;; Specs for data from global js namespace

(s/def ::samples (s/coll-of ::samples-at-iteration :kind vector?))
(s/def ::samples-at-iteration (s/coll-of ::row :kind vector?))
(s/def ::row (s/map-of keyword? any?))


(s/def ::mutual-info (s/coll-of ::mi-at-iteration :kind vector?))
(s/def ::mi-at-iteration (s/map-of keyword? (s/map-of keyword? number?)))

(s/def ::xcat-models (s/coll-of ::ensemble-at-iteration :kind vector?))
(s/def ::ensemble-at-iteration (s/coll-of ::transit-string :kind vector? :min-count 3))
(s/def ::transit-string string?)

(s/def ::js-programs (s/coll-of ::js-programs-at-iteration))
(s/def ::js-programs-at-iteration (s/coll-of string? :min-count 3))

(comment
  (check-and-throw samples ::samples "samples does not satisfy spec: ") ; Warning: very slow.
  (check-and-throw mutual-info ::mutual-info "mutual-info does not satisfy spec: ")
  (check-and-throw xcat-models ::xcat-models "xcat-models does not satisfy spec: ")
  (check-and-throw js-programs ::js-programs "js-programs does not satisfy spec: "))

;; Helper functions for accessing store data.

(defn xcat-model
  "Reifies model at given iteration and model-id."
  [iteration model-id]
  (let [t-string (get-in xcat-models [iteration model-id])
        xcat-latents (read-transit-string t-string)
        {:keys [latents spec num-rows]} xcat-latents

        data (zipmap (range) (take num-rows rows))
        options {:options (get-in config [:transitions :options])}]
    (xcat/construct-xcat-from-latents spec latents data options)))
