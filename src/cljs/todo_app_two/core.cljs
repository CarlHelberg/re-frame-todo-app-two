(ns todo-app-two.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [todo-app-two.ajax :as ajax]
    [todo-app-two.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string])
  (:import goog.History))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page @(rf/subscribe [:page])) :is-active)}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "todo-app-two"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "#/" "Home" :home]
       [nav-link "#/about" "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn todo
  [todo-item]
  [:li (:todo todo-item)
   [:br]
   (if (:done todo-item)
     [:button {:on-click #(rf/dispatch [:mark-as-not-done todo-item])} "Mark as not done"]
     [:button {:on-click #(rf/dispatch [:mark-as-done todo-item])} "Mark as done"])
   [:button {:on-click #(rf/dispatch [:edit-todo-text todo-item])} "Edit"]
   [:button {:on-click #(rf/dispatch [:delete-todo todo-item])} "Delete"]])

(defn todo-list
  [todo-list]
  [:ul
   (map todo todo-list)])

(defn home-page []
  [:section.section>div.container>div.content
   [:textarea {:value @(rf/subscribe [:new-todo-text])
               :placeholder "Write what you need to do!"
               :on-change (fn [event]
                            (rf/dispatch [:change-new-todo-text (-> event .-target .-value)]))}]
   [:button {:on-click #(rf/dispatch [:create-new-todo])} "Add todo"]
   [:br]
   [:h1 "To Do"]
   [:h3 "To edit a todo, enter the text above and click on Edit! (not Add!)"]
   [todo-list @(rf/subscribe [:incompleted-todos])]
   [:br]
   [:h1 "Done"]
   [todo-list @(rf/subscribe [:completed-todos])]])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  (if-let [page @(rf/subscribe [:page])]
    [:div
     [navbar]
     [page]]))

(defn navigate! [match _]
  (rf/dispatch [:navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
