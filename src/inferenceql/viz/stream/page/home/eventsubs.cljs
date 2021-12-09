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

;; Data table size

(rf/reg-sub
  :home-page/data-table-size
  (fn [db _]
    (get-in db [:home-page :data-table-size])))

(rf/reg-event-db
  :home-page/set-data-table-size
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:home-page :data-table-size] new-val)))
