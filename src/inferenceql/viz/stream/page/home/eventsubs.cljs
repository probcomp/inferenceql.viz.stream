(ns inferenceql.viz.stream.page.home.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.stream.interceptors :refer [event-interceptors]]))

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

;;; Inferences section

(rf/reg-sub
  :home-page/show-inferences-plot-options
  (fn [db _]
    (get-in db [:home-page :show-inferences-plot-options])))

(rf/reg-event-db
  :home-page/toggle-inferences-plot-options
  event-interceptors
  (fn [db [_]]
    (update-in db [:home-page :show-inferences-plot-options] not)))

(rf/reg-sub
  :home-page/inferences-col-selection
  (fn [db _]
    (get-in db [:home-page :inferences-col-selection])))

(rf/reg-event-db
  :home-page/inferences-select-cols
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:home-page :inferences-col-selection] new-val)))

;;; Ensemble section.

(rf/reg-sub
  :home-page/show-ensemble-options
  (fn [db _]
    (get-in db [:home-page :show-ensemble-options])))

(rf/reg-event-db
  :home-page/toggle-ensemble-options
  event-interceptors
  (fn [db [_]]
    (update-in db [:home-page :show-ensemble-options] not)))

(rf/reg-sub
  :home-page/mi-threshold
  (fn [db _]
    (get-in db [:home-page :mi-threshold])))

(rf/reg-sub
  :home-page/mi-bounds
  (fn [db _]
    (get-in db [:home-page :mi-bounds])))

(rf/reg-event-db
  :home-page/set-mi-threshold
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:home-page :mi-threshold] new-val)))

;; Select-vs-simulate selection.

(rf/reg-sub
  :home-page/show-plot-options
  (fn [db _]
    (get-in db [:home-page :show-plot-options])))

(rf/reg-event-db
  :home-page/toggle-plot-options
  event-interceptors
  (fn [db [_]]
    (update-in db [:home-page :show-plot-options] not)))

(rf/reg-sub
  :home-page/col-selection
  (fn [db _]
    (get-in db [:home-page :col-selection])))

(rf/reg-event-db
  :home-page/select-cols
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:home-page :col-selection] new-val)))

(rf/reg-sub
  :home-page/marginal-types
  (fn [db _]
    (get-in db [:home-page :marginal-types])))

(rf/reg-event-db
  :home-page/set-marginal-types
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:home-page :marginal-types] new-val)))

