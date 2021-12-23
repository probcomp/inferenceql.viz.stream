(ns inferenceql.viz.stream.panels.viz.dashboard
  "Code related to producing a vega-lite spec for a dashboard."
  (:require [cljs-bean.core :refer [->clj]]
            [vega-embed$vega :as vega]
            [inferenceql.viz.stream.panels.viz.samples :refer [ranges options-count top-options]]
            [inferenceql.viz.stream.panels.viz.util :refer [obs-data-color virtual-data-color
                                                            unselected-color vega-type-fn
                                                            vl5-schema]]))

(defn bin-counts
  "Takes a seq of numerical `data` and `binning`. Returns the number of data points in each bin.
  `binning` is a map with a :start and :stop value for the range of the bins. And a :step value
  for the bin widths."
  [data binning]
  (let [min (:start binning)
        max (:stop binning)
        width (:step binning)
        bin-vals (->> data
                      (filter #(and (<= min %) (<= % max))) ; Val in range.
                      (remove nil?)
                      (map #(quot (- % min) width))) ; Map to bucket number.
        num-bins (quot (- max min) width)

        ;; Numbers equal to `max` map to 1 bucket beyond the final bucket.
        ;; Simply dec the buckets that these numbers map to, so they land in the
        ;; final valid bucket. This is similar to how vega handles binning.
        bin-vals (map (fn [bin-num] (cond-> bin-num (= num-bins bin-num) dec))
                      bin-vals)]
    (reduce (fn [acc bv]
              (update acc bv inc))
            (vec (repeat num-bins 0))
            bin-vals)))

(defn vega-binning
  "Takes `bin-config` which is a map with bin extents: {:extent [min max]}.
  There can also be a key in the same map with the maximum number of bins
  to use: {:maxbins num-bins}.
  Returns vega's preferred binning which is map: {:start x :end y :step z}"
  [bin-config]
  (->clj (vega/bin (clj->js bin-config))))

(defn histogram-quant
  "Generates a vega-lite spec for a histogram.
  `selections` is a collection of maps representing data in selected rows and columns.
  `col` is the key within each map in `selections` that is used to extract data for the histogram.
  `vega-type` is a function that takes a column name and returns an vega datatype."
  [col _samples ranges]
  (let [col-type "quantitative"
        max-bins 30
        bin-config {:extent (get ranges col)
                    :maxbins max-bins}
        max-bin-count 800]
    {:resolve {:scale {:x "shared" :y "shared"}}
     :spacing 0
     :bounds "flush"
     :facet {:field "collection"
             :type "nominal"
             :header {:title nil :labelOrient "bottom" :labelPadding 40}}
     :spec {:layer [{:mark {:type "bar"
                            :color unselected-color
                            :tooltip {:content "data"}
                            :clip true}
                     :params [{:name "brush-all"
                               ;; TODO: is there a way to select based on collection here as well?
                               :select {:type "interval" :encodings ["x"]}}]
                     :encoding {:x {:bin bin-config
                                    :field col
                                    :type col-type
                                    :scale {:domain (get ranges col)}}
                                :y {:aggregate "count"
                                    :type "quantitative"
                                    :scale {:domain [0, max-bin-count]}}}}
                    {:transform [{:filter {:and ["cluster == null"
                                                 {:param "brush-all"}]}}]
                     :mark {:type "bar"
                            :clip true}
                     :encoding {:x {:bin bin-config
                                    :field col
                                    :type col-type
                                    :scale {:domain (get ranges col)}}
                                :y {:aggregate "count"
                                    :type "quantitative"
                                    :scale {:domain [0, max-bin-count]}}
                                :color {:field "collection"
                                        :scale {:domain ["observed", "virtual"]
                                                :range [obs-data-color virtual-data-color]}
                                        :legend {:orient "top"
                                                 :title nil}}}}
                    {:transform [{:filter {:or [{:field "collection" :equal "virtual"}
                                                "datum[view] == cluster"]}}]
                     :mark {:type "bar"
                            :clip true}
                     :encoding {:x {:bin bin-config
                                    :field col
                                    :type col-type
                                    :scale {:domain (get ranges col)}}
                                :y {:aggregate "count"
                                    :type "quantitative"
                                    :scale {:domain [0, max-bin-count]}}
                                :color {:field "collection"
                                        :scale {:domain ["observed", "virtual"]
                                                :range [obs-data-color virtual-data-color]}
                                        :legend {:orient "top"
                                                 :title nil}}}}]}}))

(defn histogram-nom
  "Generates a vega-lite spec for a histogram.
  `selections` is a collection of maps representing data in selected rows and columns.
  `col` is the key within each map in `selections` that is used to extract data for the histogram.
  `vega-type` is a function that takes a column name and returns an vega datatype."
  [col samples]
  (let [col-type "nominal"
        freqs (frequencies (map col samples))
        col-vals (sort (keys freqs))
        ;; If nil is present, move it to the back of the list.
        col-vals (if (some nil? col-vals)
                   (concat (remove nil? col-vals) [nil])
                   col-vals)
        cat-max-count 900
        bin-flag false]
    {:layer [{:mark {:type "point"
                     :shape "circle"
                     :color unselected-color
                     :tooltip {:content "data"}
                     :opacity 0.85}
              :params [{:name "brush-all"
                        :select {:type "point"
                                 :nearest true
                                 :toggle "true"
                                 :on "click[!event.shiftKey]"
                                 :fields [col "collection"]
                                 :clear "dblclick[!event.shiftKey]"}}]
              :encoding {:y {:bin bin-flag
                             :field col
                             :type col-type
                             :axis {:titleAnchor "start" :titleAlign "right" :titlePadding 1}
                             :scale {:domain col-vals}}
                         :x {:aggregate "count"
                             :type "quantitative"
                             :axis {:orient "top"}
                             :scale {:domain [0 cat-max-count]}}
                         ;; TODO: this ordering does not seem to be working.
                         :order {:field "collection"
                                 :scale {:domain ["observed", "virtual"]
                                         :range [1 0]}}
                         :opacity {:condition [{:test {:and [{:field "collection" :equal "observed"}
                                                             {:param "brush-all"}]}
                                                :value 0.75}
                                               {:test {:and [{:field "collection" :equal "virtual"}
                                                             {:param "brush-all"}
                                                             ;; Only color the virtual points when
                                                             ;; a view-cluster is not selected.
                                                             "cluster == null"]}
                                                :value 0.75}
                                               {:test "true"
                                                :value 0}]}
                         :color {:condition [{:test {:and [{:field "collection" :equal "observed"}
                                                           {:param "brush-all"}]}
                                              :value obs-data-color}
                                             {:test {:and [{:field "collection" :equal "virtual"}
                                                           {:param "brush-all"}
                                                           ;; Only color the virtual points when
                                                           ;; a view-cluster is not selected.
                                                           "cluster == null"]}
                                              :value virtual-data-color}
                                             {:test "true"
                                              :value unselected-color}]
                                 :field "collection" ; Dummy field. Never gets used.
                                 :scale {:domain ["observed", "virtual"]
                                         :range [obs-data-color virtual-data-color]}
                                 :legend {:orient "top"
                                          :title nil
                                          :offset 10}}}}
             ;; Observed data ticks.
             {:mark {:type "point"
                     :shape "stroke"
                     :size 80
                     :strokeWidth 2
                     :angle 90
                     :color obs-data-color
                     :opacity 0.8}
              :transform [{:filter {:and [{:field "collection" :equal "observed"}
                                          "cluster != null"
                                          "datum[view] == cluster"]}}]
              :encoding {:y {:bin bin-flag
                             :field col
                             :type col-type
                             :axis {:titleAnchor "start" :titleAlign "right" :titlePadding 1}
                             :scale {:domain col-vals}}
                         :x {:aggregate "count"
                             :type "quantitative"
                             :axis {:orient "top"}
                             :scale {:domain [0 cat-max-count]}}}}
             ;; Virtual data ticks.
             {:mark {:type "point"
                     :shape "triangle-right"
                     :strokeWidth 2
                     :color virtual-data-color
                     :opacity 0.8}
              :transform [{:filter {:and [{:field "collection" :equal "virtual"}
                                          "cluster != null"]}}]

              :encoding {:y {:bin bin-flag
                             :field col
                             :type col-type
                             :axis {:titleAnchor "start" :titleAlign "right" :titlePadding 1}
                             :scale {:domain col-vals}}
                         :x {:aggregate "count"
                             :type "quantitative"
                             :axis {:orient "top"}
                             :scale {:domain [0 cat-max-count]}}}}]}))

(defn- scatter-plot
  "Generates vega-lite spec for a scatter plot.
  Useful for comparing quatitative-quantitative data."
  [col-1 col-2 ranges id-gen legend]
  (let [zoom-control-name (str "zoom-control-" (id-gen))] ; Random id so pan/zoom is independent.
    {:width 250
     :height 250
     :mark {:type "point"
            :tooltip {:content "data"}
            :filled true
            :clip true
            :size {:expr "splomPointSize"}}
     :params [{:name zoom-control-name
               :bind "scales"
               :select {:type "interval"
                        :on "[mousedown[event.shiftKey], window:mouseup] > window:mousemove"
                        :translate "[mousedown[event.shiftKey], window:mouseup] > window:mousemove"
                        :clear "dblclick[event.shiftKey]"
                        :zoom "wheel![event.shiftKey]"}}
              {:name :brush-all
               :select {:type "interval"
                        :on "[mousedown[!event.shiftKey], window:mouseup] > window:mousemove"
                        :translate "[mousedown[!event.shiftKey], window:mouseup] > window:mousemove"
                        :clear "dblclick[!event.shiftKey]"
                        :zoom "wheel![!event.shiftKey]"}}]
     :encoding {:x {:field col-1
                    :type "quantitative"
                    :scale {:domain (get ranges col-1)}
                    :axis {:title col-1}}
                :y {:field col-2
                    :type "quantitative"
                    :scale {:domain (get ranges col-2)}
                    :axis {:minExtent 40
                           :title col-2}}
                :order {:condition [{:test {:and [{:field "collection" :equal "observed"}
                                                  "cluster != null"
                                                  "datum[view] == cluster"]}
                                     :value 10}
                                    {:test {:and [{:field "collection" :equal "observed"}
                                                  {:param "brush-all"}
                                                  "cluster == null"]}
                                     :value 2}
                                    ;; Show the virtual data colored even
                                    ;; when a particular cluster is selected.
                                    {:test {:and [{:field "collection" :equal "virtual"}
                                                  {:param "brush-all"}]}
                                     :value 1}
                                    {:test "true"
                                     :value 0}]
                        :value 0}
                :opacity {:field "collection"
                          :scale {:domain ["observed", "virtual"]
                                  :range [{:expr "splomAlphaObserved"} {:expr "splomAlphaVirtual"}]}
                          :legend nil}
                :color {:condition [{:test {:and [{:field "collection" :equal "observed"}
                                                  "cluster != null"
                                                  "datum[view] == cluster"]}
                                     :value obs-data-color}
                                    {:test {:and [{:field "collection" :equal "observed"}
                                                  {:param "brush-all"}
                                                  "cluster == null"]}
                                     :value obs-data-color}
                                    {:test {:and [{:field "collection" :equal "virtual"}
                                                  {:param "brush-all"}]}
                                     :value virtual-data-color}
                                    {:test "true"
                                     :value unselected-color}]
                        :field "collection" ; Dummy field. Never gets used.
                        :scale {:domain ["observed", "virtual"]
                                :range [obs-data-color virtual-data-color]}
                        :legend (if legend
                                  {:orient "top"
                                   :title nil}
                                  nil)}}}))

(defn- strip-plot-size-helper
  "Returns a vega-lite height/width size.
  Args:
    `col-type` - A vega-lite column type."
  [col-type]
  (case col-type
    "quantitative" 400
    "nominal" {:step 24}))

(defn- strip-plot
  "Generates vega-lite spec for a strip plot.
  Useful for comparing quantitative-nominal data."
  [cols vega-type n-cats _samples ranges id-gen legend]
  (let [zoom-control-name (str "zoom-control-" (id-gen)) ; Random id so pan/zoom is independent.
        ;; NOTE: This is a temporary hack to that forces the x-channel in the plot to be "numerical"
        ;; and the y-channel to be "nominal". The rest of the code remains nuetral to the order so that
        ;; it can be used by the iql-viz query language later regardless of column type order.
        first-col-nominal (= "nominal" (vega-type (first cols)))
        cols-to-draw (cond->> (take 2 cols)
                              first-col-nominal (reverse))

        [x-field y-field] cols-to-draw
        [x-type y-type] (map vega-type cols-to-draw)
        quant-dimension (if (= x-type "quantitative") :x :y)
        [width height] (map (comp strip-plot-size-helper vega-type) cols-to-draw)

        [x-min x-max] (get ranges x-field)
        y-cats (sort (take n-cats (get top-options y-field)))
        title-limit (* (count y-cats) 25)]
    {:resolve {:scale {:x "shared" :y "shared"}}
     :spacing 0
     :bounds "flush"
     :transform [;; Filtering for top categories
                 {:filter {:field y-field :oneOf y-cats}}]
     :width width
     :height height
     :mark {:type "tick"
            :tooltip {:content "data"}
            :color unselected-color}
     :params [{:name zoom-control-name
               :bind "scales"
               :select {:type "interval"
                        :on "[mousedown[event.shiftKey], window:mouseup] > window:mousemove"
                        :translate "[mousedown[event.shiftKey], window:mouseup] > window:mousemove"
                        :clear "dblclick[event.shiftKey]"
                        :encodings [quant-dimension]
                        :zoom "wheel![event.shiftKey]"}}
              {:name "brush-all"
               :select  {:type "point"
                         :nearest true
                         :toggle "true"
                         :on "click[!event.shiftKey]"
                         :resolve "union"
                         :fields [y-field "collection"]
                         :clear "dblclick[!event.shiftKey]"}}]
     :encoding {:y {:field y-field
                    :type y-type
                    :scale {:domain y-cats}
                    :axis {:titleLimit title-limit}}
                :x {:field x-field
                    :type x-type
                    :axis {:grid true :gridDash [2 2]}
                    :scale {:zero false
                            :domain [x-min x-max]}}
                :row {:field "collection"
                      :type "nominal"
                      :header (cond-> {:title nil
                                       :labelPadding 0
                                       :labelLimit title-limit}
                                      ;; If no legend, then no facet labels.
                                      (not legend) (merge {:labels false}))}
                :order {:condition {:param "brush-all"
                                    :value 1}
                        :value 0}
                :color {:condition [{:test {:and [{:field "collection" :equal "observed"}
                                                  "cluster != null"
                                                  "datum[view] == cluster"]}
                                     :value obs-data-color}
                                    {:test {:and [{:field "collection" :equal "observed"}
                                                  {:param "brush-all"}
                                                  "cluster == null"]}
                                     :value obs-data-color}
                                    {:test {:and [{:field "collection" :equal "virtual"}
                                                  {:param "brush-all"}]}
                                     :value virtual-data-color}
                                    {:test "true"
                                     :value unselected-color}]
                        :field "collection" ; Dummy field. Never gets used.
                        :scale {:domain ["observed", "virtual"]
                                :range [obs-data-color virtual-data-color]}
                        :legend (if legend
                                  {:orient "top"
                                   :title nil
                                   :offset 10}
                                  nil)}}}))

(defn- table-bubble-plot
  "Generates vega-lite spec for a table-bubble plot.
  Useful for comparing nominal-nominal data."
  [cols _vega-type n-cats _samples legend]
  (let [[x-field y-field] cols
        x-cats (sort (take n-cats (get top-options x-field)))
        y-cats (sort (take n-cats (get top-options y-field)))
        title-limit (* (count x-cats) 25)]
    {:spacing 0
     :bounds "flush"
     :transform [;; Filtering for top categories
                 {:filter {:field x-field :oneOf x-cats}}
                 {:filter {:field y-field :oneOf y-cats}}]
     :width {:step 20}
     :height {:step 20}
     :resolve {:scale {:size "shared"}}
     :facet {:column {:field "collection"
                      :type "nominal"
                      :header (cond-> {:title nil
                                       :labelPadding 0
                                       :labelLimit title-limit}
                                ;; If no legend, then no facet labels.
                                (not legend) (merge {:labels false}))}}
     :spec {:layer [{:mark {:type "circle"
                            :tooltip {:content "data"}
                            :color unselected-color}
                     :params [{:name "brush-all"
                               :select {:type "point"
                                        :nearest true
                                        :toggle "true"
                                        :on "click[!event.shiftKey]"
                                        :resolve "union"
                                        :fields [y-field x-field "collection"]
                                        :clear "dblclick[!event.shiftKey]"}}]
                     :encoding {:y {:field y-field
                                    :type "nominal"
                                    :axis {:titleOrient "left"
                                           :titleAnchor "center"}
                                    :scale {:domain y-cats}}
                                :x {:field x-field
                                    :type "nominal"
                                    :axis {:orient "bottom"
                                           :titleLimit title-limit}
                                    :scale {:domain x-cats}}
                                :size {:aggregate "count"
                                       :type "quantitative"
                                       :legend nil}
                                :color {:condition [{:test {:and [{:field "collection" :equal "observed"}
                                                                  {:param "brush-all"}
                                                                  "cluster == null"]}
                                                     :value obs-data-color}
                                                    {:test {:and [{:field "collection" :equal "virtual"}
                                                                  {:param "brush-all"}
                                                                  "cluster == null"]}
                                                     :value virtual-data-color}
                                                    {:test "true"
                                                     :value unselected-color}]
                                        :field "collection" ; Dummy field. Never gets used.
                                        :scale {:domain ["observed", "virtual"]
                                                :range [obs-data-color virtual-data-color]}
                                        :legend (if legend
                                                  {:orient "top"
                                                   :title nil
                                                   :offset 10}
                                                  nil)}}}
                    {:mark {:type "circle"}
                     :transform [{:filter {:or [{:field "collection" :equal "virtual"}
                                                {:and [{:field "collection" :equal "observed"}
                                                       "cluster != null"
                                                       "datum[view] == cluster"]}]}}]
                     :encoding {:y {:field y-field
                                    :type "nominal"
                                    :axis {:titleOrient "left"
                                           :titleAnchor "center"}
                                    :scale {:domain y-cats}}
                                :x {:field x-field
                                    :type "nominal"
                                    :axis {:orient "bottom"
                                           :titleLimit title-limit}
                                    :scale {:domain x-cats}}
                                :size {:aggregate "count"
                                       :type "quantitative"
                                       :legend nil}
                                :color {:field "collection"
                                        :scale {:domain ["observed", "virtual"]
                                                :range [obs-data-color virtual-data-color]}}}}]}}))

(defn histogram-quant-section [cols samples ranges num-columns]
  (when (seq cols)
    (let [specs (for [col cols] (histogram-quant col samples ranges))]
      {:concat specs
       :columns num-columns
       :spacing {:column 50 :row 50}})))

(defn histogram-nom-section [cols samples num-columns]
  (when (seq cols)
    (let [specs (for [col cols] (histogram-nom col samples))]
      {:concat specs
       :columns num-columns
       :spacing {:column 100 :row 50}})))

(defn scatter-plot-section [cols ranges id-gen num-columns legend]
  (when (seq cols)
    (let [specs (for [[col-1 col-2] cols] (scatter-plot col-1 col-2 ranges id-gen legend))]
      {:concat specs
       :columns num-columns
       :spacing {:column 50 :row 50}
       :resolve {:legend {:color "shared"}}})))

(defn bubble-plot-section [cols vega-type n-cats samples num-columns legend]
  (when (seq cols)
    (let [specs (for [col-pair cols]
                  (let [[col-1 col-2] col-pair
                        ;; We want the column with fewer options to be on the x-axis.
                        col-pair (if (>= (get options-count col-1)
                                         (get options-count col-2))
                                   [col-2 col-1]
                                   [col-1 col-2])]
                    ;; Produce the bubble plot with the more optionful column on the x-dim.
                    (table-bubble-plot col-pair vega-type n-cats samples legend)))]
      {:concat specs
       :columns num-columns
       :spacing {:column 100 :row 50}})))

(defn strip-plot-section [cols vega-type n-cats samples ranges id-gen num-columns legend]
  (when (seq cols)
    (let [specs (for [col-pair cols]
                  (strip-plot col-pair vega-type n-cats samples ranges id-gen legend))]
      {:concat specs
       :columns num-columns
       :spacing {:column 100 :row 50}})))

(defn top-level-spec [sections]
  {:$schema vl5-schema
   :autosize {:resize true}
   :vconcat sections
   :spacing 100
   :data {:name "rows"}
   :params [;; The current iteration. Filters out data that has not been incorporated
            ;; by this iteration.
            {:name "iter"
             :value 0}
            ;; Observed data with this view-id will be highlighted.
            {:name "view"
             :value nil}
            ;; Observed data with this cluster-id will be highlighted.
            {:name "cluster"
             :value nil}
            {:name "splomAlphaObserved"
             :value 0.7}
            {:name "splomAlphaVirtual"
             :value 0.7}
            {:name "splomPointSize"
             :value 30}
            {:name "showRegression"
             :value false}]
   :transform [{:window [{:op "row_number", :as "row_number"}]
                :groupby ["collection"]}
               {:filter {:field "iter" :lte {:expr "iter"}}}]
   :config {:countTitle "Count"
            :axisY {:minExtent 10}}
   :resolve {:legend {:size "independent"
                      :color "independent"}
             :scale {:color "independent"}}})

(defn spec
  "Produces a vega-lite spec for the QC Dashboard app.
  Paths to samples and schema are required.
  Path to correlation data is optional.
  Category limit is the max number of options to include for categorical variable.
  It can be set to nil for no limit."
  [samples schema cols category-limit marginal-types num-columns legend]
  (when (and (seq marginal-types) (seq cols))
    (let [vega-type (vega-type-fn schema)

          ;; Visualize the columns passed in.
          ;; If not specified, visualize columns found in schema.
          cols (->> (sort cols)
                    (map keyword)
                    (filter vega-type) ; Only keep the columns that we can determine a vega-type for.
                    (sort))

          cols-by-type (group-by vega-type cols)

          ;; Returns unique ids starting at 1.
          id-generator (let [c (atom 0)]
                         (fn []
                           (swap! c inc)
                           @c))

          histograms-quant (histogram-quant-section (get cols-by-type "quantitative") samples ranges num-columns)
          histograms-nom (histogram-nom-section (get cols-by-type "nominal") samples num-columns)

          select-pairs (for [x cols y cols :while (not= x y)] [x y])
          pair-types (group-by #(set (map vega-type %)) select-pairs)

          scatter-plots (scatter-plot-section (get pair-types #{"quantitative"})
                                              ranges
                                              id-generator
                                              num-columns
                                              legend)
          bubble-plots (bubble-plot-section (get pair-types #{"nominal"})
                                            vega-type
                                            category-limit
                                            samples
                                            num-columns
                                            legend)
          strip-plots (strip-plot-section (get pair-types #{"quantitative" "nominal"})
                                          vega-type
                                          category-limit
                                          samples
                                          ranges
                                          id-generator
                                          num-columns
                                          legend)
          sections-1D (remove nil? [histograms-quant histograms-nom])
          sections-2D (remove nil? [scatter-plots strip-plots bubble-plots])
          sections (cond-> []
                           (:1D marginal-types) (concat sections-1D)
                           (:2D marginal-types) (concat sections-2D))]
      (top-level-spec sections))))
