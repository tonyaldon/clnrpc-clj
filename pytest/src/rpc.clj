(ns rpc
  (:require [clnrpc-clj :as rpc])
  (:require [clojure.data.json :as json])
  (:require [com.brunobonacci.mulog :as u])
  (:require [clojure.edn :as edn])
  (:require [clojure.java.io :as io]))

(defn call-getinfo [{:keys [socket-file test-payload]}]
  (let [rpc-info {:socket-file socket-file}]
    (-> (if test-payload
          ;; to test we pass [] and not `null` in the json request
          ;; when `payload` argument of `call` in `nil`
          (rpc/call rpc-info "getinfo" nil)
          (rpc/call rpc-info "getinfo"))
        (json/write *out* :escape-slash false))))

(defn call-invoice [{:keys [socket-file]}]
  (let [rpc-info {:socket-file socket-file}
        payload {:amount_msat 10000
                 :label (str "label-" (rand))
                 :description "description"}]
    (-> (rpc/call rpc-info "invoice" payload)
        :bolt11
        print)))

(defn call-pay [{:keys [socket-file bolt11]}]
  (let [rpc-info {:socket-file socket-file}
        payload {:bolt11 bolt11}]
    (-> (rpc/call rpc-info "pay" payload)
        :status
        print)))

(defn call-unknown-command-foo [{:keys [socket-file]}]
  (let [rpc-info {:socket-file socket-file}]
    (try
      (rpc/call rpc-info "foo")
      (catch clojure.lang.ExceptionInfo e
        (when (= (:type (ex-data e)) :rpc-error)
          (-> (ex-data e)
              :error
              (json/write *out* :escape-slash false)))))))

(defn call-invoice-missing-label [{:keys [socket-file]}]
  (let [rpc-info {:socket-file socket-file}
        payload {:amount_msat 10000}]
    (try
      (rpc/call rpc-info "invoice" payload)
      (catch clojure.lang.ExceptionInfo e
        (when (= (:type (ex-data e)) :rpc-error)
          (-> (ex-data e)
              :error
              (json/write *out* :escape-slash false)))))))

(defn jsonrpc-id
  "Print the jsonrpc id used in the getinfo request to lightningd.
  SOCKET-FILE is lightningd's socket file."
  [{:keys [socket-file json-id-prefix]}]
  (let [log-file "/tmp/jsonrpc-id"
        rpc-info {:socket-file socket-file
                  :json-id-prefix json-id-prefix}]
    (io/delete-file log-file true)
    (def stop (u/start-publisher!
               {:type :simple-file :filename log-file}))
    (rpc/call rpc-info "getinfo")
    (Thread/sleep 1000) ;; wait for log dispatch
    (stop)
    (with-open [in (java.io.PushbackReader. (io/reader log-file))]
      (loop [read-more true]
        (when-let [event (edn/read {:default tagged-literal :eof nil} in)]
          (if (= (:mulog/event-name event) :clnrpc-clj/request-sent)
            (print (:req-id event))
            (recur true)))))))
