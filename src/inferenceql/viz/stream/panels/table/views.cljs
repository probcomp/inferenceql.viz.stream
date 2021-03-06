(ns inferenceql.viz.stream.panels.table.views
  (:require [inferenceql.viz.stream.store :refer [rows]]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.stream.model.xcat-util :as xcat-util]
            [inferenceql.viz.panels.table.views-simple :refer [handsontable]]
            [re-frame.core :as rf]))

(def default-cells-fn
  (fn [_ _ _] #js {}))

(defn cells-fn [xcat-model cluster-selected]
  (if-not cluster-selected
    default-cells-fn
    (let [cols-set (set (xcat-util/columns-in-view xcat-model (:view-id cluster-selected)))
          rows-set (set (xcat-util/rows-in-view-cluster xcat-model
                                                        (:view-id cluster-selected)
                                                        (:cluster-id cluster-selected)))]
      (fn [row _col prop]
        (if (and (rows-set row)
                 (cols-set (keyword prop)))
          #js {:className "blue-highlight"}
          #js {})))))

(defn data-table
  "Reagent component for data table."
  [iteration cluster-selected options]
  (let [xcat-model @(rf/subscribe [:model-page/model])
        num-points (get-in config [:transitions :num-rows-at-iter iteration])

        num-columns (get-in config [:transitions :columns-at-iter iteration])
        col-ordering (get-in config [:transitions :column-ordering])
        modeled-cols (take num-columns col-ordering)

        hot-options {:height "400px"
                     :width "1390px"
                     :cols (map name modeled-cols)
                     :cells (cells-fn xcat-model cluster-selected)}
        hot-options (merge hot-options options)]
    [handsontable {} (take num-points rows) hot-options false]))
