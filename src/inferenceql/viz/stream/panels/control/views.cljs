(ns inferenceql.viz.stream.panels.control.views
  (:require [re-frame.core :as rf]
            [re-com.core :refer [v-box h-box box slider label gap
                                 selection-list radio-button hyperlink]]
            [goog.string :refer [format]]
            [inferenceql.viz.stream.store :refer [schema col-ordering num-transitions]]))

(def column-list (keep (set (keys schema)) col-ordering))

(defn iteration []
  (let [iteration @(rf/subscribe [:control/iteration])]
    [h-box
     :children [[label :label "Number of observed game states"]
                [gap :size "10px"]
                [box
                 :style {:padding-top "3px"}
                 :child [slider
                         :min 0
                         :max (dec num-transitions)
                         :model iteration
                         :on-change (fn [iter]
                                      (rf/dispatch [:control/clear-cluster-selection])
                                      (rf/dispatch [:control/set-iteration iter]))]]
                [gap :size "10px"]
                [label :label (inc iteration)]]]))

(defn ensemble-options []
  (let [show-ensemble-options @(rf/subscribe [:control/show-ensemble-options])
        mi-threshold @(rf/subscribe [:control/mi-threshold])
        mi-bounds @(rf/subscribe [:control/mi-bounds])]
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
                                       :on-change (fn [val] (rf/dispatch [:control/set-mi-threshold val]))]]
                              [gap :size "10px"]
                              [label :label (format "%.5f" mi-threshold)]]]
                  [gap :size "10px"]]])))

(defn plot-options []
  (let [col-selection @(rf/subscribe [:control/col-selection])
        marginal-types @(rf/subscribe [:control/marginal-types])
        show-plot-options @(rf/subscribe [:control/show-plot-options])]
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
                                                   :on-change #(rf/dispatch [:control/set-marginal-types %])]]]]
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
                                                   :on-change #(rf/dispatch [:control/select-cols %])]]]]
                              [gap :size "50px"]]]]])))

