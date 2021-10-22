(ns ^:figwheel-hooks inferenceql.viz.stream.core
  (:require [goog.dom :as dom]
            [reagent.dom :as rdom]))

(enable-console-print!)
(set! *warn-on-infer* true)

(defn ^:after-load render-app
  "Renders the primary reagent component for the app onto the DOM element, #app

  Tagged with :after-load so that figwheel will call this function after every hot-reload."
  []
  (rdom/render [:span "placeholder"] (dom/$ "app")))

(defn ^:export -main
  "The main entry point for the app.

  Called from javascript in resources/index.html on initial page load."
  []
  (render-app))
