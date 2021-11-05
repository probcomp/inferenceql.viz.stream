(ns inferenceql.viz.stream.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.stream.db :as db]
            [inferenceql.viz.events.interceptors :refer [event-interceptors]]
            [inferenceql.viz.stream.store :refer [xcat-models]]
            [inferenceql.viz.stream.model.xcat-util :refer [columns-in-view]]))

(rf/reg-event-db
 :app/initialize-db
 event-interceptors
 (fn [_ _]
   (db/default-db)))

(rf/reg-sub
  :app/model
  :<-[:control/iteration]
  (fn [iteration _]
    (nth xcat-models iteration)))

(rf/reg-sub
  :app/cols-in-view
  :<-[:app/model]
  :<-[:control/cluster-selected]
  (fn [[model cluster-selected] _]
    (set (columns-in-view model (:view-id cluster-selected)))))