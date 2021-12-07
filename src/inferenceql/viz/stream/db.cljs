(ns inferenceql.viz.stream.db
  (:require [clojure.spec.alpha :as s]
            [inferenceql.viz.stream.panels.control.db :as control-panel]))

;;; Primary DB spec.

(s/def ::db (s/keys :req-un [::control-panel/control-panel]))

(defn default-db
  "When the application starts, this will be the value put in `app-db`.
  It consists of keys and values from the general db
  and panel specific dbs all merged together."
  []
  (let [dbs [control-panel/default-db
             {:app {:page [:home-page]
                    :show-data-table-section true
                    :show-ensemble-section true
                    :data-table-size "400px"}}]]
    (apply merge dbs)))
