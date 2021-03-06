(ns inferenceql.viz.stream.panels.control.views
  (:require [re-frame.core :as rf]
            [re-com.core :refer [h-box box slider label gap]]
            [inferenceql.viz.config :refer [config]]))

(defn iteration-slider []
  (let [iteration @(rf/subscribe [:control/iteration])
        label-text (get-in config [:settings :slider_text] "Iteration:")]
    [h-box
     :children [[label :label label-text]
                [gap :size "10px"]
                [box
                 :style {:padding-top "3px"}
                 :child [slider
                         :min 0
                         :max (dec (get-in config [:transitions :count]))
                         :model iteration
                         :on-change (fn [iter]
                                      (rf/dispatch [:model-page/clear-cluster-selection])
                                      (rf/dispatch [:control/set-iteration iter]))]]
                [gap :size "10px"]
                [label :label (get-in config [:transitions :num-rows-at-iter iteration])]]]))

(defn iteration-slider-section []
  [box
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
   :child [iteration-slider]])


