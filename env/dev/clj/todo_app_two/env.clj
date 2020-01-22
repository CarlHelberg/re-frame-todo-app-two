(ns todo-app-two.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [todo-app-two.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[todo-app-two started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[todo-app-two has shut down successfully]=-"))
   :middleware wrap-dev})
