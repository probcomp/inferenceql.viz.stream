(ns inferenceql.viz.stream.config-spec
  "Spec for the applications config file."
  (:require [clojure.spec.alpha :as s]
            [clojure.set]
            [inferenceql.viz.config :refer [config]]))

(s/def ::config (s/keys :req-un [::data
                                 ::schema
                                 ::js-model-template
                                 ::transitions
                                 ::settings]
                        :opt-un [::enable-debug-interceptors]))

;;; Data section.

(s/def ::data (s/coll-of ::csv-row :kind vector?))
(s/def ::csv-row (s/coll-of string? :kind vector?))

;;; Schema section.

(s/def ::schema (s/map-of (s/or :str-column-name string? :kw-column-name keyword?)
                          #{:nominal :numerical}
                          :conform-keys true))
(s/def ::js-model-template string?)

;;; Transitions section.

(s/def ::transitions (s/keys :req-un [::count
                                      ::columns-at-iter
                                      ::num-rows-at-iter
                                      ::column-ordering
                                      ::column-dependencies
                                      ::options]))
(s/def ::count integer?)
(s/def ::columns-at-iter (s/coll-of integer? :kind vector?))
(s/def ::num-rows-at-iter (s/coll-of integer? :kind vector?))
(s/def ::column-ordering (s/coll-of ::column-name :kind vector?))
(s/def ::column-name keyword?)
(s/def ::column-dependencies (s/coll-of ::cd-ensemble :kind vector?))
(s/def ::cd-ensemble (s/coll-of ::cd-model :kind vector?))
(s/def ::cd-model (s/coll-of ::cd-view :kind vector?))
(s/def ::cd-view (s/coll-of ::column-name :kind vector?))
(s/def ::options (s/map-of ::column-name (s/coll-of string? :kind vector?)))

;;; Settings section.

(s/def ::settings (s/keys :req-un [::slider_text
                                   ::allow_negative_simulations
                                   ::numerical_ranges]))
(s/def ::slider_text string?)
(s/def ::allow_negative_simulations (s/or :bool boolean?
                                          :column-names (s/coll-of string?)))
(s/def ::numerical_ranges (s/map-of ::column-name
                                    (s/cat :start number? :end number?)))

;; Optional section.

(s/def ::enable-debug-interceptors boolean?)

