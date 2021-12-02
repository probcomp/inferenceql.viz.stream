(ns inferenceql.viz.stream.views
  (:require [re-com.core :refer [v-box h-box box gap title info-button hyperlink]]
            [re-frame.core :as rf]
            [inferenceql.viz.stream.panels.control.views :as control]
            [inferenceql.viz.stream.panels.jsmodel.views :refer [js-model tiny-js-model]]
            [inferenceql.viz.stream.panels.table.views :refer [data-table]]
            [inferenceql.viz.stream.panels.viz.views :refer [mi-plot select-vs-simulate-plot]]
            [inferenceql.viz.stream.store :refer [mutual-info]]))

(defn model-summaries [iteration]
  [v-box
   :children [[title :level :level3 :label "Model summaries"]
              [h-box
               :children [[tiny-js-model 0 iteration]
                          [tiny-js-model 1 iteration]
                          [tiny-js-model 2 iteration]]]]])

(defn app
  []
  (let [iteration @(rf/subscribe [:control/iteration])
        plot-type @(rf/subscribe [:control/plot-type])
        cluster-selected @(rf/subscribe [:control/cluster-selected])
        cluster-selected-click-count @(rf/subscribe [:control/cluster-selected-click-count])]
    [v-box
     :children [[box
                 :style {:top 0
                         :position "sticky"
                         :padding "20px 20px 20px 20px"
                         :background "white"
                         :z-index 10000
                         :box-shadow (str "rgba(0, 0, 0, 0.05) 0px 1px 2px 0px, "
                                          "rgba(0, 0, 0, 0.05) 0px 1px 4px 0px, "
                                          "rgba(0, 0, 0, 0.05) 0px 2px 8px 0px")}
                 :child [control/iteration]]
                [v-box
                 :margin "20px 20px 20px 20px"
                 :children [
                            ;; Section 1
                            [h-box
                             :children [[title :level :level2 :label "Data Table"]
                                        [gap :size "5px"]
                                        [info-button
                                         :style {:margin-top "8px"}
                                         :info "This is the ..."]
                                        [gap :size "20px"]
                                        [hyperlink
                                         :parts {:wrapper {:style {:margin-top "6px" :align-self "center"}}}
                                         :label "hide" :on-click nil]
                                        [gap :size "20px"]
                                        [hyperlink
                                         :parts {:wrapper {:style {:margin-top "6px" :align-self "center"}}}
                                         :label "small" :on-click nil
                                         :style {:padding "2px 10px"
                                                 :background-color "whitesmoke"}]
                                        [gap :size "20px"]
                                        [hyperlink
                                         :parts {:wrapper {:style {:margin-top "6px" :align-self "center"}}}
                                         :label "large" :on-click nil]]]
                            [gap :size "5px"]
                            [data-table iteration cluster-selected]
                            [gap :size "20px"]

                            ;; Section 2
                            [h-box
                             :children [[title :level :level2 :label "Ensemble"]
                                        [gap :size "5px"]
                                        [info-button
                                         :style {:margin-top "8px"}
                                         :info "This is the ..."]
                                        [gap :size "20px"]
                                        [hyperlink
                                         :parts {:wrapper {:style {:margin-top "8px" :align-self "center"}}}
                                         :label "hide" :on-click nil]
                                        [gap :size "20px"]
                                        [hyperlink
                                         :parts {:wrapper {:style {:margin-top "8px" :align-self "center"}}}
                                         :label "options" :on-click nil]]]

                            [h-box
                             :children [[model-summaries iteration]
                                        [gap :size "30px"]
                                        [mi-plot mutual-info iteration]]]

                            ;; Section 3
                            [h-box
                             :children [[title :level :level2 :label "Select vs. Simulate"]
                                        [gap :size "5px"]
                                        [info-button
                                         :style {:margin-top "8px"}
                                         :info "This is the ..."]
                                        [gap :size "20px"]
                                        [hyperlink
                                         :parts {:wrapper {:style {:margin-top "8px" :align-self "center"}}}
                                         :label "options" :on-click nil]]]
                            ;; TODO: fix plot options.
                            #_[control/plot-options]
                            [gap :size "5px"]
                            [select-vs-simulate-plot cluster-selected
                             cluster-selected-click-count iteration]
                            [gap :size "40px"]

                            ;; Section 4
                            #_[h-box :children [[title :level :level2 :label "Model programs"]]]
                            #_[h-box
                               :gap "50px"
                               :children [[js-model 0 iteration cluster-selected]
                                          [js-model 1 iteration cluster-selected]
                                          [js-model 2 iteration cluster-selected]]]]]]]))
