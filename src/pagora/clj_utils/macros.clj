(ns pagora.clj-utils.macros)

(defmacro assert-x
  "Evaluates expr and throws an exception if it does not evaluate to
  logical true."
  {:added "0.1.4"}
  ([x]
   `(when-not ~x
      (throw (ex-info (str "Assert-info failed: " (pr-str '~x)) {}))))
  ([x message]
   `(assert-x ~x ~message {}))
  ([x message data]
   `(when-not ~x
      (throw (ex-info ~message ~data)))))

(defmacro if-let*
  ([bindings then]
   `(if-let* ~bindings ~then nil))
  ([bindings then else]
   (if (seq bindings)
     `(if-let [~(first bindings) ~(second bindings)]
        (if-let* ~(drop 2 bindings) ~then ~else)
        ~else
        ;; ~(if-not (second bindings) else)
        )
     then)))

(defmacro when-let*
  ([bindings & body]
   (if (seq bindings)
     `(when-let [~(first bindings) ~(second bindings)]
        (when-let* ~(drop 2 bindings) ~@body))
     `(do ~@body))))
