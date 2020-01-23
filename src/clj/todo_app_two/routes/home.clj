(ns todo-app-two.routes.home
  (:require
   [todo-app-two.layout :as layout]
   [clojure.java.io :as io]
   [todo-app-two.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]
   [clojure.data.json :as json]))

(defn home-page [request]
  (layout/render request "home.html"))

(defn get-todos
  [_]
  (-> [{:id 1 :text "first" :done false}
       {:id 2 :text "second" :done false}]
      (json/write-str )
      (response/ok )
      (response/header "Content-Type" "application/json")))
(defn create-todo
  [_]
  (println "create-todo called")
  (response/accepted "/todos"))

(defn update-todo
  [_]
  (println "update-todo called")
  (response/ok "/todos"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]
   ["/todos" {:get  #(get-todos %)
              :post #(create-todo %)}]
   ["/todos/:todo-id" {:post #(update-todo %)}]
   ])

