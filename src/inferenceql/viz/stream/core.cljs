(ns ^:figwheel-hooks inferenceql.viz.stream.core
  (:require [goog.dom :as dom]
            [re-frame.core :as rf]
            [reagent.dom :as rdom]
            [inferenceql.viz.stream.views :as views]
            [inferenceql.viz.stream.eventsubs]
            [inferenceql.viz.stream.panels.control.eventsubs]
            [inferenceql.viz.stream.panels.viz.eventsubs]
            [inferenceql.viz.stream.page.home.eventsubs]
            [inferenceql.viz.stream.page.model.eventsubs]))

(enable-console-print!)
(set! *warn-on-infer* true)

(defn ^:after-load render-app
  "Renders the primary reagent component for the app onto the DOM element, #app

  Tagged with :after-load so that figwheel will call this function after every hot-reload."
  []
  (rdom/render [views/app] (dom/$ "app")))

(defn ^:export -main
  "The main entry point for the app.

  Called from javascript in resources/index.html on initial page load."
  []
  ;; We only initialize the app-db on first load. This is so figwheel's hot code reloading does
  ;; not reset the state of the app.
  (rf/dispatch-sync [:app/initialize-db])
  (render-app))
