(ns inferenceql.viz.stream.page.home.select-vs-simulate
  (:require [re-com.core :refer [v-box h-box box gap title info-button
                                 checkbox line hyperlink popover-tooltip
                                 selection-list
                                 label
                                 horizontal-tabs]]
            [re-frame.core :as rf]
            [inferenceql.viz.stream.panels.viz.views :refer [select-vs-simulate-plot]]
            [inferenceql.viz.stream.store :refer [schema col-ordering]]))

(def column-list (keep (set (keys schema)) col-ordering))

(defn plot-options []
  (let [col-selection @(rf/subscribe [:home-page/col-selection])
        marginal-types @(rf/subscribe [:home-page/marginal-types])
        show-plot-options @(rf/subscribe [:home-page/show-plot-options])]
    (when show-plot-options
      [v-box
       :padding "0px 0px 0px 0px"
       :margin "0px 0px 0px 0px"
       :children [[v-box
                   :children [[gap :size "10px"]
                              [h-box
                               :children [[label :label "Marginals:"]
                                          [gap :size "10px"]
                                          [box
                                           :style {:padding-top "3px"}
                                           :child [selection-list
                                                   :choices (vec (for [c [:1D :2D]]
                                                                   {:id c :label (name c)}))
                                                   :model marginal-types
                                                   :on-change #(rf/dispatch [:home-page/set-marginal-types %])]]]]
                              [gap :size "10px"]
                              [h-box
                               :children [[label :label "Columns:"]
                                          [gap :size "16px"]
                                          [box
                                           :style {:padding-top "3px"}
                                           :child [selection-list
                                                   :choices (vec (for [c column-list]
                                                                   {:id c :label (name c)}))
                                                   :model col-selection
                                                   :on-change #(rf/dispatch [:home-page/select-cols %])]]]]
                              [gap :size "50px"]]]]])))

(defn select-vs-simulate-section []
  (let [iteration @(rf/subscribe [:control/iteration])]
    [:<>
     [h-box
      :children [[title :level :level2 :label "Select vs. Simulate"]
                 [gap :size "5px"]
                 [info-button
                  :style {:margin-top "8px"}
                  :info "This is the ..."]
                 [gap :size "20px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "8px" :align-self "center"}}}
                  :label "options" :on-click #(rf/dispatch [:home-page/toggle-plot-options])]]]
     [plot-options]
     [gap :size "20px"]
     [select-vs-simulate-plot iteration]]))
