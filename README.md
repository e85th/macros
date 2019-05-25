# Macros and utilities

### defsetter
Builder pattern got you down?

Are you wasting time writing conditional code to invoke setter methods on objects?

Do you want to be more productive and have more time to spend with your family and friends?

Arghh why is everything so hard? [It's all just data!](https://www.youtube.com/watch?v=jlPaby7suOc&t=1513).


Now you can set Java(Script) object properties just by passing an object and a map to a function
defined by `defsetter`.

```clj
(ns eg
  (:require [e85th.macros :refer [defsetter]])
  (:import  [java.util Date]))

(defsetter date-setter
  "date setter"
  {:type    Date
   :setters [:year "month" "date" [:my-hours "setHours"] :minutes :seconds]})

(let [dt (Date.)]
  (date-setter dt
               {:year     99
                "month"   11
                "date"    31
                :my-hours 23
                :minutes  59
                :seconds  59})
  [(= 99 (.getYear dt))
   (= 11 (.getMonth dt))
   (= 31 (.getDate dt))
   (= 23 (.getHours dt))
   (= 59 (.getMinutes dt))
   (= 59 (.getSeconds dt))]) ;; => [true true true true true true]
```

## How it works
`defsetter` defines a function in this case `date-setter` that looks something like the following:
```clj
(defn date-setter
  {:tag  Date
   :doc "date setter"}
  [^Date obj m]
  ;; body elided
  )
```
`obj` is a Java(Script) object and `m` is a map.


## NB
* Specify setters as keywords ie `:year`, or strings ie `"month"` only.
  - Symbols in the setter array don't work.
* By default the setter method generated follow Java conventions.
  - `:year` generates a call to `.setYear`
  - `"month"` generates a call to `.setMonth`
* Specify the exact method to invoke by supplying a tuple ie `[:my-hours "setHours"]`
  - `:my-hours` by itself would generate a call to `.setMyHours`
  - By specifying `"setHours"` in the tuple the method call will be `.setHours`
* Properties are set in the order specified in the `:setters` vector.
* If the map does not have a a key then the corresponding setter is not invoked.
  - Behind the scenes, the macro uses a series of `if` statements. See tests for expansion.
* Keys not specified in `:setters` are ignored.
* Generated code should *not* generate reflection warnings.

Copyright © 2019 E85th

Distributed under the Apache License 2.0.
