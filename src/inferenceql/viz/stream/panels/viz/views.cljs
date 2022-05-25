(ns inferenceql.viz.stream.panels.viz.views
  (:require [re-frame.core :as rf]
            [clojure.math.combinatorics :refer [combinations]]
            [inferenceql.viz.panels.viz.views-simple :as views-simple]
            [inferenceql.viz.stream.panels.viz.circle :refer [circle-viz-spec]]
            [inferenceql.viz.stream.model.xcat-util :refer [all-row-assignments xcat-view-id-map
                                                            xcat-cluster-id-map sample-xcat-cluster]]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.stream.panels.viz.samples :refer [observed-samples virtual-samples]]))

(def default-vega-options
  "These are the default vega options for plots in iql.viz.stream. These are mostly styling
  options."
  {:renderer "svg"
   :config {:font "Roboto"
            :axis {:labelFontSize 13
                   :labelColor "#666"
                   :titleFontSize 13
                   :titleColor "#666"
                   :titlePadding 5}
            :legend {:orient "top"
                     :title nil
                     :labelFontSize 13
                     :labelColor "#333"
                     :offset 50
                     :padding "8"
                     :fillColor "#fafafa"}
            :header {:title nil
                     :labelFontSize "9"
                     :labelFontWeight "bold"
                     :labelColor "#888"
                     :labelPadding 0
                     ;; This shortens header text to just OBS or VIRT depending on the collection.
                     :labelExpr "({'observed': 'OBS', 'virtual': 'VIRT'})[datum.label]"}
            :concat {:spacing 50}}})

(defn vega-lite
  "A wrapper around the iql.viz vega-lite reagent component that applies default-vega-options for
  iql.viz.stream."
  [spec opt init-fn data params]
  (let [new-opt (merge default-vega-options opt)]
    [views-simple/vega-lite spec new-opt init-fn data params]))

(defn mi-plot
  "Reagent component for circle viz for mutual info."
  [mi-data iteration]
  (when (seq mi-data)
    (let [mi-threshold @(rf/subscribe [:home-page/mi-threshold])
          mi-data (nth mi-data iteration)
          ;; Get nodes in consistent order by picking from column-ordering.
          nodes (keep (set (keys mi-data))
                      (get-in config [:transitions :column-ordering]))
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
        xcat-model @(rf/subscribe [:model-page/model])

        all-samples (let [;; Merge in the view-cluster assignment information on observed rows.
                          row-assignments (all-row-assignments xcat-model)
                          view-key (keyword (str "view_" (:view-id cluster-selected)))
                          num-rows (count (filter #(= (get % view-key)
                                                      (:cluster-id cluster-selected))
                                                  row-assignments))
                          view-cluster-assignments (concat row-assignments (repeat {}))
                          observed-samples (map merge observed-samples view-cluster-assignments)

                          view-id (get (xcat-view-id-map xcat-model)
                                       (:view-id cluster-selected))
                          cluster-id (get (xcat-cluster-id-map xcat-model view-id)
                                          (:cluster-id cluster-selected))


                          allow-neg (get-in config [:settings :allow_negative_simulations])
                          virtual-samples (->> (sample-xcat-cluster xcat-model view-id cluster-id
                                                                    num-rows {:allow-neg allow-neg})
                                            (map #(assoc % :collection "virtual" :iter 0)))]
                      (concat observed-samples virtual-samples))
        options {:actions false}
        data {:rows all-samples}
        params {:iter iteration
                :cluster (:cluster-id cluster-selected)
                :view (some->> (:view-id cluster-selected)
                               (str "view_"))}]
    [vega-lite spec options nil data params]))

(defn select-vs-simulate-plot
  "Reagent component for select-vs-simulate plot."
  [iteration]
  (let [spec @(rf/subscribe [:viz/select-vs-simulate-spec])
        all-samples (concat observed-samples (virtual-samples iteration))
        options {:actions false}
        data {:rows all-samples}
        params {:iter iteration}]
    [vega-lite spec options nil data params]))

(defn select-plot
  [iteration]
  (let [spec @(rf/subscribe [:viz/select-spec])
        options {:actions false}
        data {:rows observed-samples}
        params {:iter iteration}]
    [vega-lite spec options nil data params]))

(defn inferences-plot
  [iteration]
  (let [spec @(rf/subscribe [:viz/inferences-spec])
        options {:actions false :renderer "canvas"}
        data {:rows (virtual-samples iteration)}]
    [vega-lite spec options nil data nil]))
