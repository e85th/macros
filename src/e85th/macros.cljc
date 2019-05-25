(ns e85th.macros
  (:require [e85th.macros.util :as util]))

(defmacro defsetter
  "Generates a function with name `name` which can be invoked with a java obj and a map.
  The function body is essentially a `cond->` statement that calls the relevant setters if the
  value in the map satisfies `some?`. The map's keys are used to determine which setter to call.
  The order of the setters called is controlled by the order of the `:setters` vector."
  ([name opts]
   `(defsetter ~name nil ~opts))
  ([name doc-string {:keys [type setters] :as opts}]
   ;; normalize returns a tuple of [key setter-symbol]
   ;; [:foo "setCustomFoo"] => [:foo '.setCustomFoo] or :bar-id => [:bar-id '.setBarId]
   (letfn [(normalize [x]
             (if (vector? x)
               [(first x) (->> x second clojure.core/name (str ".") symbol)]
               [x (util/java-setter-method-symbol x)]))
           (if-setter-form [obj-nm map-nm [k setter]]
             ;;----------------------------------------------------
             ;; * Use some to handle boolean false
             ;; * User or to handle non builder style objects, ie setters returning nil
             ;; * If the setter returns a truthy value that is not an instance of
             ;;   the type passed in then you'll likely see an Exception
             (let [k-sym (-> k clojure.core/name symbol)]
               `(if-some [~k-sym (get ~map-nm ~k)]
                  (~setter ~obj-nm ~k-sym))))]
     (let [setters    (map normalize setters)
           ;;ks         (mapv (comp symbol clojure.core/name first) setters)
           ks         (map first setters)
           obj        (vary-meta (util/auto-gensym "obj") assoc :tag type)
           m          (util/auto-gensym "m")
           bindings   (util/let-bindings m ks)
           if-forms   (map (partial if-setter-form obj m) setters)
           attr-map   (cond-> {:tag type}
                        doc-string (assoc :doc doc-string))]
       `(defn ~name
          ~attr-map
          [~obj ~m]
          ~@if-forms
          ~obj)))))
