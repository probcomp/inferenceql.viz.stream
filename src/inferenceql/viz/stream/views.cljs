(ns inferenceql.viz.stream.views
  (:require [re-com.core :refer [v-box h-box box gap title info-button line hyperlink]]
            [re-frame.core :as rf]
            [inferenceql.viz.stream.panels.control.views :as control]
            [inferenceql.viz.stream.panels.jsmodel.views :refer [js-model tiny-js-model]]
            [inferenceql.viz.stream.panels.table.views :refer [data-table]]
            [inferenceql.viz.stream.panels.viz.views :refer [mi-plot select-vs-simulate-plot]]
            [inferenceql.viz.stream.store :refer [mutual-info]]
            [reagent.core :as reagent]))

(defn model-summaries [iteration]
  [v-box
   :children [[title
               :level :level3
               :label "Model summaries"]
              [h-box
               :style {:border-radius "4px"
                       :overflow "hidden"}
               :children [[tiny-js-model 0 iteration]
                          [line]
                          [tiny-js-model 1 iteration]
                          [line]
                          [tiny-js-model 2 iteration]]]]])

(defn home-page
  []
  (let [iteration @(rf/subscribe [:control/iteration])
        plot-type @(rf/subscribe [:control/plot-type])
        cluster-selected @(rf/subscribe [:control/cluster-selected])
        cluster-selected-click-count @(rf/subscribe [:control/cluster-selected-click-count])]
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
                [box :width "1390px"
                     :child [data-table iteration cluster-selected {}]]
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
                [gap :size "20px"]

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
                 cluster-selected-click-count iteration]]]))


(defn model-page [model-num]
  (reagent/create-class
   {:component-did-mount
    (fn [this]
      (.scrollTo js/window 0 0))

    :reagent-render
    (fn [model-num]
      (let [iteration @(rf/subscribe [:control/iteration])
            cluster-selected @(rf/subscribe [:control/cluster-selected])
            cluster-selected-click-count @(rf/subscribe [:control/cluster-selected-click-count])
            cluster-selected-y-offset @(rf/subscribe [:control/cluster-selected-y-offset])]
        [v-box
         :margin "30px 20px"
         :children [[hyperlink
                     :label "« Back"
                     :style {:font-size "24px"}
                     :on-click #(do
                                  (rf/dispatch [:app/set-page [:home-page]])
                                  (rf/dispatch [:control/clear-cluster-selection]))]
                    [gap :size "20px"]
                    [h-box
                     :children [[box
                                 :width "640px"
                                 :style {:overflow "hidden"
                                         :border-radius "4px"}
                                 :child [js-model model-num iteration cluster-selected]]
                                [gap :size "20px"]
                                (when cluster-selected
                                  (let [y-offset (max 0 (- cluster-selected-y-offset 10))]
                                    [box
                                     :style {:padding-top (str y-offset "px")}
                                     :class "smalldot"
                                     :child [select-vs-simulate-plot cluster-selected
                                             cluster-selected-click-count iteration]]))
                                [gap :size "20px"]
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

