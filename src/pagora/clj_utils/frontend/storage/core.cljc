(ns pagora.clj-utils.frontend.storage.core
  (:require
   [taoensso.timbre :as timbre]
   ;; [aum.environment :as env]
   #?@(:cljs
       [[pagora.clj-utils.frontend.storage.cookie :as cookie]
        [pagora.clj-utils.frontend.storage.local :as s]
        [pagora.clj-utils.frontend.storage.session :as ss]])))

;;Advanced compilation should get rid the mock definitions
(if true ;; (contains? #{:dev :test} env/environment)
  (do

    (def mock-storage? (atom #?(:cljs false
                                :clj true)))

    (def cookie (atom {}))
    (def local (atom {}))
    (def session (atom {}))

    (defn reset-mock-storage []
      (reset! cookie {})
      (reset! local {})
      (reset! session {}))

    (defn mock-cookie-set [k v & args]
      ;; (timbre/info :#b "MOCK cookie set" k v)
      (swap! cookie assoc k v)
      (timbre/info @cookie)
      )

    (defn mock-cookie-get [k]
      ;; (timbre/info :#b "MOCK cookie get" k)
      (get @cookie k))

    (defn mock-cookie-remove [k]
      ;; (timbre/info :#b "MOCK cookie remove" k)
      (swap! cookie dissoc k))

    (defn mock-local-set [k v & args]
      ;; (timbre/info :#b "MOCK local set" k v)
      (swap! local assoc k v))

    (defn mock-local-get [k]
      ;; (timbre/info :#b "MOCK local get" k)
      (get @local k))

    (defn mock-local-remove [k]
      ;; (timbre/info :#b "MOCK local remove" k)
      (swap! local dissoc k))


    (defn mock-session-set [k v & args]
      ;; (timbre/info :#b "MOCK session set" k v)
      (swap! session assoc k v))

    (defn mock-session-get [k]
      ;; (timbre/info :#b "MOCK session get" k)
      (get @session k))

    (defn mock-session-remove [k]
      ;; (timbre/info :#b "MOCK session remove" k)
      (swap! session dissoc k))

    (defn cookie-set [& args] (apply (if @mock-storage? mock-cookie-set #?(:cljs cookie/set-cookie)) args))
    (defn cookie-remove [& args] (apply (if @mock-storage? mock-cookie-set #?(:cljs cookie/remove-cookie)) args))
    (defn cookie-get [& args] (apply (if @mock-storage? mock-cookie-get #?(:cljs (fn [& args]))) args))

    (defn local-set [& args] (apply (if @mock-storage? mock-local-set #?(:cljs s/set)) args))
    (defn local-get [& args] (apply (if @mock-storage? mock-local-get #?(:cljs s/get)) args))
    (defn local-remove [& args] (apply (if @mock-storage? mock-local-remove #?(:cljs s/remove)) args))


    (defn session-set [& args] (apply (if @mock-storage? mock-session-set #?(:cljs ss/set)) args))
    (defn session-get [& args] (apply (if @mock-storage? mock-session-get #?(:cljs ss/get)) args))
    (defn session-remove [& args] (apply (if @mock-storage? mock-session-remove #?(:cljs ss/remove)) args))

    )

  #?(:cljs
     (do
       (def cookie-set cookie/set-cookie)
       (def cookie-remove cookie/remove-cookie)

       (def local-set s/set)
       (def local-get s/get)
       (def local-remove s/remove)

       (def session-set ss/set)
       (def session-get ss/get)
       (def session-remove ss/remove)

       ;;Mock storage
       (def cookie (atom {}))
       (def local (atom {}))
       (def session (atom {})))))
