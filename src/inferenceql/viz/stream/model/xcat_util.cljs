(ns inferenceql.viz.stream.model.xcat-util
  "Defs for extracting information from XCat records."
  (:require [medley.core :as medley]
            [clojure.edn :as edn]
            [clojure.set]
            [inferenceql.inference.gpm :as gpm]
            [inferenceql.inference.gpm.column :refer [crosscat-simulate]]))

(def range-1 (drop 1 (range)))

(defn xcat-view-id-map
  "Returns map from js-program view-id (int) to xcat view-id (keyword)."
  [xcat]
  (let [view-names (keys (get-in xcat [:latents :counts]))
        view-number (fn [view-name]
                      (-> (re-matches #"view_(\d+)" (name view-name))
                          second
                          edn/read-string))]
    (zipmap range-1 (sort-by view-number view-names))))

(defn xcat-cluster-id-map
  "Returns map from js-program cluster-id (int) to xcat cluster-id (keyword).
  Cluster id is specific to xact view view-id (keyword)."
  [xcat view-name]
  (let [view (get-in xcat [:views view-name])
        cluster-names (keys (get-in view [:latents :counts]))
        cluster-number (fn [cluster-name]
                         (-> (re-matches #"cluster_(\d+)" (name cluster-name))
                             second
                             edn/read-string))]
    (zipmap range-1 (sort-by cluster-number cluster-names))))

(defn columns-in-view [xcat view-id]
  (when view-id
    (let [view-id (get (xcat-view-id-map xcat)
                       view-id)
          view (get-in xcat [:views view-id])]
      (keys (:columns view)))))

(defn columns-in-model [xcat]
  (let [views (-> xcat :views vals)
        columns-in-view (fn [view] (-> view :columns keys))]
    (mapcat columns-in-view views)))

(defn numerical-columns [xcat]
  "Returns columns names with type :gaussian in `xcat`."
  (let [col-gpms (->> xcat :views vals (map :columns) (apply merge))
        col-types (medley/map-vals :stattype col-gpms)]
    (keys (medley/filter-vals #{:gaussian} col-types))))

(defn rows-in-view-cluster [xcat view-id cluster-id]
  (let [view-map (xcat-view-id-map xcat)
        ;; View-name-kw used in xcat model.
        view-id (view-map view-id)
        cluster-map (xcat-cluster-id-map xcat view-id)
        ;; Cluster-id used in xcat model.
        cluster-id (cluster-map cluster-id)

        view (get-in xcat [:views view-id])
        cluster-assignments (get-in view [:latents :y])]
    (->> (filter #(= cluster-id (val %)) cluster-assignments)
       (map first))))

(defn all-row-assignments [xcat]
  (let [view-map (xcat-view-id-map xcat)
        inv-view-map (zipmap (vals view-map)
                             (map #(keyword (str "view_" %)) (keys view-map)))

        view-cluster-assignemnts (->> (:views xcat)
                                   ;; Get the cluster assignments.
                                   (medley/map-vals #(get-in % [:latents :y]))
                                   ;; Sort the map of cluster assignments.
                                   (medley/map-vals #(sort-by first %))
                                   ;; Get just the cluster names. Drop row numbers.
                                   (medley/map-vals #(map second %))
                                   ;; Remap view-id and cluster-ids.
                                   (medley/map-kv (fn [view-name cluster-assignments]
                                                    (let [cluster-map (xcat-cluster-id-map xcat view-name)
                                                          inv-cluster-map (zipmap (vals cluster-map)
                                                                                  (keys cluster-map))]
                                                      [(inv-view-map view-name)
                                                       (map inv-cluster-map cluster-assignments)]))))]
    view-cluster-assignemnts
    ;; Expand the lists of cluster assigments into assignments for each row.
    (apply map (fn [& a] (zipmap (keys view-cluster-assignemnts) a))
           (vals view-cluster-assignemnts))))

(defn sample-xcat
  "Samples all targets from an XCat gpm. `n` is the number of samples."
  ([xcat n]
   (sample-xcat xcat n {}))
  ([xcat n {:keys [allow-neg]}]
   (let [targets (gpm/variables xcat)
         simulate #(gpm/simulate xcat targets {})

         ;; Returns a function that checks `row` for negative values in the keys
         ;; provided as `cols`.
         neg-check (fn [cols]
                     (fn [row]
                       (some neg? (vals (select-keys row cols)))))

         neg-row? (cond
                    (= allow-neg nil) (neg-check (numerical-columns xcat))
                    (= allow-neg false) (neg-check (numerical-columns xcat))
                    (= allow-neg true) (constantly false)
                    (seq allow-neg) (let [cols-to-check
                                          (clojure.set/difference
                                           (set (numerical-columns xcat))
                                           (set (map keyword allow-neg)))]
                                      (neg-check cols-to-check)))]
     (take n (remove neg-row? (repeatedly simulate))))))

(defn sample-xcat-cluster
  "Samples all targets from a cluster in an XCat gpm. `n` is the number of samples."
  ([xcat view-id cluster-id n]
   (sample-xcat-cluster xcat view-id cluster-id n {}))
  ([xcat view-id cluster-id n {:keys [allow-neg]}]
   (let [column-gpms (-> xcat :views view-id :columns)
         simulate (fn [] (medley/map-vals #(crosscat-simulate % cluster-id)
                                          column-gpms))

         ;; Returns a function that checks `row` for negative values in the keys
         ;; provided as `cols`.
         neg-check (fn [cols]
                     (fn [row]
                       (some neg? (vals (select-keys row cols)))))

         neg-row? (cond
                    (= allow-neg nil) (neg-check (numerical-columns xcat))
                    (= allow-neg false) (neg-check (numerical-columns xcat))
                    (= allow-neg true) (constantly false)
                    (seq allow-neg) (let [cols-to-check
                                          (clojure.set/difference
                                           (set (numerical-columns xcat))
                                           (set (map keyword allow-neg)))]
                                      (neg-check cols-to-check)))]
     (take n (remove neg-row? (repeatedly simulate))))))


