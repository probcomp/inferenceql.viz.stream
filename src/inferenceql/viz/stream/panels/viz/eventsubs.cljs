(ns inferenceql.viz.stream.panels.viz.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.stream.interceptors :refer [event-interceptors]]
            [inferenceql.viz.stream.panels.viz.dashboard :as dashboard]
            [inferenceql.viz.stream.panels.viz.inferences :as inferences]
            [inferenceql.viz.stream.store :refer [schema]]
            [inferenceql.viz.stream.panels.viz.samples :refer [observed-samples]]))


(rf/reg-sub
  :viz/select-vs-simulate-spec
  :<- [:home-page/col-selection]
  :<- [:home-page/marginal-types]
  (fn [[col-selection marginal-types] _]
      (dashboard/spec observed-samples schema col-selection 10 marginal-types 3)))

(rf/reg-sub
  :viz/cluster-simulate-spec
  :<- [:model-page/cols-in-view]
  (fn [cols-in-view _]
    (let [marginal-types (if (> (count cols-in-view) 1)
                           #{:2D}
                           #{:1D})]
      (dashboard/spec observed-samples schema cols-in-view 10 marginal-types 2))))

(rf/reg-sub
  :viz/inferences-spec
  :<- [:home-page/inferences-col-selection]
  (fn [inferences-columns  _]
    (inferences/spec inferences-columns 3)))
