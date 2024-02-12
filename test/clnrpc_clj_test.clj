(ns clnrpc-clj-test
  "Test clnrpc-clj library."
  (:require [clojure.test :refer :all])
  (:require [clnrpc-clj :as rpc])
  (:require [clojure.java.shell :refer [sh]])
  (:require [clojure.java.io :as io]))
  (:require [babashka.fs :as fs]))

(deftest read-test
  (is (=
       (let [msg "foo\nbar\nbaz\n\ndiscarded"
             socket-file (.toString (java.io.File/createTempFile "socket-file-" nil))
             send-msg-cmd (format "echo '%s' | nc -U %s -l" msg socket-file)]
         ;; start socket server and send `msg`
         (.start (Thread. (fn [] (sh "bash" "-c" send-msg-cmd))))
         (Thread/sleep 1000) ;; wait for socket server to start
         (-> socket-file rpc/connect-to rpc/read))
       "foo\nbar\nbaz")))

(deftest call-test
  ;; test that we raise an error if we receive a response
  ;; from lightningd with an id that don't match the one we send
  ;; in our request
  (is (thrown-with-msg?
       Throwable
       #"Incorrect 'id' .+ in response: .+\.  The request was: .+"
       (let [msg-wrong-id "{\"jsonrpc\":\"2.0\",\"id\":\"WRONG-ID\",\"result\": []}\n\n"
             socket-file (.toString (java.io.File/createTempFile "socket-file-" nil))
             send-msg-cmd (format "echo '%s' | nc -U %s -l" msg-wrong-id socket-file)
             rpc-info {:socket-file socket-file}]
         ;; start socket server and send `msg-wrong-id`
         (.start (Thread. (fn [] (sh "bash" "-c" send-msg-cmd))))
         (Thread/sleep 1000) ;; wait for socket server to start
         (rpc/call rpc-info "getinfo")))))

(deftest symlink-test
  (let [target (str (fs/create-temp-file))
        link (rpc/symlink target)]
    (is (fs/same-file? link target))
    (fs/delete link)
    (fs/delete target)))

;; (run-tests 'clnrpc-clj-test)
