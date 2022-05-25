(ns inferenceql.viz.stream.panels.viz.inferences
  "Code related to producing a vega-lite spec for the inferences plot."
  (:require [inferenceql.viz.stream.store :refer [schema]]
            [inferenceql.viz.stream.panels.viz.samples :refer [ranges options-count top-options]]
            [inferenceql.viz.stream.panels.viz.util :refer [vl5-schema]]))

(defn vega-type [schema col]
  (let [iql-type (get schema col)
        vega-type-map {:numerical "quantitative"
                       :nominal "nominal"
                       :ignore "nominal"}]
    (get vega-type-map iql-type)))

(defn dimensions [c1-type c2-type]
  (case [c1-type c2-type]
    ["quantitative" "quantitative"]
    {:width 250 :height 250}

    ["quantitative" "nominal"]
    {:width 400 :height {:step 20}}

    ["nominal" "quantitative"]
    {:width {:step 20} :height 400}

    ["nominal" "nominal"]
    {:width {:step 20} :height {:step 20}}))

(defn inference-plot [n-cats [c1 c2]]
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
                    [c1 c2]))

        ;; Now deal with c1-c2 whether they have been reversed or not.
        c1-type (vega-type schema c1)
        c2-type (vega-type schema c2)
        spec {:mark {:type "rect" :tooltip true}
              :width (-> (dimensions c1-type c2-type) :width)
              :height (-> (dimensions c1-type c2-type) :height)
              :transform []
              :encoding {:x {:bin (case c1-type
                                    "nominal" false
                                    "quantitative" {:maxbins 50
                                                    :extent (get ranges c1)})
                             :field (name c1),
                             :type c1-type
                             :axis {:labelAngle 89}}
                         :y {:bin (case c2-type
                                    "nominal" false
                                    "quantitative" {:maxbins 50
                                                    :extent (get ranges c2)})
                             :field (name c2),
                             :type c2-type}
                         :color {:aggregate "count",
                                 :type "quantitative"
                                 :legend nil}}}]
    (let [c1-options (take n-cats (get top-options c1))
          c2-options (take n-cats (get top-options c2))]
      (cond-> spec
        (= c1-type "nominal") (update-in [:transform] conj {:filter {:field c1 :oneOf c1-options}})
        (= c1-type "nominal") (assoc-in [:encoding :x :axis :titleLimit] (* (count c1-options) 19))
        (= c2-type "nominal") (update-in [:transform] conj {:filter {:field c2 :oneOf c2-options}})
        (= c2-type "nominal") (assoc-in [:encoding :y :axis :titleLimit] (* (count c2-options) 22))))))

(defn make-section [num-columns col-space n-cats column-pairs]
  (when (seq column-pairs)
    {:concat (for [pair column-pairs]
               (inference-plot n-cats pair))
     :columns num-columns
     :spacing {:column col-space :row 50}
     :resolve {:scale {:color "independent"}}}))

(defn spec
  [columns n-cats num-columns]
  (let [columns (sort columns)
        column-pairs (for [x columns
                           y columns
                           :while (not= x y)]
                       [x y])
        pair-groups (group-by #(set (map (fn [col] (vega-type schema col))
                                         %))
                              column-pairs)]
    {:$schema vl5-schema
     :autosize {:resize true}
     :vconcat (remove nil? [(make-section num-columns
                                          50
                                          n-cats
                                          (get pair-groups #{"quantitative"}))
                            (make-section num-columns
                                          100
                                          n-cats
                                          (get pair-groups #{"quantitative" "nominal"}))
                            (make-section num-columns
                                          100
                                          n-cats
                                          (get pair-groups #{"nominal"}))])
     :spacing 100
     :data {:name "rows"}
     :config {:countTitle "Count"
              :axisY {:minExtent 10}
              :view {:stroke "transparent"}}
     :resolve {:scale {:color "independent"}}}))


