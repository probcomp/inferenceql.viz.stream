(ns inferenceql.viz.stream.panels.jsmodel.views
  (:require [clojure.zip :as z]
            [clojure.edn :as edn]
            [cljstache.core :refer [render]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [hickory.core]
            [hickory.zip]
            [hickory.render]
            [goog.string :as gstring]
            [goog.style :as gstyle]
            [goog.dom :as gdom]
            ["highlight.js/lib/core" :as yarn-hljs]
            ["highlight.js/lib/languages/javascript" :as yarn-hljs-js]
            [inferenceql.viz.panels.jsmodel.multimix :as multimix]
            [inferenceql.viz.stream.store :refer [js-programs column-dependencies]]
            [re-com.core :refer [v-box h-box box gap title info-button line hyperlink popover-tooltip]]
            [medley.core :as medley]
            [clojure.string :as string]))

;; We are using the minimal version of highlight.js where
;; every language used has to be registered individually.
(.registerLanguage yarn-hljs "javascript" yarn-hljs-js)

(defn add-cluster-spans
  "Takes html of highlighted js-program and returns hiccup with cluster if-statements clickable."
  [highlighted-js-text model-num cluster-selected]
  (let [highlighted-js-text (str "<span>" highlighted-js-text "</span>")
        hiccup (->> (hickory.core/parse-fragment highlighted-js-text)
                    (map hickory.core/as-hiccup)
                    (first))

        ;; Checks that a cluster section is not nested in a span with class "cluster-clickable".
        ;; This would mean we have edited it before and already made it clickable.
        cluster-parent-ok? (fn [loc]
                             (when loc
                               (let [;; Get aspects of our parent node for checking.
                                     parent (some-> loc z/up z/node)
                                     [_p-tag p-attrs] parent
                                     p-classes (:class p-attrs)
                                     ;; Ensure p-classes is a seq of strings (class names).
                                     p-classes (if (string? p-classes) [p-classes] p-classes)]
                                 ;; Checks that our parent does not have class
                                 ;; "cluster-clickable".
                                 (not-any? #{"cluster-clickable"} p-classes))))

        ;; Checks the three nodes to the right of `loc` to see if they represent the start of
        ;; a cluster section of the program.
        cluster-context-ok? (fn [loc]
                              (when loc
                                (let [[r1 r2 r3] (z/rights loc)
                                      [r2-tag r2-attr r2-content] r2]
                                  (and (= r1 " (cluster_id == ")
                                       (= [:span {:class "hljs-number"}] [r2-tag r2-attr])
                                       (number? (edn/read-string r2-content))
                                       (= r3 ") {\n    ")))))

        ;; Returns true if `loc` represents the start of a cluster section of the programe.
        cluster-start? (fn [loc]
                         (let [node (z/node loc)]
                           (and (or (= node [:span {:class "hljs-keyword"} "if"])
                                    (= node [:span {:class "hljs-keyword"} "else if"]))
                                (cluster-context-ok? loc)
                                (cluster-parent-ok? loc))))

        ;; Returns the cluster-id (number) given `loc` which is the starting loc of a cluster
        ;; section of the program.
        cluster-id (fn [loc]
                     (let [[_ r2 _] (take 3 (z/rights loc))
                           [_ _ r2-content] r2]
                       (edn/read-string r2-content)))

        ;; Moves left of from `loc` until a function node is encountered. Extracts view-id from
        ;; the function name as a number.
        view-id (fn [loc]
                  (loop [l loc]
                    (cond
                      (nil? l) nil

                      (= (take 2 (z/node l)) [:span {:class "hljs-function"}])
                      (let [func-name (-> l z/down z/right z/right z/down z/node)]
                        (-> (re-matches #"view_(\d+)" func-name) second edn/read-string))

                      :else (recur (z/left l)))))

        ;; Remove nodes starting from `loc` until reaching one of the nodes in set `endings`.
        ;; Returns `[loc acc]` where `loc` is the position of the first node encountered in
        ;; `endings` and `acc` is the list of nodes removed to reach the ending and also the
        ;; ending node itself, which does not get removed.
        remove-until (fn [loc endings]
                       (loop [l loc acc []]
                         (let [n (z/node l)]
                           (if (endings n)
                             [l (conj acc n)]
                             (recur (z/next (z/remove l)) (conj acc n))))))

        ;; This functions wraps all nodes that correspond to a cluster into a span that
        ;; can be clicked.
        wrap-cluster-nodes
        (fn [loc]
          (if (cluster-start? loc)
            (let [cluster-id (cluster-id loc)
                  view-id (view-id loc)
                  current {:model-id model-num :cluster-id cluster-id :view-id view-id}
                  current-selected (= current cluster-selected)

                  cluster-endings #{;; After intermediate cluster, last var categorical.
                                    "])\n    };\n  }\n}\n\n"
                                    ;; After intermediate cluster, last var gaussian.
                                    ")\n    };\n  }\n}\n\n"
                                    ;; After last cluster, last var categorical.
                                    "])\n    };\n  } "
                                    ;; After last cluster, last var gaussian.
                                    ")\n    };\n  } "}
                  [cluster-end-loc cluster-nodes] (remove-until loc cluster-endings)]
              (z/replace cluster-end-loc
                         (into [:span {:class ["cluster-clickable"
                                               (when current-selected "cluster-selected")
                                               (when current-selected "no-select")]
                                       :onClick #(rf/dispatch [:model-page/select-cluster current])}]
                               cluster-nodes)))
            loc))

        ;; Correctly escapes strings in hljs-string nodes, so Reagent displays them correctly.
        fix-hljs-string-nodes (fn [loc]
                                (let [node (z/node loc)
                                      class (get-in node [1 :class])]
                                  (if (= class "hljs-string")
                                    ;; Unescapes the string portion of the hljs-string vector.
                                    (z/edit loc update 2 gstring/unescapeEntities)
                                    loc)))

        ;; Combines three nodes representing else-if into a single node.
        merge-else-if-nodes (fn [loc]
                              (let [node (z/node loc)
                                    [r1 r2] (take 2 (z/rights loc))]
                                (if (and (= node [:span {:class "hljs-keyword"} "else"])
                                         (= r1 " ")
                                         (= r2 [:span {:class "hljs-keyword"} "if"]))
                                  (-> loc
                                      z/remove z/next
                                      z/remove z/next
                                      (z/replace [:span {:class "hljs-keyword"} "else if"]))
                                  loc)))

        ;; Makes zipper from `hiccup` and applies `f` to each child node moving right each time.
        ;; Returns hiccup.
        map-right (fn [hiccup f]
                    (let [zip (-> hiccup hickory.zip/hiccup-zip z/down)]
                      ;; Iterate through all nodes by moving right at each step.
                      (loop [loc zip]
                        (if (nil? (z/right loc))
                          ;; We are at the right-most already. Fix current node and return root.
                          (z/root (f loc))
                          ;; Recur case.
                          (recur (z/right (f loc)))))))]
    (-> hiccup
        (map-right fix-hljs-string-nodes)
        (map-right merge-else-if-nodes)
        (map-right wrap-cluster-nodes))))

(defn highlight
  "Returns html of js-text highlighted with highlight.js"
  [js-text]
  (.-value (.highlight yarn-hljs js-text #js {"language" "js"})))

(defn js-clickable-clusters
  "A reagent component that highlights js-code and makes clusters clickable."
  [model-num js-code cluster-selected]
  ;; Returns hiccup.
  (-> js-code highlight (add-cluster-spans model-num cluster-selected)))

(defn js-code-block
  "Reagent component that display of Javascript code with syntax highlighting.
  Args: `js-code` -- (string) The Javascript source code to display."
  [model-num js-code cluster-selected]
  (let [dom-nodes (r/atom {})]
    (r/create-class
     {:display-name "js-model-code"

      :component-did-mount
      (fn [this]
        (.addEventListener (:code-elem @dom-nodes)
                           "click"
                           (fn [event]
                             ;; TODO: Add comment.
                             (when (not= (.-target event) (:code-elem @dom-nodes))
                               (let [clicked-node (.-target event)
                                     cluster-node (gdom/getAncestorByClass clicked-node "cluster-clickable")
                                     pos (gstyle/getRelativePosition cluster-node
                                                                     (:code-elem @dom-nodes))]
                                 (rf/dispatch [:model-page/set-cluster-selected-y-offset (.-y pos)])))
                             ;; TODO: Add comment.
                             (when (= (.-target event) (:code-elem @dom-nodes))
                               (rf/dispatch [:model-page/clear-cluster-selection])))))

      :reagent-render
      (fn [model-num js-code cluster-selected]
        [:pre#program-display
         [:code {:ref #(swap! dom-nodes assoc :code-elem %)}
          [js-clickable-clusters model-num js-code cluster-selected]]])})))

(defn js-model
  "Reagent component for js-model."
  [model-num iteration cluster-selected]
  (let [js-model-text (get-in js-programs [iteration model-num])]
    [js-code-block model-num js-model-text cluster-selected]))

(defn column-grouping-chips
  [column-groupings]
  [v-box
   :class "column-grouping-section"
   :height "150px"
   :style {:font-size "11px"
           :overflow "hidden"}
   :gap "10px"
   :children (for [cg column-groupings]
               (let [cg (map name cg)]
                 [h-box
                  :class "column-grouping"
                  :style {:flex-flow "row wrap"}
                  :children (for [col cg]
                              [box
                               :class "column-chip"
                               :child col])]))])

(defn tiny-js-model-placeholder [num-missing-models]
  (let [show-tooltip (r/atom false)]
    (fn [num-missing-models]
      [v-box
       :width "50px"
       :children [[gap :size "182.78px"]
                  [popover-tooltip
                   :position :right-center
                   :width "200px"
                   :showing? show-tooltip
                   :label [:div
                           {:style {:text-align "left"}}
                           [:span (gstring/format "%s more models not shown here." num-missing-models)]
                           [:br]
                           [:br]
                           [:span (gstring/format (str "All %s models are used to produce the column "
                                                       "dependencies plot and the select vs. simulate "
                                                       "plots below.")
                                                  (+ num-missing-models 3))]]
                   :anchor [:pre {:style {:border "solid #7bb0db"
                                          :border-width "0px 0px 1px 0px"
                                          :border-radius "0px"
                                          :font-size "10px"
                                          :margin "0px"
                                          :height "420px"}
                                  :on-mouse-over #(reset! show-tooltip true)
                                  :on-mouse-out #(reset! show-tooltip false)
                                  :class "tiny-js-model"}]]]])))

(defn tiny-js-model [model-num iteration]
  (let [js-model-text (get-in js-programs [iteration model-num])
        column-groupings (get-in column-dependencies [iteration model-num])
        highlighted-html (highlight js-model-text)]
    [v-box
     :width "275px"
     :children [[title
                 :level :level4
                 :style {:font-size "12px"}
                 :label (gstring/format "Program %s, modeling dependencies" (inc model-num))]
                [gap :size "10px"]
                [column-grouping-chips column-groupings]
                [:pre {:style {:border "solid #7bb0db"
                               :border-width "0px 0px 1px 0px"
                               :border-radius "0px"
                               :margin "0px"}
                       :class "tiny-js-model"}
                 [:div {:style {:font-size "5px"
                                :height "400px"
                                :overflow "hidden"}
                        :onClick #(rf/dispatch [:app/set-page [:model-page model-num]])
                        :dangerouslySetInnerHTML {:__html highlighted-html}}]]]]))

