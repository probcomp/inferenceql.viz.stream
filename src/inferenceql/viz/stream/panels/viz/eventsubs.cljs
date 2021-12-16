(ns inferenceql.viz.stream.panels.viz.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.stream.interceptors :refer [event-interceptors]]
            [inferenceql.viz.stream.panels.viz.dashboard :as dashboard]
            [inferenceql.viz.stream.panels.viz.inferences :as inferences]
            [inferenceql.viz.stream.store :refer [schema]]
            [inferenceql.viz.stream.panels.viz.samples :refer [observed-samples]]))

(def num-cats
  "Maximum number of categorical options to show in plots."
  10)

(rf/reg-sub
  :viz/select-vs-simulate-spec
  :<- [:home-page/data-section-col-selection]
  :<- [:home-page/marginal-types]
  (fn [[col-selection marginal-types] _]
      (dashboard/spec observed-samples schema col-selection num-cats marginal-types 3 true)))

(rf/reg-sub
  :viz/cluster-simulate-spec
  :<- [:model-page/cols-in-view]
  (fn [cols-in-view _]
    (let [marginal-types (if (> (count cols-in-view) 1)
                           #{:2D}
                           #{:1D})]
      (dashboard/spec observed-samples schema cols-in-view num-cats marginal-types 2 true))))

(rf/reg-sub
  :viz/select-spec
  :<- [:home-page/data-section-col-selection]
  (fn [col-selection  _]
    (dashboard/spec observed-samples schema col-selection num-cats #{:2D} 3 false)))

(rf/reg-sub
  :viz/inferences-spec
  :<- [:home-page/data-section-col-selection]
  (fn [inferences-columns  _]
    (inferences/spec inferences-columns num-cats 3)))
