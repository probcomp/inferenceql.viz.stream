(ns inferenceql.viz.stream.panels.viz.views
  (:require [re-frame.core :as rf]
            [clojure.math.combinatorics :refer [combinations]]
            [inferenceql.viz.panels.viz.views-simple :refer [vega-lite]]
            [inferenceql.viz.stream.panels.viz.circle :refer [circle-viz-spec]]
            [inferenceql.viz.stream.model.xcat-util :refer [columns-in-view all-row-assignments
                                                            xcat-view-id-map xcat-cluster-id-map
                                                            sample-xcat-cluster]]
            [inferenceql.viz.stream.store :refer [schema col-ordering
                                                  observed-samples virtual-samples]]))

(defn mi-plot
  "Reagent component for circle viz for mutual info."
  [mi-data iteration]
  (when (seq mi-data)
    (let [mi-threshold @(rf/subscribe [:control/mi-threshold])
          mi-data (nth mi-data iteration)
          nodes (-> (set (keys mi-data))
                    ;; Get nodes in consistent order by picking from col-ordering.
                    (keep col-ordering))
          edges (map (fn [[col-1 col-2]]
                       {:targets [col-1 col-2]
                        :val (get-in mi-data [col-1 col-2])})
                     ;; All potential edges
                     (combinations nodes 2))
          spec (circle-viz-spec nodes edges mi-threshold)
          options {:actions false :mode "vega" :renderer "canvas"}]
      ;; TODO: Make this faster by passing in nodes and edges as datasets.
      [vega-lite spec options nil nil nil])))

(defn cluster-simulate-plot
  "Reagent component for select-vs-simulate plot."
  [cluster-selected _click-count iteration]
  (let [spec @(rf/subscribe [:viz/cluster-simulate-spec])
        cols-in-view @(rf/subscribe [:app/cols-in-view])
        xcat-model @(rf/subscribe [:app/model])

        ;; Merge in the view-cluster information only when we have to.
        all-samples (let [row-assignments (all-row-assignments xcat-model)
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

                          columns-in-view (set (columns-in-view xcat-model view-id))
                          remove-neg (not (contains? columns-in-view :my-special-column)) ; Change this line
                          virtual-samples (->> (sample-xcat-cluster xcat-model view-id cluster-id
                                                                    num-rows {:remove-neg remove-neg})
                                            (map #(assoc % :collection "virtual" :iter 0)))]
                      (concat observed-samples virtual-samples))
        options {:actions false}
        data {:rows all-samples}
        params {:iter iteration
                :cluster (:cluster-id cluster-selected)
                :view_columns (clj->js (map name cols-in-view))
                :view (some->> (:view-id cluster-selected) (str "view_"))}]
    [vega-lite spec options nil data params]))

(defn select-vs-simulate-plot
  "Reagent component for select-vs-simulate plot."
  [iteration]
  (let [spec @(rf/subscribe [:viz/select-vs-simulate-spec])
        all-samples (concat observed-samples (virtual-samples iteration))
        options {:actions false}
        data {:rows all-samples}
        params {:iter iteration
                :cluster nil
                :view_columns []
                :view nil}]
    [vega-lite spec options nil data params]))

