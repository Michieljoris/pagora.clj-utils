(ns pagora.clj-utils.frontend.storage.cookie
  (:import goog.net.Cookies))

(def cookies (Cookies. js/document))


;; (set-cookie "bla" "123" :max-age -1 :path "/" :domain "localhost")
(defn set-cookie [k v & opts]
  "Sets a cookie.
   Options:
   max-age -- The max age in seconds (from now). Use -1 to set a session cookie. If not provided, the default is -1 (i.e. set a session cookie).
   "
  (when-let [k (and (.isValidName cookies (name k)) (name k))]
    (when (.isValidValue cookies v)
      (let [{:keys [max-age path domain secure?]} (apply hash-map (vec opts))]
        (.set cookies k v max-age path domain secure?)))))

(defn get-cookie [k]
  "Returns the value for the first cookie with the given key."
  (.get cookies (name k) nil))

(defn remove-cookie
  ([key] (remove-cookie key nil))
  ([key path]
   "Removes and expires a cookie."
   (set-cookie (name key) "" :path path :max-age 0))
  ;; (.remove cookies (name key))
  )

(defn cookie-enabled?
  ([] (cookie-enabled? cookies))
  ([c] (.isEnabled c)))

(defn cookie-empty?
  ([] (cookie-empty? cookies))
  ([c] (.isEmpty c)))
