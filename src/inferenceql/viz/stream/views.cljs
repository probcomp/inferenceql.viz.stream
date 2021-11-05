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
     :children [[v-box
                 :margin "20px"
                 :children [[data-table iteration cluster-selected]
                            [gap :size "10px"]
                            [control/panel]
                            [gap :size "10px"]
                            [h-box
                             :children [[v-box
                                         :children [#_[js-model iteration cluster-selected]
                                                    [select-vs-simulate-plot cluster-selected
                                                     cluster-selected-click-count iteration]]]
                                        [v-box
                                         :children [[:h5
                                                     {:style {:color "black"
                                                              :font-weight "bold"
                                                              :text-align "center"}}
                                                     "Mutual Information"]
                                                    [mi-plot mutual-info iteration]]]]]]]]]))

