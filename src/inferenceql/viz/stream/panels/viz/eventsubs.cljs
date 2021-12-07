(ns inferenceql.viz.stream.panels.viz.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.events.interceptors :refer [event-interceptors]]
            [inferenceql.viz.stream.panels.viz.dashboard :as dashboard]
            [inferenceql.viz.stream.store :refer [schema observed-samples]]))


(rf/reg-sub
  :viz/select-vs-simulate-spec
  :<- [:control/col-selection]
  :<- [:control/marginal-types]
  (fn [[col-selection marginal-types] _]
      (dashboard/spec observed-samples schema col-selection 10 marginal-types 3)))

(rf/reg-sub
  :viz/cluster-simulate-spec
  :<- [:app/cols-in-view]
  (fn [cols-in-view _]
    (let [marginal-types (if (> (count cols-in-view) 1)
                           #{:2D}
                           #{:1D})]
      (dashboard/spec observed-samples schema cols-in-view 10 marginal-types 2))))
