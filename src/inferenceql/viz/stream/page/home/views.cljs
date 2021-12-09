(ns inferenceql.viz.stream.page.home.views
  (:require [re-com.core :refer [v-box h-box box gap title info-button
                                 checkbox line hyperlink popover-tooltip
                                 horizontal-tabs]]
            [re-frame.core :as rf]
            [inferenceql.viz.stream.panels.control.views :as control]
            [inferenceql.viz.stream.panels.jsmodel.views :refer [js-model tiny-js-model tiny-js-model-placeholder]]
            [inferenceql.viz.stream.panels.table.views :refer [data-table]]
            [inferenceql.viz.stream.panels.viz.views :refer [mi-plot select-vs-simulate-plot
                                                             cluster-simulate-plot]]
            [inferenceql.viz.stream.store :refer [mutual-info]]
            [reagent.core :as r]))

(defn data-table-section []
  (let [iteration @(rf/subscribe [:control/iteration])
        cluster-selected @(rf/subscribe [:control/cluster-selected])
        show-data-table-section @(rf/subscribe [:app/show-data-table-section])
        data-table-size @(rf/subscribe [:app/data-table-size])
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
                  :label "hide" :on-click #(rf/dispatch [:app/toggle-show-data-table-section])]
                 [gap :size "20px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "6px" :align-self "center"}}}
                  :label "small" :on-click #(rf/dispatch [:app/set-data-table-size small-size])
                  :style {:padding "2px 10px"
                          :background-color (when (= data-table-size small-size)
                                              "whitesmoke")}]
                 [gap :size "10px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "6px" :align-self "center"}}}
                  :label "large"
                  :on-click #(rf/dispatch [:app/set-data-table-size large-size])
                  :style {:padding "2px 10px"
                          :background-color (when (= data-table-size large-size)
                                              "whitesmoke")}]]]
     (when show-data-table-section
       [:<>
        [gap :size "5px"]
        [box :width "1390px"
         :child [data-table iteration cluster-selected {:height data-table-size}]]
        [gap :size "20px"]])]))

(defn model-summaries [iteration]
  [v-box
   :children [[h-box
               :style {:border-radius "4px"
                       :overflow "hidden"}
               :children [[tiny-js-model 0 iteration]
                          [gap :size "5px"]
                          [tiny-js-model 1 iteration]
                          [gap :size "5px"]
                          [tiny-js-model 2 iteration]
                          [gap :size "5px"]
                          [tiny-js-model-placeholder 97]]]]])

(defn ensemble-section []
  (let [iteration @(rf/subscribe [:control/iteration])
        show-ensemble-section @(rf/subscribe [:app/show-ensemble-section])]
    [:<>
     [h-box
      :children [[title :level :level2 :label "Ensemble"]
                 [gap :size "5px"]
                 [info-button
                  :style {:margin-top "8px"}
                  :info "This is the ..."]
                 [gap :size "20px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "8px" :align-self "center"}}}
                  :style {:color (when-not show-ensemble-section "darkblue")}
                  :label "hide" :on-click #(rf/dispatch [:app/toggle-show-ensemble-section])]
                 [gap :size "20px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "8px" :align-self "center"}}}
                  :label "options" :on-click #(rf/dispatch [:control/toggle-ensemble-options])]]]
     [control/ensemble-options]
     (when show-ensemble-section
       [:<>
        [h-box
         :children [[model-summaries iteration]
                    [gap :size "50px"]
                    [v-box
                     :children [[title
                                 :level :level3 :label "Column dependencies"
                                 :parts {:wrapper {:style {:align-self "center"}}}
                                 :style {:text-align "center"}]

                                [mi-plot mutual-info iteration]]]]]
        [gap :size "20px"]])]))

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
                  :label "options" :on-click #(rf/dispatch [:control/toggle-plot-options])]]]
     [control/plot-options]
     [gap :size "20px"]
     [select-vs-simulate-plot iteration]]))

(defn home-page []
  [v-box
   :margin "20px 20px 20px 20px"
   :children [[data-table-section]
              [ensemble-section]
              [select-vs-simulate-section]]])

