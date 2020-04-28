(ns  pagora.clj-utils.timbre
  (:require
   #?(:clj [clojure.pprint :refer [pprint]])
   #?(:cljs [cljs.pprint :refer [pprint]])
   [taoensso.timbre :as timbre]
   [fipp.edn :refer (pprint) :rename {pprint fipp}]
   #?(:clj [jansi-clj.core :as jansi])
   [clojure.string :as str]))

(def abbr-map {:b :blue
               :r :red
               :o :orange
               :g :green
               :p :purple
               :y :darkgoldenrod
               :c :cyan
               })
#?(:clj
   (def jansi-map
     {:#b jansi/blue :#blue jansi/blue
      :#r jansi/red :#red jansi/red
      :#w jansi/white :#white jansi/white
      :#o jansi/magenta :#orange jansi/magenta
      :#m jansi/magenta :#magenta jansi/magenta
      :#g jansi/green :#green jansi/green
      :#p jansi/cyan :#purple jansi/cyan
      :#y jansi/yellow :#darkgoldenrod jansi/yellow
      :#c jansi/cyan :#cyan jansi/cyan}))

#?(:cljs
   (defn extract-color [output]
     (let [re #":#([a-z]*) "
           match (re-find re output)
           color (keyword (get match 1))
           color (if color (str "color:" (name (or (color abbr-map) color))))
           output (str/replace-first output re "")
           output (if color (str "%c" output) output)
           color (if color color "")]
       [output color])
     ))

;; Appender adapts console output so you can colorize your output, also
;; console.warn is replaced with console.info. Use like this:
;; (info :#b "whatever you want to log" :bla some-symbol)
;; (warn :#blue "whatever you want to log" :bla some-symbol) ;exactly the same
#?(:cljs
   (defn console-appender
     "Returns a simple js/console appender for ClojureScript.
  For accurate line numbers in Chrome, add these Blackbox[1] patterns:
    `/taoensso/timbre/appenders/core\\.js$`
    `/taoensso/timbre\\.js$`
    `/cljs/core\\.js$`
  [1] Ref. https://goo.gl/ZejSvR"

     ;; TODO Any way of using something like `Function.prototype.bind`
     ;; (Ref. https://goo.gl/IZzkQB) to get accurate line numbers in all
     ;; browsers w/o the need for Blackboxing?

     [& [opts]]
     {:enabled?   true
      :async?     false
      :min-level  nil
      :rate-limit nil
      :output-fn  :inherit
      :fn
      (if (exists? js/console)
        (let [;; Don't cache this; some libs dynamically replace js/console
              level->logger
              (fn [level]
                (or
                 (case level
                   :trace  js/console.trace
                   :debug  js/console.debug
                   :info   js/console.info
                   :warn   js/console.warn ;warn screws up autoscroll in chrome
                   :error  js/console.error
                   :fatal  js/console.error
                   :report js/console.error)
                 js/console.log))]

          (fn [{:keys [vargs level] :as data} ]
            (when-let [logger (level->logger level)]
              (condp = (first vargs)
                :#cp (let [{:keys [?ns-str ?line]} data]
                       (logger (str (str/upper-case (name level)) ":pprint" " [" ?ns-str ":" ?line "]"))
                       (enable-console-print!)
                       (logger (second vargs)))
                :#pp (let [{:keys [?ns-str ?line]} data]
                       (logger (str (str/upper-case (name level)) ":pprint" " [" ?ns-str ":" ?line "]"))
                       (enable-console-print!)
                       (logger (with-out-str (fipp (second vargs)))))
                 (if (or (:raw-console? data)
                         (get-in data [:?meta :raw-console?])) ; Undocumented
                   (let [output
                         ((:output-fn data)
                          (assoc data
                                 :msg_  ""
                                 :?err nil))
                         ;; (<output> <raw-error> <raw-arg1> <raw-arg2> ...):
                         args (->> vargs (cons (:?err data)) (cons output))]
                     (.apply logger js/console (into-array args)))

                   (let [output (force (:output_ data))
                         [output color] (extract-color output)]
                     (.call logger js/console output color)))))))

        (fn [data] nil))}))

#?(:clj
   (defn middleware [data]
     (let [{:keys [vargs]} data
           modifier (first vargs)]
       (cond
         (contains? jansi-map modifier) (assoc data :vargs (mapv (get jansi-map modifier)
                                                                 (rest vargs)))
         (= modifier :#pp) (assoc data :vargs (mapv (fn [d]
                                                      (with-out-str (fipp d)))
                                                    vargs))
         :else data))))
