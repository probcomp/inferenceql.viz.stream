(ns inferenceql.viz.stream.views
  (:require [re-com.core :refer [v-box h-box gap]]
            [re-frame.core :as rf]
            [inferenceql.viz.stream.panels.control.views :as control]
            [inferenceql.viz.stream.panels.jsmodel.views :refer [js-model]]
            [inferenceql.viz.stream.panels.table.views :refer [data-table]]
            [inferenceql.viz.stream.panels.viz.views :refer [mi-plot select-vs-simulate-plot]]
            [inferenceql.viz.stream.store :refer [mutual-info]]))

(defn app
  []
  (let [iteration @(rf/subscribe [:control/iteration])
        plot-type @(rf/subscribe [:control/plot-type])
        cluster-selected @(rf/subscribe [:control/cluster-selected])
        cluster-selected-click-count @(rf/subscribe [:control/cluster-selected-click-count])]
    [v-box
     :children [[control/panel]
                [v-box
                 :margin "20px"
                 :children [[data-table iteration cluster-selected]
                            [gap :size "30px"]
                            (case plot-type
                              :mutual-information
                              [h-box
                               :width "3700px"
                               :style {:flex-flow "row wrap"}
                               ;; Create a mi-plot for mi-info from each CrossCat sample.
                               :children (for [mi mutual-info]
                                           [mi-plot mi iteration])]

                              :select-vs-simulate
                              [h-box
                               :children [#_[js-model iteration cluster-selected]
                                          [gap :size "20px"]
                                          [select-vs-simulate-plot cluster-selected
                                           cluster-selected-click-count iteration]]])]]]]))
