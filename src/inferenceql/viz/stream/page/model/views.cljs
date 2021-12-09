(ns inferenceql.viz.stream.page.model.views
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
         :children [[hyperlink
                     :label "« Back"
                     :style {:font-size "16px"
                             :margin "0px"}
                     :on-click #(do
                                  (rf/dispatch [:app/set-page [:home-page]])
                                  (rf/dispatch [:control/clear-cluster-selection]))]
                    [title :level :level2
                     :label "Ensemble detail"]
                    [gap :size "10px"]

                    [h-box
                     :children [[v-box
                                 :width "640px"
                                 :style {:overflow "hidden"}
                                 :children [[box
                                             :style {:align-self "flex-end"
                                                     :position "relative"
                                                     :margin-top "-20px"
                                                     :bottom "-23px"}
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
                                                                        "show simulation plots"]])]]
                                            [horizontal-tabs
                                             :style {:margin-bottom "-1px"}
                                             :model model-num
                                             :tabs [{:id 0 :label "Program 1"}
                                                    {:id 1 :label "Program 2"}
                                                    {:id 2 :label "Program 3"}]
                                             :on-change #(rf/dispatch [:app/set-page [:model-page %]])]
                                            [box
                                             :height "4000px"
                                             :style {:background "#f8f8f8"}
                                             :child [js-model model-num iteration cluster-selected]]]]
                                (if (and cluster-selected show-cluster-simulation-plots)
                                  (let [y-offset (max 0 (- cluster-selected-y-offset 10))]
                                    [:<>
                                     [gap :size "20px"]
                                     [box
                                      :margin "28px 0px 0px 0px"
                                      :class "smalldot"
                                      :style {:padding-top (str y-offset "px")}
                                      :child [cluster-simulate-plot cluster-selected
                                              cluster-selected-click-count iteration]]
                                     [gap :size "20px"]])
                                  [gap :size "30px"])
                                [box
                                 :margin "28px 0px 0px 0px"
                                 :child [data-table iteration cluster-selected {:height "4000px" :width "2000px"}]]]]]]))}))

