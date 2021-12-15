(ns inferenceql.viz.stream.page.home.data-table
  (:require [re-com.core :refer [v-box h-box box gap title info-button
                                 checkbox line hyperlink popover-tooltip
                                 h-split
                                 horizontal-tabs]]
            [re-frame.core :as rf]
            [inferenceql.viz.stream.panels.table.views :refer [data-table]]
            [inferenceql.viz.stream.panels.viz.views :refer [select-plot]]))

(defn data-table-section []
  (let [iteration @(rf/subscribe [:control/iteration])
        cluster-selected @(rf/subscribe [:model-page/cluster-selected])
        show-data-table-section @(rf/subscribe [:home-page/show-data-table-section])
        data-table-size @(rf/subscribe [:home-page/data-table-size])
        small-size "400px"
        large-size "1000px"]
    [:<>
     [h-box
      :children [[title :level :level2 :label "Data"]
                 [gap :size "5px"]
                 [info-button
                  :style {:margin-top "8px"}
                  :info "This is the ..."]
                 [gap :size "20px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "6px" :align-self "center"}}}
                  :style {:color (when-not show-data-table-section "darkblue")}
                  :label "hide" :on-click #(rf/dispatch [:home-page/toggle-show-data-table-section])]
                 [gap :size "20px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "6px" :align-self "center"}}}
                  :label "small" :on-click #(rf/dispatch [:home-page/set-data-table-size small-size])
                  :style {:padding "2px 10px"
                          :background-color (when (= data-table-size small-size)
                                              "whitesmoke")}]
                 [gap :size "10px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "6px" :align-self "center"}}}
                  :label "large"
                  :on-click #(rf/dispatch [:home-page/set-data-table-size large-size])
                  :style {:padding "2px 10px"
                          :background-color (when (= data-table-size large-size)
                                              "whitesmoke")}]]]
     (when show-data-table-section
       [:<>
        [gap :size "5px"]
        [h-split
         :margin "0px"
         :style {:border "1px solid #e7e7e7"
                 :border-radius "4px"}
         :parts {:splitter {:style {:background-color "#eee"}}}
         :split-is-px? true
         :initial-split "1250px"
         :splitter-size "10px"
         :panel-1 [box
                   :width "2000px"
                   :class "smalldot"
                   :child [data-table iteration cluster-selected
                           {:height data-table-size
                            :width "1250px"}]]
         :panel-2 [box
                   :style {:margin-top "10px"
                           :margin-left "40px"}
                   :child [select-plot iteration]]]

        [gap :size "20px"]])]))

