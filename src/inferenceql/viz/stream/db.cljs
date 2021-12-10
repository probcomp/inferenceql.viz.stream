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
               ;;--
               :show-plot-options false
               :marginal-types #{:1D}
               :col-selection (set starting-cols)
               ;;--
               :show-ensemble-options false
               :mi-bounds {:min 0 :max 10}
               :mi-threshold 3}
   :model-page {:show-cluster-simulation-plots false
                :cluster-selected-click-count 0}})

(s/def ::db (s/keys :req-un [::app
                             ::control-panel
                             ::home-page
                             ::model-page]))

;;; App section.

(s/def ::app (s/keys :req-un [::page]))
(s/def ::page (s/cat :page-name #{:home-page :model-page}
                     :model-num (s/? integer?)))

;;; Control-panel section.

(s/def ::control-panel (s/keys :req-un [::iteration]))
(s/def ::iteration integer?)

;;; Home-page section.

(s/def ::home-page (s/keys :req-un [::show-data-table-section
                                    ::show-ensemble-section
                                    ::data-table-size
                                    ;;--
                                    ::show-plot-options
                                    ::marginal-types
                                    ::col-selection
                                    ;;--
                                    ::show-ensemble-options
                                    ::mi-bounds
                                    ::mi-threshold]))
(s/def ::show-data-table-section boolean?)
(s/def ::show-ensemble-section boolean?)
(s/def ::data-table-size string?)
(s/def ::show-plot-options boolean?)
(s/def ::marginal-types #(clojure.set/subset? % #{:1D :2D}))
(s/def ::col-selection (s/coll-of ::column-name :kind set?))
(s/def ::column-name keyword?)
(s/def ::show-ensemble-options boolean?)
(s/def ::mi-bounds (s/keys :req-un [::min ::max]))
(s/def ::min number?)
(s/def ::max number?)
(s/def ::mi-threshold number?)

;;; Model-page section.

(s/def ::model-page (s/keys :req-un [::show-cluster-simulation-plots]
                            :opt-un [::cluster-selected
                                     ::cluster-selected-click-count
                                     ::cluster-selected-y-offset]))
(s/def ::cluster-selected (s/keys :req-un [::model-id ::view-id ::cluster-id]))
(s/def ::model-id integer?)
(s/def ::view-id integer?)
(s/def ::cluster-id integer?)
(s/def ::cluster-selected-click-count integer?)
(s/def ::cluster-selected-y-offset number?)
(s/def ::show-cluster-simulation-plots boolean?)
