(ns inferenceql.viz.stream.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.stream.db :as db]
            [inferenceql.viz.events.interceptors :refer [event-interceptors]]
            [inferenceql.viz.stream.store :refer [xcat-model]]
            [inferenceql.viz.stream.model.xcat-util :refer [columns-in-view]]))

(rf/reg-event-db
 :app/initialize-db
 event-interceptors
 (fn [_ _]
   (db/default-db)))

(rf/reg-sub
  :app/model
  :<-[:control/iteration]
  :<-[:control/cluster-selected]
  (fn [[iteration cluster-selected] _]
    (when cluster-selected
      (xcat-model iteration (:model-id cluster-selected)))))

(rf/reg-sub
  :app/cols-in-view
  :<-[:app/model]
  :<-[:control/cluster-selected]
  (fn [[model cluster-selected] _]
    (set (columns-in-view model (:view-id cluster-selected)))))

;; Page selection

(rf/reg-sub
  :app/page
  (fn [db _]
    (get-in db [:app :page])))

(rf/reg-event-db
  :app/set-page
  event-interceptors
  (fn [db [_ new-val]]
    (assoc-in db [:app :page] new-val)))

;; Hide/show sections

(rf/reg-sub
  :app/show-data-table-section
  (fn [db _]
    (get-in db [:app :show-data-table-section])))

(rf/reg-event-db
  :app/toggle-show-data-table-section
  event-interceptors
  (fn [db [_]]
    (update-in db [:app :show-data-table-section] not)))

(rf/reg-sub
  :app/show-ensemble-section
  (fn [db _]
    (get-in db [:app :show-ensemble-section])))

(rf/reg-event-db
  :app/toggle-show-ensemble-section
  event-interceptors
  (fn [db [_]]
    (update-in db [:app :show-ensemble-section] not)))
