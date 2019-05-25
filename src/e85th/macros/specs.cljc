(ns e85th.macros.specs
  (:require [clojure.spec.alpha :as s]
            [e85th.macros.util :as util]))


(defn assert-spec
  "Asserts that `data` is valid per `spec` otherwise throws an exception."
  [spec data]
  (when-not (s/valid? spec data)
    (throw (ex-info "Spec not satisfied."
                    {:spec spec
                     :error (s/explain-str spec data)}))))


(s/def ::macro-name symbol?)
(s/def ::doc-string (s/nilable string?))

(s/def :defsetter/type symbol?)
(s/def :defsetter/setter-key util/name?)

(s/def :defsetter/setter-tuple (s/tuple :defsetter/setter-key util/name?))

(s/def :defsetter/setter (s/or :key :defsetter/setter-key :tuple :defsetter/setter-tuple))
(s/def :defsetter/setters (s/coll-of :defsetter/setter))
(s/def :defsetter/options (s/keys :req-un [:defsetter/type :defsetter/setters]))

(s/fdef e85th.macros/defsetter
  :args (s/or
         :without-doc (s/cat :name ::macro-name :opts :defsetter/options)
         :with-doc    (s/cat :name ::macro-name :doc ::doc-string :opts :defsetter/options))
  :ret list?)


(s/valid? :defsetter/setter-key :key)
(s/valid? :defsetter/setter-key 'key)
(s/valid? :defsetter/setter-key "key")

(s/valid? :defsetter/setter-tuple [:key "setKey"])
(s/valid? :defsetter/setter [:key "setKey"])
(s/valid? :defsetter/setter :key)
(s/valid? :defsetter/setter 'key)
(s/valid? :defsetter/setter "key")
(s/valid? :defsetter/setters [:a 'b ["c" "omg"]])

(s/valid? :defsetter/options {:type 'foo
                              :setters [:a 'b ["c" "omg"]]})

(s/valid?
 :defsetter/options
 {:type    'Date
  :setters [:year 'month "date" :hours :minutes :seconds]})


;; Don't do keys cause could be strs or syms extract and bind each item


(util/name? 'month)
(s/explain-str )
