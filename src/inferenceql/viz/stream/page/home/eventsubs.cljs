(ns inferenceql.viz.stream.page.home.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.stream.db :as db]
            [inferenceql.viz.events.interceptors :refer [event-interceptors]]
            [inferenceql.viz.stream.store :refer [xcat-model]]
            [inferenceql.viz.stream.model.xcat-util :refer [columns-in-view]]))

;; Hide/show sections

(rf/reg-sub
  :home-page/show-data-table-section
  (fn [db _]
    (get-in db [:home-page :show-data-table-section])))

(rf/reg-event-db
  :home-page/toggle-show-data-table-section
  event-interceptors
  (fn [db [_]]
    (update-in db [:home-page :show-data-table-section] not)))

(rf/reg-sub
  :home-page/show-ensemble-section
  (fn [db _]
    (get-in db [:home-page :show-ensemble-section])))

(rf/reg-event-db
  :home-page/toggle-show-ensemble-section
  event-interceptors
  (fn [db [_]]
    (update-in db [:home-page :show-ensemble-section] not)))

;;; Data-table section

(rf/reg-sub
  :home-page/data-table-size
  (fn [db _]
    (get-in db [:home-page :data-table-size])))

(rf/reg-event-db
  :home-page/set-data-table-size
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:home-page :data-table-size] new-val)))

;;; Ensemble section.

(rf/reg-sub
  :control/show-ensemble-options
  (fn [db _]
    (get-in db [:control-panel :show-ensemble-options])))

(rf/reg-event-db
  :control/toggle-ensemble-options
  event-interceptors
  (fn [db [_]]
    (update-in db [:control-panel :show-ensemble-options] not)))

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

;; Select-vs-simulate selection.

(rf/reg-sub
  :control/show-plot-options
  (fn [db _]
    (get-in db [:control-panel :show-plot-options])))

(rf/reg-event-db
  :control/toggle-plot-options
  event-interceptors
  (fn [db [_]]
    (update-in db [:control-panel :show-plot-options] not)))

(rf/reg-sub
  :control/col-selection
  (fn [db _]
    (get-in db [:control-panel :col-selection])))

(rf/reg-event-db
  :control/select-cols
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:control-panel :col-selection] new-val)))

(rf/reg-sub
  :control/plot-type
  (fn [db _]
    (get-in db [:control-panel :plot-type])))

(rf/reg-event-db
  :control/set-plot-type
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:control-panel :plot-type] new-val)))

(rf/reg-sub
  :control/marginal-types
  (fn [db _]
    (get-in db [:control-panel :marginal-types])))

(rf/reg-event-db
  :control/set-marginal-types
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:control-panel :marginal-types] new-val)))

