(ns inferenceql.viz.stream.interceptors
  (:require [re-frame.core :as rf]
            [inferenceql.viz.stream.db :as db]
            [inferenceql.viz.config :as config]
            [inferenceql.viz.events.interceptors :refer [log-name check-spec]]))

(def event-interceptors
  "A default set of event interceptors to use within events across the app."
  (if (get config/config :enable-debug-interceptors false)  ;; App setting for debug level.
    [rf/debug (check-spec ::db/db)]
    [log-name]))
