(ns pagora.clj-utils.core
  (:require
   [cuerdas.core :as str]
   [clojure.walk :as walk])
  #?(:cljs
     (:import goog.Delay))
  )

(defn parse-natural-number
  "Reads and returns an integer from a string, or the param itself if
  it's already a natural number. Returns nil if not a natural
  number (includes 0)"
  [s]
  (cond
    (and (string? s) (re-find #"^\d+$" s)) (#?(:cljs cljs.reader/read-string)
                                            #?(:clj read-string)
                                            s)
    (and (number? s) (>= s 0))  s
    :else nil))

;; https://github.com/jeremyheiler/wharf/blob/master/src/wharf/core.clj
(defn transform-keys
  "Recursively transforms all map keys in coll with t."
  [t coll]
  (let [f (fn [[k v]] [(t k) v])]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) coll)))

(defn empty-keys
  "Given a map, and keys, returns subset of keys for which val in the
  map is empty? "
  [m ks]
  (let [empty-map (into {} (map (fn [k] [k nil]) ks))
        m (merge empty-map m)
        ks (set ks)]
    (set (->> m
              (filter (fn [e]
                        (let [v (second e)]
                          (and (contains? ks (first e))
                               (or (nil? v) (and (string? v) (empty? v)))))))
              (mapv first)))))

(defn hyphen->underscore
  [s]
  (str/replace s #"-" "_"))

(defn underscore->hyphen
  [s]
  (str/replace s #"_" "-"))

(defn includes?
  "Returns true if collection c includes element e, otherwise nil"
  [c e]
  (some #(= % e) c))

(defn map->keys-and-vals
  "Takes a map and returns a map with two keys, :keys and :vals
  Containing the keys and vals of the original map in the same order"
  [m]
  (reduce (fn [p n]
            (-> p
                (update  :keys conj (first n))
                (update  :vals conj (second n))))
          {:keys [] :vals []} m))

(defn keyword->underscored-string [k]
  (if k (hyphen->underscore (name k))))

(defn deep-merge-maps
  "Deep merges two maps. As per normal merge, values in b overwrite
  values in a"
  [a b]
  (letfn [(dm [a b]
            (merge-with (fn [x y]
                          (cond (and (map? x) (map? y))
                                (dm x y)
                                :else y))
                        a b))]
    (dm a b)))

(defn deep-merge-concat-vectors
  "Deep merges two maps. As per normal merge, values in b overwrite
  values in a, however vectors are concatenated, with duplicates
  removed. "
  [a b]
  (merge-with (fn [x y]
                (cond (and (map? x) (map? y)) (deep-merge-concat-vectors x y)
                      (and (or (vector? x) (nil? x))
                           (or (vector? y) (nil? y))
                           (or (vector? x) (vector? y))) (vec (distinct (concat x y)))
                      :else y))
              a b))

#?(:clj
   (defn parse-ex-info [^Exception e]
     {:msg (or (.getMessage e) (.toString e))
      :context (ex-data e)
      :stacktrace (.getStackTrace e)}))

#?(:cljs
   (defn parse-ex-info [e]
     {:msg (goog.object/get e "message")
      :context (goog.object/get e "data")
      :stacktrace (goog.object/get e "stack")}))

(def ^:private uuid-regex
  (let [x "[0-9a-fA-F]"] (re-pattern (str
    "^" x x x x x x x x "-" x x x x "-" x x x x "-" x x x x "-" x x x x x x x x x x x x "$"))))

#?(:clj
   (defn is-uuid?
     [uuid-str]
     (try
       (re-find uuid-regex uuid-str)
       (catch Exception e))))

#?(:cljs
   (defn is-uuid?
     [uuid-str]
     (try
       (re-find uuid-regex uuid-str)
       (catch :default e))))

#?(:cljs
   (defn debounce [f interval]
     (let [timeout (atom nil)]
       (fn [& args]
         (when-not (nil? @timeout)
           (.disposeInternal @timeout))
         (reset! timeout (Delay. #(apply f args)))
         (.start @timeout interval)))))

(defn atom? [x]
  #?(:clj  (instance? clojure.lang.Atom x)
     :cljs (satisfies? IAtom x)))

#?(:cljs
   (defn getTimezoneOffsetInMinutes []
     (.getTimezoneOffset (js/Date.))))

#?(:cljs
   (defn from-now
     ([t some-moment] (from-now some-moment :nl))
     ([t some-moment locale]
      (let [some-moment (js/moment some-moment)
            now (js/moment)
            tzOffset (getTimezoneOffsetInMinutes)
            some-moment (.subtract some-moment tzOffset "minutes")]
        (.locale js/moment (name locale))
        (cond
          ;; (not (.isValid some-moment)) (t "Date not valid")
          (not (.isValid some-moment)) ""
          (.isAfter some-moment (.subtract now 21 "hours")) (.fromNow some-moment false)
          (.isAfter some-moment (.subtract now 48 "hours")) (str (t "Yesterday at") " " (.format some-moment "LT"))
          :else (.format some-moment (str "LL [" (t "at") "] LT")))))))

(defn escape-sql-like-term
   "Returns string that can be used as term after LIKE in a sql
  query by escaping specific sql chars ([, ], _ and %). Regex works
  differently in clj then cljs"
   [s]
   (str "%" (str/replace s #"(\[|\]|\_|\%)" "\\\\$1") "%"))

(defn transform-values
  "Recursively transforms all map values in coll with t."
  [t coll]
  (let [f (fn [[k v]] [k (t v)])]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) coll)))

(defn string->number [s]
  (cond
    (number? s) s
    (and (string? s) (re-find #"^-?\d+$" s)) (#?(:cljs cljs.reader/read-string)
                                            #?(:clj read-string)
                                            s)
    :else nil))
