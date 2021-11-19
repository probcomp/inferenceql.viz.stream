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
                                         :children [[select-vs-simulate-plot cluster-selected
                                                     cluster-selected-click-count iteration]]]
                                        [gap :size "50px"]
                                        [v-box
                                         :children [[:h5
                                                     {:style {:color "black"
                                                              :font-weight "bold"
                                                              :text-align "center"}}
                                                     "Column dependencies"]
                                                    [mi-plot mutual-info iteration]]]]]
                            [gap :size "50px"]
                            [h-box
                             :gap "50px"
                             :children [[js-model 0 iteration cluster-selected]
                                        [js-model 1 iteration cluster-selected]
                                        [js-model 2 iteration cluster-selected]]]]]]]))
