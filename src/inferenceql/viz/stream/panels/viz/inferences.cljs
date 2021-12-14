(ns inferenceql.viz.stream.panels.viz.inferences
  "Code related to producing a vega-lite spec for the inferences plot."
  (:require [cljs-bean.core :refer [->clj]]
            [goog.string :refer [format]]
            [vega-embed$vega :as vega]
            [goog.object]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.stream.panels.viz.util :refer [filtering-summary
                                                            obs-data-color virtual-data-color
                                                            unselected-color vega-type-fn
                                                            vl5-schema]]
            [medley.core :as medley]))

(def ranges
  (get-in config [:settings :numerical_ranges]))

(defn inference-plot [[c1 c2]]
  {
   #_:transform #_[{:filter
                    {:and
                     [{:field "IMDB Rating", :valid true}
                      {:field "Rotten Tomatoes Rating", :valid true}]}}],
   ;; TODO use ranges
   :mark "rect",
   :width 300,
   :height 300,
   :encoding {:x {:bin {:maxbins 50}, :field (name c1), :type "quantitative"},
              :y {:bin {:maxbins 50}, :field (name c2), :type "quantitative"},
              :color {:aggregate "count", :type "quantitative"}},
   :config {:view {:stroke "transparent"}}})

(defn spec
  [column-pairs num-columns]
  (let []
    (.log js/console :check-me column-pairs)
    {:$schema vl5-schema
     :autosize {:resize true}
     :columns num-columns
     :concat (for [pair column-pairs]
               (inference-plot pair))
     :spacing 100
     :data {:name "rows"}
     :config {:countTitle "Count"
              :axisY {:minExtent 10}}
     :resolve {:legend {:size "independent"
                        :color "independent"}
               :scale {:color "independent"}}}))


