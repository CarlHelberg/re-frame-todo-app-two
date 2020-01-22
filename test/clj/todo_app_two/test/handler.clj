(ns todo-app-two.test.handler
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [todo-app-two.handler :refer :all]
    [todo-app-two.middleware.formats :as formats]
    [muuntaja.core :as m]
    [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'todo-app-two.config/env
                 #'todo-app-two.handler/app-routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))))
