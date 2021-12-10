(ns inferenceql.viz.stream.db
  "The one and only database for the re-frame app."
  (:require [clojure.spec.alpha :as s]
            [clojure.set]
            [inferenceql.viz.config :refer [config]]))

(def starting-cols
  "Columns incorporated at the start of the sequence of model iterations."
  (let [num-cols (get-in config [:transitions :columns-at-iter 0])
        col-ordering (get-in config [:transitions :column-ordering])]
    (take num-cols col-ordering)))

;;; Primary DB spec.

(defn default-db
  "When the application starts, this will be the value put in `app-db`."
  []
  {:app {:page [:home-page]}
   :control-panel {:iteration 0}

   :home-page {:show-data-table-section true
               :show-ensemble-section true
               :data-table-size "400px"

               :show-plot-options false
               :marginal-types #{:1D}
               :col-selection (set starting-cols)

               :show-ensemble-options false
               :mi-bounds {:min 0 :max 10}
               :mi-threshold 3}

   :model-page {:cluster-selected nil
                :cluster-selected-click-count nil
                :cluster-selected-y-offset nil
                :show-cluster-simulation-plots false}})


;; Specs

(s/def ::db (s/keys :req-un [::app
                             ::control-panel
                             ::home-page
                             ::model-page]))

(s/def ::app any?)
(s/def ::control-panel any?)
(s/def ::home-page any?)
(s/def ::model-page any?)


(comment
  (s/def ::control-panel (s/keys :req-un [::iteration
                                          ::col-selection
                                          ::marginal-types
                                          ::show-plot-options
                                          ::mi-threshold]
                                 :opt-un [::cluster-selected
                                          ::cluster-selected-click-count]))

  (s/def ::iteration integer?)
  (s/def ::col-selection set?)
  (s/def ::marginal-types #(and (set? %)
                                (clojure.set/subset? % #{:1D :2D})))
  (s/def ::show-plot-options boolean?)
  (s/def ::mi-threshold number?)
  (s/def ::show-cluster-simulation-plots boolean?)

  (s/def ::cluster-selected (s/keys :req-un [::cluster-id ::view-id]))
  (s/def ::cluster-selected-click-count integer?)
  (s/def ::cluster-id integer?)
  (s/def ::view-id integer?))
