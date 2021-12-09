(ns inferenceql.viz.stream.db
  (:require [clojure.spec.alpha :as s]
            [clojure.set]
            [inferenceql.viz.stream.store :refer [starting-cols]]
            [inferenceql.viz.config :refer [config]]))

;;; Primary DB spec.

(defn default-db
  "When the application starts, this will be the value put in `app-db`."
  []
  {:app {:page [:home-page]}

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
                :show-cluster-simulation-plots false}



   :control-panel {:iteration 0
                   :slider-label (or (get-in config [:settings :slider_text])
                                     "Iteration: ")}})

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
