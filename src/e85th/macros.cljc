(ns e85th.macros
  (:require [e85th.macros.util :as util]))

(defmacro defsetter
  "Generates a function with name `name` which can be invoked with a java obj and a map.
  The function body is essentially a series of `if-some` statements that calls the
  relevant setters if the value in the map is not `nil`. The map's keys are used
  to determine which setter to call. The order of the setters called is controlled
  by the order of the `:setters` vector."
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
             ;; if-some allows a false value to be set for a property
             (let [k-sym (-> k clojure.core/name symbol)]
               `(if-some [~k-sym (get ~map-nm ~k)]
                  (~setter ~obj-nm ~k-sym))))]
     (let [setters    (map normalize setters)
           ks         (map first setters)
           obj        (vary-meta (util/auto-gensym "obj") assoc :tag type)
           m          (util/auto-gensym "m")
           if-forms   (map (partial if-setter-form obj m) setters)
           attr-map   (cond-> {:tag type}
                        doc-string (assoc :doc doc-string))]
       `(defn ~name
          ~attr-map
          [~obj ~m]
          ~@if-forms
          ~obj)))))
