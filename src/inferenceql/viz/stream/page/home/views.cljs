(ns inferenceql.viz.stream.page.home.views
  (:require [re-com.core :refer [v-box]]
            [inferenceql.viz.stream.page.home.ensemble :refer [ensemble-section]]
            [inferenceql.viz.stream.page.home.select-vs-simulate :refer [select-vs-simulate-section]]
            [inferenceql.viz.stream.page.home.data-table :refer [data-table-section]]))

(defn home-page []
  [v-box
   :margin "20px 20px 20px 20px"
   :children [[data-table-section]
              [ensemble-section]
              [select-vs-simulate-section]]])
