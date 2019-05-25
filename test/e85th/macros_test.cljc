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

;; Not sure why but for some reason if this expect is in a deftest it fails
;; This expect tests the macro expansion only
(expect
 '(clojure.core/defn foo
    {:tag String
     :doc "foo doc string"}
    [obj__0__auto__ m__1__auto__]
    (clojure.core/if-some [length (clojure.core/get m__1__auto__ :length)]
      (.setLength obj__0__auto__ length))
    (clojure.core/if-some [foo (clojure.core/get m__1__auto__ "foo")]
      (.setFoo obj__0__auto__ foo))
    (clojure.core/if-some [bar (clojure.core/get m__1__auto__ :bar)]
      (.myCustomBarSetter obj__0__auto__ bar))
    obj__0__auto__)

 (->> '(defsetter foo
         "foo doc string"
         {:type    String
          :setters [:length "foo" [:bar "myCustomBarSetter"]]})
      macroexpand-1
      util/rename-auto-gensyms))

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

    ;; nils don't mutate objects
    (date-setter dt {:year nil})
    (expect 88 (.getYear dt))

    ;; empty maps don't have any effect
    (date-setter dt {})
    (expect 88 (.getYear dt))))
