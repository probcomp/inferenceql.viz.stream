(ns inferenceql.viz.stream.page.model.views
  (:require [re-com.core :refer [v-box h-box box gap title checkbox hyperlink popover-tooltip
                                 horizontal-tabs]]
            [re-frame.core :as rf]
            [inferenceql.viz.stream.panels.jsmodel.views :refer [js-model]]
            [inferenceql.viz.stream.panels.table.views :refer [data-table]]
            [inferenceql.viz.stream.panels.viz.views :refer [cluster-simulate-plot]]
            [reagent.core :as r]))

(defn back-link []
  [hyperlink
   :label "« Back"
   :style {:font-size "16px"
           :margin "0px"}
   :on-click #(do
                (rf/dispatch [:app/set-page [:home-page]])
                (rf/dispatch [:model-page/clear-cluster-selection]))])

(defn program-section [model-num]
  (let [iteration @(rf/subscribe [:control/iteration])
        cluster-selected @(rf/subscribe [:model-page/cluster-selected])
        show-cluster-simulation-plots @(rf/subscribe [:model-page/show-cluster-simulation-plots])]
    [v-box
     :width "640px"
     :style {:overflow "hidden"}
     :children [[box
                 :style {:align-self "flex-end"
                         :position "relative"
                         :margin-top "-20px"
                         :bottom "-23px"}
                 :child [checkbox
                         :model show-cluster-simulation-plots
                         :on-change #(rf/dispatch [:model-page/set-cluster-simulation-plots %])
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
                 :child [js-model model-num iteration cluster-selected]]]]))

(defn cluster-simulation-section []
  (let [iteration @(rf/subscribe [:control/iteration])
        cluster-selected @(rf/subscribe [:model-page/cluster-selected])
        cluster-selected-click-count @(rf/subscribe [:model-page/cluster-selected-click-count])
        cluster-selected-y-offset @(rf/subscribe [:model-page/cluster-selected-y-offset])
        show-cluster-simulation-plots @(rf/subscribe [:model-page/show-cluster-simulation-plots])]
    (if (and cluster-selected show-cluster-simulation-plots)
      (let [y-offset (max 0 (- cluster-selected-y-offset 10))]
        [:<>
         [gap :size "20px"]
         [box
          :margin "28px 0px 0px 0px"
          :class "smalldot"
          :style {:padding-top (str y-offset "px")}
          :child [cluster-simulate-plot cluster-selected cluster-selected-click-count iteration]]
         [gap :size "20px"]])
      [gap :size "30px"])))

(defn data-table-section []
  (let [iteration @(rf/subscribe [:control/iteration])
        cluster-selected @(rf/subscribe [:model-page/cluster-selected])]
    [box
     :margin "28px 0px 0px 0px"
     :child [data-table iteration cluster-selected {:height "4000px" :width "2000px"}]]))

(defn model-page [_model-num]
  (r/create-class
   {:component-did-mount
    (fn [_]
      (.scrollTo js/window 0 0))

    :reagent-render
    (fn [model-num]
      [v-box
       :margin "20px 20px"
       :children [[back-link]
                  [title :level :level2 :label "Ensemble of synthesized probabilistic programs (details)"]
                  [gap :size "10px"]
                  [h-box
                   :children [[program-section model-num]
                              [cluster-simulation-section]
                              [data-table-section]]]]])}))

