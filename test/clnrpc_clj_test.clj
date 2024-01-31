(ns clnrpc-clj-test
  "Test clnrpc-clj library."
  (:require [clojure.test :refer :all])
  (:require [clnrpc-clj :as client])
  (:require [clojure.java.shell :refer [sh]])
  (:require [clojure.java.io :as io]))

(deftest read-test
  (is (=
       (let [msg "foo\nbar\nbaz\n\ndiscarded"
             socket-file "/tmp/socket-file"
             send-msg-cmd (format "echo '%s' | nc -U %s -l" msg socket-file)]
         (doto (Thread. (fn []
                          (io/delete-file socket-file true)
                          ;; start socket server and send msg
                          (sh "bash" "-c" send-msg-cmd)))
           .start)
         (Thread/sleep 1000) ;; wait for socket server to start
         (-> socket-file client/connect client/read))
       "foo\nbar\nbaz")))

(deftest call-test
  ;; test that we raise an error if we receive a response
  ;; from lightningd with and id that don't the one we send
  ;; in our request
  (is (thrown-with-msg?
       Throwable
       #"Incorrect 'id' .+ in response: .+\.  The request was: .+ "
       (let [msg-wrong-id "{\"jsonrpc\":\"2.0\",\"id\":\"WRONG-ID\",\"result\": []}\n\n"
             socket-file "/tmp/socket-file"
             send-msg-cmd (format "echo '%s' | nc -U %s -l" msg-wrong-id socket-file)]
         (doto (Thread. (fn []
                          (io/delete-file socket-file true)
                          ;; start socket server and send msg-wrong-id
                          (sh "bash" "-c" send-msg-cmd)))
           .start)
         (Thread/sleep 1000) ;; wait for socket server to start
         (client/call socket-file "getinfo")))))

(deftest symlink-test
  (let [target (.toString (java.io.File/createTempFile "clnrpc-clj-" nil))
        link (client/symlink target)]
    (is (java.nio.file.Files/isSameFile
         (java.nio.file.Paths/get link (into-array String []))
         (java.nio.file.Paths/get target (into-array String []))))
    (io/delete-file target true)
    (io/delete-file link true)))

;; (run-tests 'clnrpc-clj-test)