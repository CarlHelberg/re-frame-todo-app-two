(ns todo-app-two.app
  (:require [todo-app-two.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
