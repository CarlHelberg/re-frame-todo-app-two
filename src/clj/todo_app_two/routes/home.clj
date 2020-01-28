(ns todo-app-two.routes.home
  (:require
   [todo-app-two.layout :as layout]
   [clojure.java.io :as io]
   [todo-app-two.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]
   [clojure.data.json :as json]
   [datomic.api :as d]
   [todo-app-two.db.core :as db]))

(defn home-page [request]
  (layout/render request "home.html"))


(defn get-todos
  [_]
(let [DB                (d/db db/conn)
      todos             (d/q '[:find ?e ?text ?done
                              :where
                              [?e :todo/done ?done]
                              [?e :todo/text ?text]
                              ] DB)
      convert-for-front-end   (fn [[id text done]] {:text text :done done :id id}) ]
    (-> (map convert-for-front-end todos)
      (json/write-str )
      (response/ok )
      (response/header "Content-Type" "application/json"))))

(defn create-todo
  [request]
  (let [params (:body-params request)]
    @(d/transact db/conn [{:db/id "new"
                           :todo/text (:text params)
                           :todo/done (:done params)}])
  (-> (response/accepted "{}")
      (response/header "Content-Type" "application/json"))))


(defn update-todo
  [input]
  (let [params      (:body-params input)
        todo-id     (first params)
        text        (second params)]
    (if (> (count params) 1)
        @(d/transact db/conn [{ :db/id todo-id
                                :todo/text text
                                :todo/done (nth params 2)}])
        @(d/transact db/conn [[:db.fn/retractEntity todo-id]]))
    (-> (response/accepted "{}")
        (response/header "Content-Type" "application/json"))))

(defn home-routes []
  [""
   {:middleware [
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]
   ["/todos" {:get  #(get-todos %)
              :post #(create-todo %)}]
   ["/todos/:todo-id" {:post #(update-todo %)}]
   ])