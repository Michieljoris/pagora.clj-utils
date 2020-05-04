;;Adapted from https://github.com/clojure/data.csv/blob/master/src/main/clojure/clojure/data/csv.clj
(ns pagora.clj-utils.csv.core
  (:require
   [taoensso.timbre :as timbre]
   [clojure.string :as str])
  #?(:cljs
     (:import [goog.string StringBuffer])))

(defn- write-cell [writer obj sep quote quote?]
  (let [string (cond
                 (inst? obj) (.toISOString obj)
                 :else (str obj))
        must-quote (quote? string)]
    (when must-quote (#?(:clj .write
                         :cljs -write) writer quote))
    (#?(:clj .write
        :cljs -write) writer (if must-quote
                               (str/escape string
                                           {quote (str quote quote)})
                               string))
    (when must-quote (#?(:clj .write
                         :cljs -write) writer quote))))

(defn- write-record [writer record sep quote quote?]
  (loop [record record]
    (when-first [cell record]
      (write-cell writer cell sep quote quote?)
      (when-let [more (next record)]
        (#?(:clj .write
            :cljs -write) writer sep)
        (recur more)))))

(defn- write-csv*
  [writer records sep quote quote? ^String newline]
  (loop [records records]
    (when-first [record records]
      (write-record writer record sep quote quote?)
      (#?(:clj .write
          :cljs -write) writer newline)
      (recur (next records)))))

(do
  (defn vals-by-keys [m ks]
    (->> ks
         (reduce (fn [v k]
                   (conj v (get m k)))
                 [])))
  ;; (vals-by-keys {:a 1 :b 2 :c 3 :d 4} [:a :d :e])
  )

(defn write-csv
  "Writes data to writer in CSV-format.

   Valid options are
     :separator (Default \\,)
     :quote (Default \\\")
     :quote? (A predicate function which determines if a string should be quoted. Defaults to quoting only when necessary.)
     :newline (:lf (default) or :cr+lf)"
  [writer data {:keys [separator quote quote? newline]
                :or {separator \,
                     quote \"
                     quote? #(some #{separator quote \return \newline} %)
                     newline :lf}}]
  {:pre [(seqable? data)]}
  (write-csv* writer
              data
              separator
              quote
              quote?
              ({:lf "\n" :cr+lf "\r\n"} newline)))

#?(:cljs
   (defn maps->csv-str [maps {:keys [columns options]}]
     (let [data (->> maps
                     (map #(vals-by-keys % columns)))
           data (cond->> data
                  (:include-column-names options) (cons (mapv #(str/replace (name %) "-" " ") columns)))
           sb (StringBuffer.)
           writer (StringBufferWriter. sb)]
       (write-csv writer data options)
       (.toString sb))))
