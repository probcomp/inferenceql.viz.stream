(ns inferenceql.viz.stream.panels.control.db
  "Main db for state of user interactions and settings."
  (:require [clojure.spec.alpha :as s]
            [clojure.set]
            [inferenceql.viz.stream.store :as store]
            [inferenceql.viz.stream.store :refer [mutual-info]]))

(def default-col-selection
  (-> store/xcat-models first :latents :z keys set))

(def mi-bounds
  (if (seq mutual-info)
    (let [mi-vals (flatten
                   (for [mi-model-iter mutual-info]
                     (for [[_col-1 inner-map] (:mi mi-model-iter)]
                       (for [[_col-2 mi-val] inner-map]
                         mi-val))))]
      {:min (apply min mi-vals)
       :max (apply max mi-vals)})
    {:min 0
     :max 1}))

(def mi-initial-threshold
  (if (seq mutual-info)
    (* (- (:max mi-bounds)
          (:min mi-bounds))
       (/ 1 200))
    0))

(def default-db
  {:control-panel {:iteration 0
                   :col-selection default-col-selection
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
