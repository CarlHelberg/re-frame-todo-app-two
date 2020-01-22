(ns todo-app-two.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [todo-app-two.core-test]))

(doo-tests 'todo-app-two.core-test)

