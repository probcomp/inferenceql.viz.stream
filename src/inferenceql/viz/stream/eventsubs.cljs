(ns inferenceql.viz.stream.eventsubs
  (:require [re-frame.core :as rf]
            [inferenceql.viz.stream.db :as db]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.events.interceptors :refer [event-interceptors]]
            [inferenceql.viz.stream.store :refer [rows xcat-models read-transit-string]]
            [inferenceql.viz.stream.model.xcat-util :refer [columns-in-view]]
            [inferenceql.inference.gpm.crosscat :as xcat]))

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
      (let [t-string (get-in xcat-models [iteration (:model-id cluster-selected)])
            xcat-latents (read-transit-string t-string)
            {:keys [latents spec num-rows]} xcat-latents

            data (zipmap (range) (take num-rows rows))
            options {:options (get-in config [:transitions :options])}]
        (xcat/construct-xcat-from-latents spec latents data options)))))

(rf/reg-sub
  :app/cols-in-view
  :<-[:app/model]
  :<-[:control/cluster-selected]
  (fn [[model cluster-selected] _]
    (set (columns-in-view model (:view-id cluster-selected)))))
