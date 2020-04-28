(ns pagora.clj-utils.database.connection
  (:require [clojure.pprint :refer [pprint]]
            #?(:clj
               [jdbc.pool.c3p0 :as pool])
            [taoensso.timbre :as timbre]
             ;; #?(:cljs [aum.js.alasql])
             ))


#?(:clj
   (defn set-pool-loglevel [loglevel]
     (System/setProperties
      (doto (java.util.Properties. (System/getProperties))
        (.put "com.mchange.v2.log.MLog" "com.mchange.v2.log.FallbackMLog")
        (.put "com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL" loglevel)))))

(defn make-subname [url db-name use-ssl]
  (let [utf-encoding true]
    (cond-> (str url db-name "?zeroDateTimeBehavior=convertToNull")
      (= (boolean use-ssl) use-ssl) (str "&useSSL=" (if use-ssl "true" "false"))
      utf-encoding (str "&characterEncoding=UTF-8"))))

#?(:cljs
   (defn make-db-connection [{:keys [db-name print-spec alasql-persistence]}]
     (when print-spec
       (timbre/info :#w "Database details:")
       (timbre/info :#w (str "\n" (with-out-str (pprint {:db-name db-name})))))
     ;; (case alasql-persistence
     ;;   "LOCALSTORAGE" (do
     ;;                    (.exec (goog.object/getValueByKeys js/alasql "databases" "alasql")
     ;;                           (str "CREATE " alasql-persistence " DATABASE IF NOT EXISTS " db-name) )
     ;;                    (.exec (goog.object/getValueByKeys js/alasql "databases" "alasql")
     ;;                           (str "ATTACH " alasql-persistence " DATABASE " db-name)))
     ;;   nil)
     ;; {:alasql js/alasql :db-name db-name :alasql-persistence alasql-persistence}
     )
   )

#?(:clj
(defn make-db-connection [{:keys [user password url db-name use-ssl pool print-spec min-pool-size initial-pool-size
                                  pool-loglevel]}]
  (let [db-spec {:classname   "com.mysql.jdbc.Driver"
                 :subprotocol "mysql"
                                        ; This has zeroDateTimeBehaviour set for jdbc
                                        ; So that 0000-00-00 00:00:00 date times from the database
                                        ; are handled as null otherwise it will throw exceptions.
                 :subname  (make-subname url db-name use-ssl)
                 :min-pool-size (or min-pool-size 3)
                 :initial-pool-size (or initial-pool-size 3)
                 :user  user
                 :password password}]
    (when print-spec
      (timbre/info :#w "Database details:")
      (timbre/info :#w (str "\n" (with-out-str (pprint (assoc db-spec :password "***"))))))
    (if pool
      (do
        (when pool-loglevel
          (set-pool-loglevel pool-loglevel))
        (pool/make-datasource-spec db-spec))
      db-spec))))
