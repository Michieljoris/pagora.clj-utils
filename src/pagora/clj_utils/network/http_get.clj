(ns pagora.clj-utils.network.http-get
  (:require [clojure.data.json :as json]
            ;; [clj-http.client :as client]
            [org.httpkit.client :as http]
            [taoensso.timbre :as timbre]))

;;TODO: only a sketch of such a namespace sofar.

(defn make-options [username password]
  {:timeout 30000             ; ms
   :basic-auth [username password]
   :as :auto
   :headers {"Content-Type" "application/json"
             "Accept"       "application/json"}})

(defn report-error  [cb error-info]
  (let [msg "Failed to get data"]
    (timbre/warn {:error msg :info error-info})
    (cb {:error msg :info error-info})))

(defn http-get [{:keys [url cb username password]}]
  (let [options (make-options username password)]
    (try
      (http/get url options
                (fn [{:keys [status headers body error]}] ;; asynchronous response handling
                  (if (or error (not= status 200))
                    (report-error cb {:status status
                                      :username username
                                      :url url
                                      :http-error (.toString error)})
                    (try
                      (cb {:value (json/read-str body)})
                      (catch Exception e
                        (report-error cb {:exception (.toString e)
                                          :username username
                                          :url url}))))))
      (catch Exception e ;;timeout
        (report-error cb {:username username
                          :url url :exception (.toString e)})))))
