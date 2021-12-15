(ns inferenceql.viz.stream.panels.viz.inferences
  "Code related to producing a vega-lite spec for the inferences plot."
  (:require [cljs-bean.core :refer [->clj]]
            [goog.string :refer [format]]
            [vega-embed$vega :as vega]
            [clojure.math.combinatorics :refer [combinations]]
            [inferenceql.viz.stream.store :refer [schema]]
            [goog.object]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.stream.panels.viz.util :refer [filtering-summary
                                                            obs-data-color virtual-data-color
                                                            unselected-color vega-type-fn
                                                            vl5-schema]]))


(def ranges
  (get-in config [:settings :numerical_ranges]))

(defn vega-type [schema col]
  (let [iql-type (get schema col)
        vega-type-map {:numerical "quantitative"
                       :nominal "nominal"
                       :ignore "nominal"}]
    (get vega-type-map iql-type)))

(defn inference-plot [[c1 c2]]
  (let [c1-type (vega-type schema c1)
        c2-type (vega-type schema c2)]
    {:mark {:type "rect" :tooltip true}
     :width (case c1-type
              "nominal" {:step 14},
              "quantitative" 240)
     :height (case c2-type
              "nominal" {:step 14},
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
                        :legend nil}}}))

(defn spec
  [columns num-columns]
  (let [column-pairs (for [x columns
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


