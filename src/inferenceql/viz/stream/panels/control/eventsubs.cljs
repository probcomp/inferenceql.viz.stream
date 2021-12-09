(ns inferenceql.viz.stream.panels.control.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.events.interceptors :refer [event-interceptors]]))

;; Iteration.

(rf/reg-sub
  :control/iteration
  (fn [db _]
    (get-in db [:control-panel :iteration])))

(rf/reg-event-db
  :control/set-iteration
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:control-panel :iteration] new-val)))

;; Slider label.

(rf/reg-sub
  :control/slider-label
  (fn [db _]
    (get-in db [:control-panel :slider-label])))
