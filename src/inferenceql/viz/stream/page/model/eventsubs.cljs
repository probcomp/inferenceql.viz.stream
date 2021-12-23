(ns inferenceql.viz.stream.page.model.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.stream.interceptors :refer [event-interceptors]]
            [inferenceql.viz.stream.store :refer [xcat-model]]
            [inferenceql.viz.stream.model.xcat-util :refer [columns-in-view]]))

;; Cluster selected.

(rf/reg-sub
  :model-page/cluster-selected
  (fn [db _]
    (get-in db [:model-page :cluster-selected])))

(rf/reg-sub
  :model-page/cluster-selected-click-count
  (fn [db _]
    (get-in db [:model-page :cluster-selected-click-count])))

(rf/reg-event-db
  :model-page/select-cluster
  event-interceptors
  (fn [db [_ new-selection]]
    (-> db
        (assoc-in [:model-page :cluster-selected] new-selection)
        (update-in [:model-page :cluster-selected-click-count] inc 1))))

(rf/reg-event-db
  :model-page/clear-cluster-selection
  event-interceptors
  (fn [db [_]]
    (update db :model-page dissoc :cluster-selected)))

;; Cluster selected y-offset.

(rf/reg-sub
  :model-page/cluster-selected-y-offset
  (fn [db _]
    (get-in db [:model-page :cluster-selected-y-offset])))

(rf/reg-event-db
  :model-page/set-cluster-selected-y-offset
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:model-page :cluster-selected-y-offset] new-val)))

;; Cluster simulation plots.

(rf/reg-sub
  :model-page/show-cluster-simulation-plots
  (fn [db _]
    (get-in db [:model-page :show-cluster-simulation-plots])))

(rf/reg-event-db
  :model-page/set-cluster-simulation-plots
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:model-page :show-cluster-simulation-plots] new-val)))

;; Model from cluster selected.

(rf/reg-sub
  :model-page/model
  :<- [:control/iteration]
  :<- [:model-page/cluster-selected]
  (fn [[iteration cluster-selected] _]
    (when cluster-selected
      (xcat-model iteration (:model-id cluster-selected)))))

(rf/reg-sub
  :model-page/cols-in-view
  :<- [:model-page/model]
  :<- [:model-page/cluster-selected]
  (fn [[model cluster-selected] _]
    (set (columns-in-view model (:view-id cluster-selected)))))

