(ns e85th.macros-test
  (:require [e85th.macros :as sut :refer [defsetter]]
            [e85th.macros.util :as util]
            [clojure.string :as str]
            [e85th.macros.specs]
            #?(:clj [expectations.clojure.test :refer [expect]])
            #?(:clj [clojure.test :refer [deftest]]
               :cljs [cljs.test :as t :include-macros true]))
  #?(:clj (:import [java.util Date])))


(set! *warn-on-reflection* true)

;; TODO: Make this work w/ ClojureScript
;; expands to: (defn date-setter "date setter" [obj m] ,,,)
(defsetter date-setter
  "date setter"
  {:type    Date
   :setters [:year "month" "date" [:my-hours "setHours"] :minutes :seconds]})

(deftest date-setter-test
  (let [dt (Date.)]
    (date-setter dt
                 {:year 99
                  "month" 11
                  "date" 31
                  :my-hours 23
                  :minutes 59
                  :seconds 59})
    (expect 99 (.getYear dt))
    (expect 11 (.getMonth dt))
    (expect 31 (.getDate dt))
    (expect 23 (.getHours dt))
    (expect 59 (.getMinutes dt))
    (expect 59 (.getSeconds dt))

    (date-setter dt {:year 88 :seconds 51})
    (expect 88 (.getYear dt))
    (expect 51 (.getSeconds dt))

    ;; month and other properties stay as they were
    (expect 11 (.getMonth dt))


    ;; empty maps don't have any effect
    (date-setter dt {})
    (expect 88 (.getYear dt))

    (date-setter dt {:ignored-key "omg"})
    (expect 88 (.getYear dt))

    ;; nil in this case will cause NPE
    (expect NullPointerException (date-setter dt {:year nil}))))
