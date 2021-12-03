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

;; Column selection.

(rf/reg-sub
  :control/col-selection
  (fn [db _]
    (get-in db [:control-panel :col-selection])))

(rf/reg-event-db
  :control/select-cols
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:control-panel :col-selection] new-val)))

;; Plot type.

(rf/reg-sub
  :control/plot-type
  (fn [db _]
    (get-in db [:control-panel :plot-type])))

(rf/reg-event-db
  :control/set-plot-type
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:control-panel :plot-type] new-val)))

;; Marginal types.

(rf/reg-sub
  :control/marginal-types
  (fn [db _]
    (get-in db [:control-panel :marginal-types])))

(rf/reg-event-db
  :control/set-marginal-types
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:control-panel :marginal-types] new-val)))

;; Cluster selected.

(rf/reg-sub
  :control/cluster-selected
  (fn [db _]
    (get-in db [:control-panel :cluster-selected])))

(rf/reg-sub
  :control/cluster-selected-click-count
  (fn [db _]
    (get-in db [:control-panel :cluster-selected-click-count])))

(rf/reg-event-db
  :control/select-cluster
  event-interceptors
  (fn [db [_ new-selection]]
    (-> db
        (assoc-in [:control-panel :cluster-selected] new-selection)
        (update-in [:control-panel :cluster-selected-click-count] fnil inc 1))))

(rf/reg-event-db
  :control/clear-cluster-selection
  event-interceptors
  (fn [db [_]]
    (update db :control-panel dissoc :cluster-selected)))

;; Cluster selected y-offset.

(rf/reg-sub
  :control/cluster-selected-y-offset
  (fn [db _]
    (get-in db [:control-panel :cluster-selected-y-offset])))

(rf/reg-event-db
  :control/set-cluster-selected-y-offset
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:control-panel :cluster-selected-y-offset] new-val)))

;; Show plot options.

(rf/reg-sub
  :control/show-plot-options
  (fn [db _]
    (get-in db [:control-panel :show-plot-options])))

(rf/reg-event-db
  :control/toggle-plot-options
  event-interceptors
  (fn [db [_]]
    (update-in db [:control-panel :show-plot-options] not)))

;; MI threshold.

(rf/reg-sub
  :control/mi-threshold
  (fn [db _]
    (get-in db [:control-panel :mi-threshold])))

(rf/reg-sub
  :control/mi-bounds
  (fn [db _]
    (get-in db [:control-panel :mi-bounds])))

(rf/reg-event-db
  :control/set-mi-threshold
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:control-panel :mi-threshold] new-val)))
