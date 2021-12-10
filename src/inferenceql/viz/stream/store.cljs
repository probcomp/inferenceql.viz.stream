(ns inferenceql.viz.stream.store
  "Main static data-store for the app.
  Contains defs for model iterations and samples to be used in visualizations."
  (:require [clojure.set]
            [cljs-bean.core :refer [->clj]]
            [inferenceql.viz.config :refer [config]]
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

;; TODO: remove this.
(def col-ordering
  "Ordering of columns as they appear in the sequence of model iterations."
  (get-in config [:transitions :column-ordering]))
