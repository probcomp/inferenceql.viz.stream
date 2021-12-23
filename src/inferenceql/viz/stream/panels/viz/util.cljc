(ns inferenceql.viz.stream.panels.viz.util
  "Supporting code for producing vega-lite specs."
  (:require [medley.core :as medley]))

(def vl5-schema "https://vega.github.io/schema/vega-lite/v5.json")

(def obs-data-color "#4e79a7") ;; Tableau-10 Blue
(def virtual-data-color "#f28e2b") ;; Tableau-10 Orange
(def unselected-color "lightgrey")

(defn vega-type-fn
  "Given a `schema`, returns a vega-type function.

  Args:
    schema: (map) Mapping from column name to iql stat-type.

  Returns: (a function) Which returns a vega-lite type given `col-name`, a column name
    from the data table. Returns nil if vega-lite type can't be deterimend."
  [schema]
  (fn [col-name]
    (let [;; Ensure schema's columns names are strings in order to
          ;; be more permissive.
          schema (medley/map-keys name schema)
          ;; Mapping from multi-mix stat-types to vega-lite data-types.
          mapping {:numerical "quantitative"
                   :nominal "nominal"
                   :ignore "nominal"}]
      (get mapping (get schema (name col-name))))))
