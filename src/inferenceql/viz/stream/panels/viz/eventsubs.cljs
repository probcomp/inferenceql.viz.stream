(ns inferenceql.viz.stream.panels.viz.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.events.interceptors :refer [event-interceptors]]
            [inferenceql.viz.stream.panels.viz.dashboard :as dashboard]
            [inferenceql.viz.stream.store :refer [schema observed-samples]]))

(def ranges {:BMI [-15 65]})

(rf/reg-sub
  :viz/spec
  :<- [:control/col-selection]
  :<- [:control/marginal-types]
  :<- [:app/cols-in-view]
  (fn [[col-selection marginal-types cols-in-view] _]
    (.log js/console :in-viz-spec--------)
    (let [cols (or (seq cols-in-view) col-selection)]
      (dashboard/spec observed-samples schema cols 10 marginal-types ranges))))

