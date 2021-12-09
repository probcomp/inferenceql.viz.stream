(ns inferenceql.viz.stream.views
  (:require [re-com.core :refer [v-box box]]
            [re-frame.core :as rf]
            [inferenceql.viz.stream.panels.control.views :as control]
            [inferenceql.viz.stream.page.home.views :refer [home-page]]
            [inferenceql.viz.stream.page.model.views :refer [model-page]]))

(defn app
  []
  (let [page-vector @(rf/subscribe [:app/page])
        page (first page-vector)]
    [v-box
     :children [[control/iteration-slider-section]
                [box :margin "60px 0px 0px 0px"
                     :child (case page
                              :home-page [home-page]
                              :model-page [model-page (second page-vector)])]]]))

