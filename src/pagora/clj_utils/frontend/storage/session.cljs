(ns pagora.clj-utils.frontend.storage.session
  (:refer-clojure :exclude [get remove set keys])
  (:require cljs.reader))

;; Get/set/remove keys in sessionStorage if it exists. If not then all functions
;; return nil and do nothing. Keys and values can be any clojure value.

(defn test-session-storage []
  (try
    (js/sessionStorage.setItem "foo" "bar")
    (js/sessionStorage.removeItem "foo")
    :ok
    (catch :default e ;; js/Error e
      )))

(def session-storage? (test-session-storage))

(defn stringify [v]
  (with-out-str (pr v)))

(defn parse [s]
  (cljs.reader/read-string s))

(defn get
  "Retrieves value for k in sessionStorage. If value is not a clojure
  value, returns raw (string) value"
  [k]
  (if session-storage?
    (if-let [s (js/sessionStorage.getItem (stringify k))]
      (try (parse s)
           (catch :default e
             ;;TODO: Probably not an issue anymore (in ie), but when console object is
             ;;not available this will probably throw
             (js/console.error "Couldn't parse session storage string into clojure value: " s " for key " k)
             s)))))

(defn _remove [k]
  (js/sessionStorage.removeItem (stringify k)))

(defn remove
  "Remove any number of ks from sessionStorage"
  [& ks]
  (if session-storage?
    (doseq [k ks]
      (_remove k))))

(defn set
  "Set k to v in sessionStorage. k and v can both be any clojure value"
  [k v]
  (if session-storage? (js/sessionStorage.setItem (stringify k) (stringify v))))

(defn keys
  "Returns seq of clojure keys in sessionStorage"
  []
  (if session-storage? (map parse (js->clj (js/Object.keys js/sessionStorage)))))
