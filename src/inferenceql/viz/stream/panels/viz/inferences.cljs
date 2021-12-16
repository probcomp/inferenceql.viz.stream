(ns inferenceql.viz.stream.panels.viz.inferences
  "Code related to producing a vega-lite spec for the inferences plot."
  (:require [cljs-bean.core :refer [->clj]]
            [goog.string :refer [format]]
            [vega-embed$vega :as vega]
            [clojure.math.combinatorics :refer [combinations]]
            [inferenceql.viz.stream.store :refer [schema]]
            [medley.core :as medley]
            [goog.object]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.stream.panels.viz.util :refer [vl5-schema]]))

(def ranges
  (get-in config [:settings :numerical_ranges]))

(def options-count
  "Map of nominal column name to number of options"
  (->> (get-in config [:transitions :options])
    (medley/map-vals count)))

(defn vega-type [schema col]
  (let [iql-type (get schema col)
        vega-type-map {:numerical "quantitative"
                       :nominal "nominal"
                       :ignore "nominal"}]
    (get vega-type-map iql-type)))

(defn inference-plot [[c1 c2]]
  ;; First we determine if we need to swap the x and y columns.
  (let [col-types (map #(vega-type schema %) [c1 c2])
        [c1 c2] (case col-types
                  ["quantitative" "quantitative"]
                  [c1 c2]

                  ["nominal" "quantitative"]
                  ;; Quantitative should be on the x-axis.
                  [c2 c1]

                  ["quantitative" "nominal"]
                  [c1 c2]

                  ["nominal" "nominal"]
                  ;; Column with fewer categories should be on x-axis.
                  (if (>= (get options-count c1)
                          (get options-count c2))
                    [c2 c1]
                    [c1 c2]))]
    ;; Now deal with c1-c2 whether they have been reversed or not.
    (let [c1-type (vega-type schema c1)
          c2-type (vega-type schema c2)]
      {:mark {:type "rect" :tooltip true}
       :width (case c1-type
                "nominal" {:step 20},
                "quantitative" 240)
       :height (case c2-type
                "nominal" {:step 20},
                "quantitative" 240),
       :encoding {:x {:bin (case c1-type
                             "nominal" false
                             "quantitative" {:maxbins 50
                                             :extent (get ranges c1)})
                      :field (name c1),
                      :type c1-type}
                  :y {:bin (case c2-type
                             "nominal" false
                             "quantitative" {:maxbins 50}),
                                             :extent (get ranges c2)
                      :field (name c2),
                      :type c2-type}
                  :color {:aggregate "count",
                          :type "quantitative"
                          :legend nil}}})))

(defn spec
  [columns num-columns]
  (let [columns (sort columns)
        column-pairs (for [x columns
                           y columns
                           :while (not= x y)]
                       [x y])]
    {:$schema vl5-schema
     :autosize {:resize true}
     :columns num-columns
     :concat (for [pair column-pairs]
               (inference-plot pair))
     :spacing 100
     :data {:name "rows"}
     :config {:countTitle "Count"
              :axisY {:minExtent 10}
              :view {:stroke "transparent"}}
     :resolve {:scale {:color "independent"}}}))


