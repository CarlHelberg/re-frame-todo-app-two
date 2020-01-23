(ns todo-app-two.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))

;;dispatchers

(rf/reg-event-db
  :navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :route new-match))))

(rf/reg-fx
  :navigate-fx!
  (fn [[k & [params query]]]
    (rfe/push-state k params query)))

(rf/reg-event-fx
  :navigate!
  (fn [_ [_ url-key params query]]
    {:navigate-fx! [url-key params query]}))

(rf/reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

(rf/reg-event-db
  :set-todos
  (fn [db [_ todos]]
    (assoc db :todos todos)))

(rf/reg-event-fx
  :fetch-docs
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/docs"
                  :response-format (ajax/raw-response-format)
                  :on-success       [:set-docs]}}))

(rf/reg-event-fx
  :fetch-todos
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/todos"
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success       [:set-todos]}}))

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-event-fx
  :page/init-home
  (fn [_ _]
    {:dispatch [:fetch-todos]}))

(rf/reg-event-db
  :change-new-todo-text
  (fn [db [event value]]
    (assoc db :new-todo-text value)))

(defn create-todo-from-text
  [text]
  {:todo text :done false})

(rf/reg-event-fx
  :create-new-todo
  (fn [effects _]
    (let [db    (:db effects)
          todos (:todos db)
          text  (:new-todo-text db)
          new-todos (if (nil? todos)
                      [(create-todo-from-text text)]
                      (conj todos (create-todo-from-text text)))]

      {:db (assoc db :todos new-todos)
       :dispatch [:reset-new-todo-text]
       :http-xhrio {:method          :post
                    :uri             "/todos"
                    :params           text
                    :format           (ajax/json-request-format)
                    :response-format  (ajax/json-response-format {:keywords? true})}})))

(rf/reg-event-db
  :reset-new-todo-text
  (fn [db _]
    (assoc db :new-todo-text "")))

(rf/reg-event-fx
  :mark-as-done
  (fn [effects [_ todo-item]]
    (let [db (:db effects)
          new-todo  (map (fn [todo]
                            (if (= (:todo todo) (:todo todo-item))
                              (assoc todo :done true)
                                  todo)) (:todos db))]

      {:db (assoc db :todos new-todo)
       :http-xhrio {:method          :post
                    :uri             (str "/todos/" (:id todo-item))
                    :params           new-todo
                    :format           (ajax/json-request-format)
                    :response-format  (ajax/json-response-format {:keywords? true})}})))

(rf/reg-event-fx
  :mark-as-not-done
  (fn [effects [_ todo-item]]
    (let [db        (:db effects)
         new-todo   (map (fn [todo]
                            (if (= (:todo todo) (:todo todo-item))
                              (assoc todo :done false)
                              todo)) (:todos db))]
      {:db (assoc db :todos new-todo)
       :http-xhrio {:method          :post
                    :uri             (str "/todos/" (:id todo-item))
                    :params           new-todo
                    :format           (ajax/json-request-format)
                    :response-format  (ajax/json-response-format {:keywords? true})}}
      ))
  )

(rf/reg-event-fx
  :edit-todo-text
  (fn [effects [event-name todo-item]]
    (let [db        (:db effects)
          todos     (:todos db)
          text      (:new-todo-text db)
          new-todos (map (fn [todo] (if (= (:todo todo) (:todo todo-item))
                                       (assoc todo :todo text)
                                       todo)) todos)]
    {:db (assoc db :todos new-todos)
     :dispatch [:reset-new-todo-text]})))

(rf/reg-event-db
  :delete-todo
  (fn [db [_ todo-item]]
    (assoc db :todos (remove #(= (:todo todo-item) (:todo %)) (:todos db)))))

;;subscriptions

(rf/reg-sub
  :route
  (fn [db _]
    (-> db :route)))

(rf/reg-sub
  :page-id
  :<- [:route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :page
  :<- [:route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

(rf/reg-sub
  :new-todo-text
  (fn [db _]
    (:new-todo-text db)))

(rf/reg-sub
  :todo-list
  (fn [db _]
    (:todos db)))

(rf/reg-sub
  :completed-todos
  (fn [db _]
    (let [todos (:todos db)]
      (filter :done todos))))

(rf/reg-sub
  :incompleted-todos
  (fn [db _]
    (let [todos (:todos db)]
      (filter #(not (:done %)) todos))))

