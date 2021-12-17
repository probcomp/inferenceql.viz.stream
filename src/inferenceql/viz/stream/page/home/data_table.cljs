(ns inferenceql.viz.stream.page.home.data-table
  (:require [re-com.core :refer [v-box h-box box gap title info-button
                                 checkbox line label selection-list hyperlink popover-tooltip
                                 h-split
                                 horizontal-tabs]]
            [re-frame.core :as rf]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.stream.panels.table.views :refer [data-table]]
            [inferenceql.viz.stream.panels.viz.views :refer [select-plot]]))

(def column-list (get-in config [:transitions :column-ordering]))

(defn col-selection []
  (let [col-selection @(rf/subscribe [:home-page/col-selection])
        show-col-selection @(rf/subscribe [:home-page/show-col-selection])]
    (when show-col-selection
      [v-box
       :padding "0px 0px 0px 0px"
       :margin "0px 0px 0px 0px"
       :children [[v-box
                   :children [[gap :size "10px"]
                              [h-box
                               :children [[label :label "Columns to plot:"]
                                          [gap :size "16px"]
                                          [box
                                           :style {:padding-top "3px"}
                                           :child [selection-list
                                                   :choices (vec (for [c column-list]
                                                                   {:id c :label (name c)}))
                                                   :model col-selection
                                                   :on-change #(rf/dispatch [:home-page/select-cols %])]]]]
                              [gap :size "50px"]]]]])))

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
                  :label "small table" :on-click #(rf/dispatch [:home-page/set-data-table-size small-size])
                  :style {:padding "2px 10px"
                          :background-color (when (= data-table-size small-size)
                                              "whitesmoke")}]
                 [gap :size "10px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "6px" :align-self "center"}}}
                  :label "large table"
                  :on-click #(rf/dispatch [:home-page/set-data-table-size large-size])
                  :style {:padding "2px 10px"
                          :background-color (when (= data-table-size large-size)
                                              "whitesmoke")}]
                 [gap :size "20px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "8px" :align-self "center"}}}
                  :label "columns to plot" :on-click #(rf/dispatch [:home-page/toggle-col-selection])]]]
     (when show-data-table-section
       [:<>
        [col-selection]
        [gap :size "5px"]
        [box
         :width "1390px"
         :child [data-table iteration cluster-selected
                 {:height data-table-size}]]
        [gap :size "15px"]
        [select-plot iteration]
        [gap :size "20px"]])]))

