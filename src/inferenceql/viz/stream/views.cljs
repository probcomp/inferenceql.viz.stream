(ns inferenceql.viz.stream.views
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


(defn home-page
  []
  (let [iteration @(rf/subscribe [:control/iteration])
        cluster-selected @(rf/subscribe [:control/cluster-selected])
        show-data-table-section @(rf/subscribe [:app/show-data-table-section])
        show-ensemble-section @(rf/subscribe [:app/show-ensemble-section])
        data-table-size @(rf/subscribe [:app/data-table-size])
        small-size "400px"
        large-size "1000px"]
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
                   [gap :size "20px"]])

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
                   [gap :size "20px"]])

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
                             :label "options" :on-click #(rf/dispatch [:control/toggle-plot-options])]]]
                [control/plot-options]
                [gap :size "20px"]
                [select-vs-simulate-plot iteration]]]))


(defn model-page [model-num]
  (r/create-class
   {:component-did-mount
    (fn [this]
      (.scrollTo js/window 0 0))

    :reagent-render
    (fn [model-num]
      (let [iteration @(rf/subscribe [:control/iteration])
            cluster-selected @(rf/subscribe [:control/cluster-selected])
            cluster-selected-click-count @(rf/subscribe [:control/cluster-selected-click-count])
            cluster-selected-y-offset @(rf/subscribe [:control/cluster-selected-y-offset])
            show-cluster-simulation-plots @(rf/subscribe [:control/show-cluster-simulation-plots])]
        [v-box
         :margin "20px 20px"
         :children [[h-box
                     :width "640px"
                     :justify :between
                     :children [[v-box
                                 :children [[hyperlink
                                             :label "« Back"
                                             :style {:font-size "16px"
                                                     :margin "0px"}
                                             :on-click #(do
                                                          (rf/dispatch [:app/set-page [:home-page]])
                                                          (rf/dispatch [:control/clear-cluster-selection]))]
                                            [title :level :level2
                                             :label "Ensemble detail"]
                                            [gap :size "10px"]]]
                                [box
                                 :style {:align-self "flex-end"
                                         :margin-bottom "-22px"}
                                 :child [checkbox
                                         :model show-cluster-simulation-plots
                                         :on-change #(rf/dispatch [:control/set-cluster-simulation-plots %])
                                         :label (let [show-tooltip (r/atom false)]
                                                  [popover-tooltip
                                                   :label (str "This shows cluster simulation plots "
                                                               "whenever a cluster is clicked in the "
                                                               "model (js-program).")
                                                   :position :above-center
                                                   :showing? show-tooltip
                                                   :width "200px"
                                                   :anchor [:div
                                                            {:style {:z-index "10000"}
                                                             :on-mouse-over #(reset! show-tooltip true)
                                                             :on-mouse-out #(reset! show-tooltip false)}
                                                            "show simulation plots"]])]]]]
                    [h-box
                     :children [[v-box
                                 :width "640px"
                                 :style {:overflow "hidden"}
                                 :children [[horizontal-tabs
                                             :style {:margin-bottom "-1px"}
                                             :model model-num
                                             :tabs [{:id 0 :label "Program 1"}
                                                    {:id 1 :label "Program 2"}
                                                    {:id 2 :label "Program 3"}]
                                             :on-change #(rf/dispatch [:app/set-page [:model-page %]])]
                                            [js-model model-num iteration cluster-selected]]]
                                (if (and cluster-selected show-cluster-simulation-plots)
                                  (let [y-offset (max 0 (- cluster-selected-y-offset 10))]
                                    [:<>
                                     [gap :size "20px"]
                                     [box
                                      :style {:padding-top (str y-offset "px")}
                                      :class "smalldot"
                                      :child [cluster-simulate-plot cluster-selected
                                              cluster-selected-click-count iteration]]
                                     [gap :size "20px"]])
                                  [gap :size "30px"])
                                [data-table iteration cluster-selected {:height "4000px" :width "2000px"}]]]]]))}))

(defn app
  []
  (let [page-vector @(rf/subscribe [:app/page])
        page (first page-vector)]
    [v-box
     :children [[box
                 :style {:top 0
                         :left 0
                         :right 0
                         :position "fixed"
                         :padding "20px 20px 20px 20px"
                         :background "white"
                         :z-index 10000
                         :box-shadow (str "rgba(0, 0, 0, 0.05) 0px 1px 2px 0px, "
                                          "rgba(0, 0, 0, 0.05) 0px 1px 4px 0px, "
                                          "rgba(0, 0, 0, 0.05) 0px 2px 8px 0px")}
                 :child [control/iteration]]
                [box :margin "60px 0px 0px 0px"
                     :child (case page
                              :home-page [home-page]
                              :model-page [model-page (second page-vector)])]]]))

