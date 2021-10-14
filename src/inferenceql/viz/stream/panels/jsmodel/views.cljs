(ns inferenceql.viz.stream.panels.jsmodel.views
  (:require [re-com.core :refer [border v-box title button gap]]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [clojure.string :refer [replace]]
            [goog.string :refer [format] :as gstring]
            ["highlight.js/lib/core" :as yarn-hljs]
            ["highlight.js/lib/languages/javascript" :as yarn-hljs-js]
            [hickory.core]
            [hickory.select :as s]
            [hickory.zip]
            [hickory.render]
            [clojure.zip :as zip]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [cljstache.core :refer [render]]
            [inferenceql.viz.panels.jsmodel.multimix :as multimix]
            [inferenceql.viz.config :refer [config]]
            [inferenceql.viz.stream.store :refer [mmix-models]]))

;; We are using the minimal version of highlight.js where
;; every language used has to be registered individually.
(.registerLanguage yarn-hljs "javascript" yarn-hljs-js)

(defn add-cluster-spans
  "Takes html of highlighted js-program and returns hiccup with cluster if-statements clickable."
  [highlighted-js-text cluster-selected]
  (let [highlighted-js-text (str "<span>" highlighted-js-text "</span>")
        hiccup (->> (hickory.core/parse-fragment highlighted-js-text)
                    (map hickory.core/as-hiccup)
                    (first))
        hiccup-zipper (hickory.zip/hiccup-zip hiccup)

        ;_ (.log js/console :foo (zip/node hiccup-zipper))

        ;; Returns the view-id of the view function that contains `loc`, the start of a cluster
        ;; if-statement.
        view-id (fn [loc]
                  (loop [l loc]
                    (cond
                      (nil? l)
                      nil

                      (= (take 2 (zip/node l)) [:span {:class "hljs-function"}])
                      (let [func-name (-> l
                                          zip/down zip/right
                                          zip/right zip/down
                                          zip/node)]
                        (-> (re-matches #"view_(\d+)" func-name)
                            second
                            edn/read-string))

                      :else
                      (recur (zip/left l)))))


        cluster-parent-ok? (fn [loc]
                             (when loc
                               (let [;; Get aspects of our parent node for checking.
                                     parent (some-> loc zip/up zip/node)
                                     [_p-tag p-attrs] parent
                                     p-classes (:class p-attrs)
                                     ;; Ensure p-classes is a seq of strings (class names).
                                     p-classes (if (string? p-classes) [p-classes] p-classes)]
                                ;; Checks that our parent does not have class
                                ;; "cluster-clickable".
                                (not-any? #{"cluster-clickable"} p-classes))))

        cluster-context-ok? (fn [loc]
                              (when loc
                                (let [node (zip/node loc)
                                      ;; Get aspects of the right 2 nodes for checking.
                                      [r1 r2] (zip/rights loc)
                                      [r1-tag r1-attr r1-content] r1]
                                  (and (= node " (cluster_id == ")
                                       (= [:span {:class "hljs-number"}] [r1-tag r1-attr])
                                       (number? (edn/read-string r1-content))
                                       (= r2 ") {\n    ")))))

        ;; Returns true if `loc` represents the start of an if-statement for a cluster.
        ;; We also check that this if-statement in not nested in a span with class
        ;; "cluster-clickable". This would mean we have edited it before.
        cluster-if-statement? (fn [loc]
                                (let [node (zip/node loc)]
                                  (and (= node [:span {:class "hljs-keyword"} "if"])
                                       (cluster-context-ok? (some-> loc zip/right))
                                       (cluster-parent-ok? loc))))

        cluster-else-if-statement? (fn [loc]
                                     (let [node (zip/node loc)
                                           [r1 r2] (take 2 (zip/rights loc))]
                                       (and (= node [:span {:class "hljs-keyword"} "else"])
                                            (= r1 " ")
                                            (= r2 [:span {:class "hljs-keyword"} "if"])
                                            (cluster-context-ok? (some-> loc zip/right
                                                                         zip/right zip/right))
                                            (cluster-parent-ok? loc))))


        ;; Returns the cluster-id from a `loc` representing the start of a cluster if-statement.
        cluster-id (fn [loc]
                     (let [[_ r2 _] (take 3 (zip/rights loc))
                           [_ _ r2-content] r2]
                       (edn/read-string r2-content)))

        remove-until (fn [loc endings]
                       (loop [l loc acc []]
                         (let [n (zip/node l)]
                           (if (endings n)
                             [(zip/remove l) (conj acc n)]
                             (recur (zip/next (zip/remove l))
                                    (conj acc n))))))

        ;; This function edits the current zipper `loc` if needed otherwise it returns
        ;; the current `loc` un-edited.
        fix-node (fn [loc]
                   (let [node (zip/node loc)]
                     (cond
                       (or (cluster-if-statement? loc) #_(cluster-else-if-statement? loc))
                       (let [cluster-id (cluster-id loc)
                             view-id (view-id loc)
                             current {:cluster-id cluster-id :view-id view-id}
                             current-selected (= current cluster-selected)

                             cluster-endings #{"])\n    };\n  }\n}\n\n"
                                               "])\n    };\n  } "}
                             [new-loc targets] (remove-until loc cluster-endings)]
                         (-> new-loc
                             (zip/insert-right (into [:span {:class ["cluster-clickable"
                                                                     (when current-selected "cluster-selected")]
                                                             :onClick #(rf/dispatch [:control/select-cluster current])}]
                                                     targets))))

                       (string? node)
                       (-> (zip/edit loc gstring/unescapeEntities))

                       :else
                       loc)))]
    ;; Loop through all nodes, fixing as needed.
    (loop [loc hiccup-zipper]
      (if (zip/end? loc)
        (zip/root loc)
        (recur (zip/next (fix-node loc)))))))

(defn highlight
  "Returns html of js-text highlighted with highlight.js"
  [js-text]
  (.-value (.highlight yarn-hljs js-text #js {"language" "js"})))

(defn js-clickable-clusters
  "A reagent component that highlights js-code and makes clusters clickable."
  [js-code cluster-selected]
  ;; Returns hiccup.
  (-> js-code highlight (add-cluster-spans cluster-selected)))

(defn js-code-block
  "Reagent component that display of Javascript code with syntax highlighting.
  Args: `js-code` -- (string) The Javascript source code to display."
  [js-code cluster-selected]
  (let [dom-nodes (r/atom {})]
    (r/create-class
     {:display-name "js-model-code"

      :component-did-mount
      (fn [this]
        (.addEventListener (:code-elem @dom-nodes)
                           "click"
                           (fn [event]
                             (if (= (.-target event) (:code-elem @dom-nodes))
                               (rf/dispatch [:control/clear-cluster-selection])))))

      :reagent-render
      (fn [js-code cluster-selected]
        [:pre#program-display
         [:code {:ref #(swap! dom-nodes assoc :code-elem %)}
          [js-clickable-clusters js-code cluster-selected]]])})))

(defn js-model
  "Reagent component for js-model."
  [iteration cluster-selected]
  (let [mmix-model (nth mmix-models iteration)
        js-model-text (render (:js-model-template config)
                              (multimix/template-data mmix-model))]
    [js-code-block js-model-text cluster-selected]))
