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

;; Cluster simulation plots.

(rf/reg-sub
  :control/show-cluster-simulation-plots
  (fn [db _]
    (get-in db [:control-panel :show-cluster-simulation-plots])))

(rf/reg-event-db
  :control/set-cluster-simulation-plots
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:control-panel :show-cluster-simulation-plots] new-val)))
