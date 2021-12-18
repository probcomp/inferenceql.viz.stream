(ns inferenceql.viz.stream.page.home.ensemble
  (:require [re-com.core :refer [v-box h-box box gap title info-button hyperlink label slider]]
            [re-frame.core :as rf]
            [goog.string :refer [format]]
            [inferenceql.viz.stream.panels.jsmodel.views :refer [tiny-js-model tiny-js-model-placeholder]]
            [inferenceql.viz.stream.panels.viz.views :refer [mi-plot]]
            [inferenceql.viz.stream.store :refer [mutual-info]]))

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

(defn ensemble-options []
  (let [show-ensemble-options @(rf/subscribe [:home-page/show-ensemble-options])
        mi-threshold @(rf/subscribe [:home-page/mi-threshold])
        mi-bounds @(rf/subscribe [:home-page/mi-bounds])]
    (when show-ensemble-options
      [v-box
       :children [[gap :size "5px"]
                  [h-box
                   :children [[label :label "Dep-prob exponential weighting:"]
                              [gap :size "10px"]
                              [box
                               :style {:padding-top "3px"}
                               :child [slider
                                       :width "200px"
                                       :min (:min mi-bounds)
                                       :max (:max mi-bounds)
                                       :step (/ (- (:max mi-bounds) (:min mi-bounds))
                                                100)
                                       :model mi-threshold
                                       :on-change (fn [val] (rf/dispatch [:home-page/set-mi-threshold val]))]]
                              [gap :size "10px"]
                              [label :label (format "%.5f" mi-threshold)]]]
                  [gap :size "10px"]]])))

(defn ensemble-section []
  (let [iteration @(rf/subscribe [:control/iteration])
        show-ensemble-section @(rf/subscribe [:home-page/show-ensemble-section])]
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
                  :label "hide" :on-click #(rf/dispatch [:home-page/toggle-show-ensemble-section])]
                 [gap :size "20px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "8px" :align-self "center"}}}
                  :label "options" :on-click #(rf/dispatch [:home-page/toggle-ensemble-options])]]]
     [ensemble-options]
     (when show-ensemble-section
       [:<>
        [h-box
         :children [[model-summaries iteration]
                    [gap :size "50px"]
                    [v-box
                     :children [[title
                                 :level :level3 :label "Pairwise predictive relationships"
                                 :parts {:wrapper {:style {:align-self "center"}}}
                                 :style {:text-align "center"}]
                                [mi-plot mutual-info iteration]]]]]
        [gap :size "20px"]])]))
