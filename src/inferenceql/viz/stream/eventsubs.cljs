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

