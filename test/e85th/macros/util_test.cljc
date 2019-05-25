(ns e85th.macros.util-test
  (:require [e85th.macros.util :as sut]
            [clojure.string :as str]
            #?(:clj [expectations.clojure.test :refer [expect]])
            #?(:clj [clojure.test :refer [deftest]]
               :cljs [cljs.test :as t :include-macros true])))

(set! *warn-on-reflection* true)

(deftest gensym-test
  (let [sym (sut/auto-gensym)
        sym-name (name sym)]
    (expect symbol? sym)
    (expect true (str/starts-with? sym-name "G__"))
    (expect true (str/ends-with? sym-name "__auto__")))

  (let [sym (sut/auto-gensym "foo")
        sym-name (name sym)]
    (expect symbol? sym)
    (expect true (str/starts-with? sym-name "foo__"))
    (expect true (str/ends-with? sym-name "__auto__"))))


(deftest auto-gensym?-test
  (expect true (sut/auto-gensym? 'obj__22802__auto__))
  (expect false (sut/auto-gensym? "obj__22802_auto__"))
  (expect false (sut/auto-gensym? :obj__22802__auto__))
  (expect false (sut/auto-gensym? {})))


(deftest rename-auto-gensym-test
  (expect 'obj__1__auto__ (sut/rename-auto-gensym 'obj__22802__auto__ 1)))


(deftest auto-gensym-index-test
  (expect '{a__22802__auto__ a__0__auto__
            b__24725__auto__ b__1__auto__}
          (sut/auto-gensym-index '[1 a__22802__auto__ :hello b__24725__auto__ "done"]))

  (expect '{a__22802__auto__ a__0__auto__
            b__24725__auto__ b__1__auto__}
          (sut/auto-gensym-index '[1 a__22802__auto__ :hello b__24725__auto__ "done"
                               b__24725__auto__ :<-repeated]))

  (let [ans (sut/auto-gensym-index `(let [a# 1
                                          b# 2
                                          c# (+ 1 2)]
                                      c#))]
    (expect #{'a__0__auto__
              'b__1__auto__
              'c__2__auto__}
            (-> ans vals set))))


(deftest java-setter-symobl-test
  (expect '.setProjectId (sut/java-setter-method-symbol :project-id))
  (expect '.setFoo (sut/java-setter-method-symbol :foo)))

(deftest let-bindings-test
  (expect '[a (clojure.core/get my-map :a)
            b (clojure.core/get my-map "b")]
          (sut/let-bindings 'my-map [:a "b"])))
