(ns inferenceql.viz.stream.page.home.data-table
  (:require [re-com.core :refer [v-box h-box box gap title info-button
                                 checkbox line hyperlink popover-tooltip
                                 horizontal-tabs]]
            [re-frame.core :as rf]
            [inferenceql.viz.stream.panels.table.views :refer [data-table]]))

(defn data-table-section []
  (let [iteration @(rf/subscribe [:control/iteration])
        cluster-selected @(rf/subscribe [:control/cluster-selected])
        show-data-table-section @(rf/subscribe [:home-page/show-data-table-section])
        data-table-size @(rf/subscribe [:home-page/data-table-size])
        small-size "400px"
        large-size "1000px"]
    [:<>
     [h-box
      :children [[title :level :level2 :label "Data Table"]
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
        [box :width "1390px"
         :child [data-table iteration cluster-selected {:height data-table-size}]]
        [gap :size "20px"]])]))

