(ns e85th.macros.util
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [clojure.spec.alpha :as s]))

(defn auto-gensym
  "Uses clojure.core/gensym to produce symbols that correspond to
   auto gensym names."
  ([]
   (auto-gensym "G"))
  ([prefix]
   (let [sym-name (-> prefix gensym name)
         id       (str/replace-first sym-name prefix "")]
     (symbol (str prefix "__" id "__auto__")))))

(defn auto-gensym?
  "Tests if `x` is an auto gensym symbol."
  [x]
  (and (symbol? x)
       (str/ends-with? (name x) "__auto__")))

(defn rename-auto-gensym
  "Useful for tests to have predictable auto gensym names."
  [sym n]
  (-> sym
      name
      (str/replace #"__\d+__auto__$" (str "__" n "__auto__"))
      symbol))


(defn auto-gensym-index
  "Returns a map of symbols to symbols. keys are the symbols occuring
  in `form` and the value is the renamed symbol to be predictable for testing."
  [form]
  (let [counter (volatile! -1)
        index   (volatile! {})
        f       (fn [x]
                  (when (and (auto-gensym? x)
                             (not (get @index x)))
                    (let [n (vswap! counter inc)]
                      (vswap! index assoc x (rename-auto-gensym x n))))
                  ;; only care about side effects return x as is
                  x)]
    (walk/postwalk f form)
    @index))

(defn rename-auto-gensyms
  "Renames autos in form with consistent names useful for tests."
  [form]
  (let [old->new (auto-gensym-index form)]
    (walk/postwalk (fn [x]
                     (cond-> x
                       (contains? old->new x) old->new))
                   form)))

(defn java-setter-method-symbol
  "Turns a string, keyword or symbol
  to a java setter method symbol. ie `:foo-id` to `.setFooId` symbol"
  [x]
  (->> (str/split (name x) #"-")
       (map str/capitalize)
       (cons ".set")
       (str/join "")
       symbol))

(defn java-getter-method-symbol
  "Turns a string, keyword or symbol
  to a java getter method symbol. ie `:foo-id` to `.getFooId` symbol"
  [x]
  (->> (str/split (name x) #"-")
       (map str/capitalize)
       (cons ".get")
       (str/join "")
       symbol))


(defn name?
  "True if input satisfies `keyword?` `string?` or `symbol?` and is not blank."
  [x]
  (and ((some-fn keyword? string? symbol?) x)
       (-> x name str/blank? not)))


(defn let-bindings
  "Generates a list of bindings which can be spliced into a let form.
   (map-let-bindings 'my-map [:a :b])
     =>
    [a (clojure.core/get my-map :a)
     b (clojure.core/get my-map :b)] "
  [m ks]
  (mapcat (fn [k]
            `[~(-> k name symbol) (get ~m ~k)])
          ks))
