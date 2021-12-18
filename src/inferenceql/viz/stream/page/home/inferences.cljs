(ns inferenceql.viz.stream.page.home.inferences
  (:require [re-com.core :refer [h-box gap title info-button hyperlink]]
            [re-frame.core :as rf]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.stream.panels.viz.views :refer [inferences-plot]]))

(def column-list (get-in config [:transitions :column-ordering]))

(defn inferences-section []
  (let [iteration @(rf/subscribe [:control/iteration])
        show-inferences-section @(rf/subscribe [:home-page/show-inferences-section])]
    [:<>
     [h-box
      :children [[title :level :level2 :label "Inferences"]
                 [gap :size "5px"]
                 [info-button
                  :style {:margin-top "8px"}
                  :info "This is the ..."]
                 [gap :size "20px"]
                 [hyperlink
                  :parts {:wrapper {:style {:margin-top "8px" :align-self "center"}}}
                  :style {:color (when-not show-inferences-section "darkblue")}
                  :label "hide" :on-click #(rf/dispatch [:home-page/toggle-show-inferences-section])]]]
     (when show-inferences-section
       [inferences-plot iteration])]))
