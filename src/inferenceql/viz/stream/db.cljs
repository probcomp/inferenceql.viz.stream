(ns inferenceql.viz.stream.db
  (:require [clojure.spec.alpha :as s]
            [clojure.set]
            [inferenceql.viz.stream.store :refer [mutual-info starting-cols]]))

;;; Primary DB spec.

(defn default-db
  "When the application starts, this will be the value put in `app-db`."
  []
  {:app {:page [:home-page]}

   :home-page {:show-data-table-section true
               :show-ensemble-section true
               :data-table-size "400px"

               :col-selection (set starting-cols)

               :marginal-types #{:1D}
               :show-plot-options false
               :show-ensemble-options false

               :mi-bounds {:min 0 :max 10}
               :mi-threshold 3}

   :control-panel {:iteration 0
                   :show-cluster-simulation-plots false}})

;; Specs

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
(s/def ::view-id integer?)
