(ns inferenceql.viz.stream.panels.table.views
  (:require [handsontable$default :as yarn-handsontable]
            [reagent.core :as reagent]
            [medley.core :refer [filter-kv]]
            [reagent.core :as r]
            [re-com.core :refer [v-box h-box gap box]]
            [cljs-bean.core :refer [->clj]]
            [inferenceql.viz.stream.store :refer [rows col-ordering num-rows-at-iter
                                                  xcat-models]]
            [inferenceql.viz.stream.model.xcat-util :as xcat-util]
            [inferenceql.viz.panels.table.views-simple :refer [handsontable]]))

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
  [iteration cluster-selected]
  (let [xcat-model (nth xcat-models iteration)
        num-points (nth num-rows-at-iter iteration)
        modeled-cols (-> (set (xcat-util/columns-in-model xcat-model))
                         ;; Get modeled columns in the correct order by picking items in order
                         ;; from col-ordering.
                         (keep col-ordering))]
    [:div {:style {:overflow "hidden"
                   :border "1px solid #ccc"
                   :border-radius "4px"
                   :width "1390px"}}
     [:div {:style {:margin-left "-1px" :margin-top "-1px"}}
      [handsontable :reagent {} (take num-points rows)
       {:height "400px"
        :width "1390px"
        :cols (map name modeled-cols)
        :cells (cells-fn xcat-model cluster-selected)}]]]))
