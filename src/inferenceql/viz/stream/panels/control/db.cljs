(ns inferenceql.viz.stream.panels.control.db
  "Main db for state of user interactions and settings."
  (:require [clojure.spec.alpha :as s]
            [clojure.set]
            [inferenceql.viz.stream.store :refer [mutual-info starting-cols]]))

(def mi-bounds
  {:min 0
   :max 10})

(def mi-initial-threshold
    3)

(def default-db
  {:control-panel {:iteration 0
                   :col-selection (set starting-cols)
                   :plot-type :select-vs-simulate
                   :marginal-types #{:1D}
                   :show-plot-options false
                   :mi-bounds mi-bounds
                   :mi-threshold mi-initial-threshold}})

(s/def ::control-panel (s/keys :req-un [::iteration
                                        ::col-selection
                                        ::plot-type
                                        ::marginal-types
                                        ::show-plot-options
                                        ::mi-threshold]
                               :opt-un [::cluster-selected
                                        ::cluster-selected-click-count]))

(s/def ::iteration integer?)
(s/def ::col-selection set?)
(s/def ::plot-type #{:select-vs-simulate :mutual-information})
(s/def ::marginal-types #(and (set? %)
                              (clojure.set/subset? % #{:1D :2D})))
(s/def ::show-plot-options boolean?)
(s/def ::mi-threshold number?)

(s/def ::cluster-selected (s/keys :req-un [::cluster-id ::view-id]))
(s/def ::cluster-selected-click-count integer?)
(s/def ::cluster-id integer?)
(s/def ::view-id integer?)
