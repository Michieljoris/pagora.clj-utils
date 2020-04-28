(ns pagora.clj-utils.frontend.storage.local
  (:refer-clojure :exclude [get remove set keys])
  (:require cljs.reader))

;; Get/set/remove keys in localStorage if it exists. If not then all functions
;; return nil and do nothing. Keys and values can be any clojure value.

(defn test-local-storage []
  (try
    (js/localStorage.setItem "foo" "bar")
    (js/localStorage.removeItem "foo")
    :ok
    (catch :default e ;; js/Error e
      )))

(def local-storage? (test-local-storage))

(defn stringify [v]
  (with-out-str (pr v)))

(defn parse [s]
  (cljs.reader/read-string s))

(defn get
  "Retrieves value for k in localStorage. If value is not a clojure
  value, returns raw (string) value"
  [k]
  (when local-storage?
    (if-let [s (js/localStorage.getItem (stringify k))]
      (try (parse s)
           (catch :default e
             ;;TODO: Probably not an issue anymore (in ie), but when console object is
             ;;not available this will probably throw
             (js/console.error "Couldn't parse local storage string into clojure value: " s " for key " k)
             s)))))

(defn _remove [k]
  (js/localStorage.removeItem (stringify k)))

(defn remove
  "Remove any number of ks from localStorage"
  [& ks]
  (when local-storage?
    (doseq [k ks]
      (_remove k))))

(defn set
  "Set k to v in localStorage. k and v can both be any clojure value"
  [k v]
  (when local-storage? (js/localStorage.setItem (stringify k) (stringify v))))

(defn keys
  "Returns seq of clojure keys in localStorage"
  []
  (when local-storage? (map parse (js->clj (js/Object.keys js/localStorage)))))
