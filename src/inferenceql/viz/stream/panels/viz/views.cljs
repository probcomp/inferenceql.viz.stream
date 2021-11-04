(ns inferenceql.viz.stream.panels.viz.views
  (:require [re-frame.core :as rf]
            [clojure.math.combinatorics :refer [combinations]]
            [inferenceql.viz.panels.viz.views-simple :refer [vega-lite]]
            [inferenceql.viz.stream.panels.viz.dashboard :as dashboard]
            [inferenceql.viz.stream.panels.viz.circle :refer [circle-viz-spec]]
            [inferenceql.viz.stream.model.xcat-util :refer [columns-in-view all-row-assignments
                                                            xcat-view-id-map xcat-cluster-id-map
                                                            sample-xcat-cluster]]
            [inferenceql.viz.stream.store :refer [schema xcat-models col-ordering
                                                  observed-samples virtual-samples]]))

(defn mi-plot
  "Reagent component for circle viz for mutual info."
  [mi-data iteration]
  (when mi-data
    (let [mi-threshold @(rf/subscribe [:control/mi-threshold])
          mi-data (-> mi-data (nth iteration) :mi)
          nodes (-> (set (keys mi-data))
                    ;; Get nodes in consistent order by picking from col-ordering.
                    (keep col-ordering))
          edges (filter (fn [[col-1 col-2]]
                          (>= (get-in mi-data [col-1 col-2])
                              mi-threshold))
                        ;; All potential edges
                        (combinations nodes 2))
          spec (circle-viz-spec nodes edges)
          options {:actions false :mode "vega" :renderer "canvas"}]
      ;; TODO: Make this faster by passing in nodes and edges as datasets.
      [vega-lite spec options nil nil nil])))

(defn select-vs-simulate-plot
  "Reagent component for select-vs-simulate plot."
  [cluster-selected _click-count iteration]
  (let [viz-cols @(rf/subscribe [:control/col-selection])
        marginal-types @(rf/subscribe [:control/marginal-types])

        xcat-model (nth xcat-models iteration)

        ;; Merge in the view-cluster information only when we have to.
        all-samples (if cluster-selected
                      (let [row-assignments (all-row-assignments xcat-model)
                            view-key (keyword (str "view_" (:view-id cluster-selected)))
                            num-rows (count (filter #(= (get % view-key)
                                                        (:cluster-id cluster-selected))
                                                    row-assignments))
                            view-cluster-assignments (concat row-assignments (repeat {}))
                            observed-samples (map merge observed-samples view-cluster-assignments)

                            view-map (xcat-view-id-map xcat-model)
                            view-id (view-map (:view-id cluster-selected))
                            cluster-map (xcat-cluster-id-map xcat-model view-id)
                            cluster-id (cluster-map (:cluster-id cluster-selected))

                            virtual-samples (->> (sample-xcat-cluster xcat-model view-id cluster-id
                                                                      num-rows {:remove-neg true})
                                                 (map #(assoc % :collection "virtual" :iter 0)))]
                        (concat observed-samples virtual-samples))
                      (concat observed-samples (virtual-samples iteration)))
        cols-in-view (set (columns-in-view xcat-model (:view-id cluster-selected)))
        cols (or (seq cols-in-view) viz-cols)
        ranges {:BMI [-15 65]}

        spec (dashboard/spec observed-samples schema cols 10 marginal-types ranges)
        options {:actions false}
        data {:rows all-samples}
        params {:iter iteration
                :cluster (:cluster-id cluster-selected)
                :view_columns (clj->js (map name cols-in-view))
                :view (some->> (:view-id cluster-selected) (str "view_"))}]
    [vega-lite spec options nil data params]))

