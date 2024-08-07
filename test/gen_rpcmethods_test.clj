(ns gen-rpcmethods-test
  "Test gen-rpcmethods library (under tools directory)."
  (:require [clojure.test :refer :all])
  (:require [gen-rpcmethods :as gen]))

(deftest docstring-type-description-test
  (is (= (gen/docstring-type-description
          {:type "foo-type" :description "foo-description"})
         "      - type: foo-type
      - description: foo-description"))
  (is (= (gen/docstring-type-description
          {:type "foo-type" :description ""})
         "      - type: foo-type
      - description:"))
  (is (= (gen/docstring-type-description
          {:type "foo-type"})
         "      - type: foo-type
      - description:"))
  ;; when :description is a vector
  (is (= (gen/docstring-type-description
          {:type "foo-type" :description ["foo-description"]})
         "      - type: foo-type
      - description: foo-description"))
  (is (= (gen/docstring-type-description
          {:type "foo-type"
           :description ["foo-description-line-1"
                         "    * foo-description-line-2"
                         "    * foo-description-line-3"]})
         "      - type: foo-type
      - description: foo-description-line-1
          * foo-description-line-2
          * foo-description-line-3")))

;; (run-test docstring-type-description-test)

(deftest docstring-type-test
  (is (= (gen/docstring-type
          true "foo-req"
          {:type "foo-req-type" :description "foo-req-description"})
         "  FOO-REQ
      - type: foo-req-type
      - description: foo-req-description"))

  (is (= (gen/docstring-type
          true "bar-req" {:type "bar-req-type" :description ""})
         "  BAR-REQ
      - type: bar-req-type
      - description:"))
  (is (= (gen/docstring-type
          true "bar-req" {:type "bar-req-type"})
         "  BAR-REQ
      - type: bar-req-type
      - description:"))
  (is (= (gen/docstring-type
          true "baz-req"
          {:oneOf
           [{:type "baz-req-type-1" :description "baz-req-description"}
            {:type "baz-req-type-2" :description ""}
            {:type "baz-req-type-3"}]
           })
         "  BAZ-REQ
      - type: baz-req-type-1
      - description: baz-req-description

      or

      - type: baz-req-type-2
      - description:

      or

      - type: baz-req-type-3
      - description:"))
  (is (= (gen/docstring-type
          false "foo-opt"
          {:type "foo-opt-type" :description "foo-opt-description"})
         "  :foo-opt
      - type: foo-opt-type
      - description: foo-opt-description"))
  (is (= (gen/docstring-type
          false "bar-opt" {:type "bar-opt-type" :description ""})
         "  :bar-opt
      - type: bar-opt-type
      - description:"))
  (is (= (gen/docstring-type
          false "bar-opt" {:type "bar-opt-type"})
         "  :bar-opt
      - type: bar-opt-type
      - description:"))
  (is (= (gen/docstring-type
          false "baz-opt"
          {:oneOf
           [{:type "baz-opt-type-1" :description "baz-opt-description"}
            {:type "baz-opt-type-2" :description ""}
            {:type "baz-opt-type-3"}]
           })
         "  :baz-opt
      - type: baz-opt-type-1
      - description: baz-opt-description

      or

      - type: baz-opt-type-2
      - description:

      or

      - type: baz-opt-type-3
      - description:")))

(deftest docstring-test
  (is (= (gen/docstring
          "foo-req-and-opt-args"
          {:required ["foo-req" "bar1-req" "bar2-req" "baz-req"],
           :properties
           {:foo-req {:type "foo-req-type" :description "foo-req-description"}
            :bar1-req {:type "bar1-req-type" :description ""}
            :bar2-req {:type "bar2-req-type"}
            :baz-req {:oneOf
                      [{:type "baz-req-type-1" :description "baz-req-description"}
                       {:type "baz-req-type-2" :description ""}
                       {:type "baz-req-type-3"}]}
            :foo-opt {:type "foo-opt-type" :description "foo-opt-description"}
            :bar1-opt {:type "bar1-opt-type" :description ""}
            :bar2-opt {:type "bar2-opt-type"}
            :baz-opt {:oneOf
                      [{:type "baz-opt-type-1" :description "baz-opt-description"}
                       {:type "baz-opt-type-2" :description ""}
                       {:type "baz-opt-type-3"}]}}})
         "Send foo-req-and-opt-args request to lightningd via unix socket.

  The connection is done via :socket-file specified in RPC-INFO.
  :json-id-prefix key of RPC-INFO is used as the first part of
  the JSON-RPC request id.  See tonyaldon.cln.rpc/call for more details.

  FOO-REQ
      - type: foo-req-type
      - description: foo-req-description

  BAR1-REQ
      - type: bar1-req-type
      - description:

  BAR2-REQ
      - type: bar2-req-type
      - description:

  BAZ-REQ
      - type: baz-req-type-1
      - description: baz-req-description

      or

      - type: baz-req-type-2
      - description:

      or

      - type: baz-req-type-3
      - description:

  Use OPT-PARAMS to set optional parameters of the request.
  The following keyword argument(s) can be passed with values:

  :foo-opt
      - type: foo-opt-type
      - description: foo-opt-description

  :bar1-opt
      - type: bar1-opt-type
      - description:

  :bar2-opt
      - type: bar2-opt-type
      - description:

  :baz-opt
      - type: baz-opt-type-1
      - description: baz-opt-description

      or

      - type: baz-opt-type-2
      - description:

      or

      - type: baz-opt-type-3
      - description:"))
  (is (= (gen/docstring
          "foo-req-and-no-opt-args"
          {:required ["foo-req" "bar-req" "baz-req"],
           :properties
           {:foo-req {:type "foo-req-type" :description "foo-req-description"}
            :bar-req {:type "bar-req-type" :description "bar-req-description"}
            :baz-req {:type "baz-req-type" :description "baz-req-description"}}})
         "Send foo-req-and-no-opt-args request to lightningd via unix socket.

  The connection is done via :socket-file specified in RPC-INFO.
  :json-id-prefix key of RPC-INFO is used as the first part of
  the JSON-RPC request id.  See tonyaldon.cln.rpc/call for more details.

  FOO-REQ
      - type: foo-req-type
      - description: foo-req-description

  BAR-REQ
      - type: bar-req-type
      - description: bar-req-description

  BAZ-REQ
      - type: baz-req-type
      - description: baz-req-description"))
  (is (= (gen/docstring
          "foo-no-req-and-opt-args"
          {:required [],
           :properties
           {:foo-opt {:type "foo-opt-type" :description "foo-opt-description"}
            :bar-opt {:type "bar-opt-type" :description "bar-opt-description"}
            :baz-opt {:type "baz-opt-type" :description "baz-opt-description"}}})
         "Send foo-no-req-and-opt-args request to lightningd via unix socket.

  The connection is done via :socket-file specified in RPC-INFO.
  :json-id-prefix key of RPC-INFO is used as the first part of
  the JSON-RPC request id.  See tonyaldon.cln.rpc/call for more details.

  Use OPT-PARAMS to set optional parameters of the request.
  The following keyword argument(s) can be passed with values:

  :foo-opt
      - type: foo-opt-type
      - description: foo-opt-description

  :bar-opt
      - type: bar-opt-type
      - description: bar-opt-description

  :baz-opt
      - type: baz-opt-type
      - description: baz-opt-description"))
  (is (= (gen/docstring
          "foo-no-req-and-no-opt-args"
          {:required [], :properties {}})
         "Send foo-no-req-and-no-opt-args request to lightningd via unix socket.

  The connection is done via :socket-file specified in RPC-INFO.
  :json-id-prefix key of RPC-INFO is used as the first part of
  the JSON-RPC request id.  See tonyaldon.cln.rpc/call for more details.")))

;; (run-test docstring-test)

(defn remove-docstring [f]
  "Remove the docstring of a function definition list.

    (remove-docstring '(defn foo \"docstring\" [] \"bar\"))
    ;; (defn foo [] \"bar\")"
  (keep-indexed #(when (not= %1 2) %2) f))

(deftest generate-rpcmethod-test
  (is (= (remove-docstring
          (gen/generate-rpcmethod
           "foo-req-and-opt-args"
           {:required ["foo-req" "bar1-req" "bar2-req" "baz-req"],
            :properties
            {:foo-req {:type "foo-req-type" :description "foo-req-description"}
             :bar1-req {:type "bar1-req-type" :description ""}
             :bar2-req {:type "bar2-req-type"}
             :baz-req {:oneOf
                       [{:type "baz-req-type-1" :description "baz-req-description"}
                        {:type "baz-req-type-2" :description ""}
                        {:type "baz-req-type-3"}]}
             :foo-opt {:type "foo-opt-type" :description "foo-opt-description"}
             :bar1-opt {:type "bar1-opt-type" :description ""}
             :bar2-opt {:type "bar2-opt-type"}
             :baz-opt {:oneOf
                       [{:type "baz-opt-type-1" :description "baz-opt-description"}
                        {:type "baz-opt-type-2" :description ""}
                        {:type "baz-opt-type-3"}]}}}))
         '(defn foo-req-and-opt-args
            [rpc-info foo-req bar1-req bar2-req baz-req & opt-params]
            (call rpc-info "foo-req-and-opt-args"
                  (merge
                   {:foo-req foo-req
                    :bar1-req bar1-req
                    :bar2-req bar2-req
                    :baz-req baz-req}
                   (apply hash-map opt-params))))))
  (is (= (remove-docstring
          (gen/generate-rpcmethod
           "foo-req-and-no-opt-args"
           {:required ["foo-req" "bar-req" "baz-req"],
            :properties
            {:foo-req {:type "foo-req-type" :description "foo-req-description"}
             :bar-req {:type "bar-req-type" :description "bar-req-description"}
             :baz-req {:type "baz-req-type" :description "baz-req-description"}}}))
         '(defn foo-req-and-no-opt-args
            [rpc-info foo-req bar-req baz-req]
            (call rpc-info "foo-req-and-no-opt-args"
                  {:foo-req foo-req
                   :bar-req bar-req
                   :baz-req baz-req}))))
  (is (= (remove-docstring
          (gen/generate-rpcmethod
           "foo-no-req-and-opt-args"
           {:required [],
            :properties
            {:foo-opt {:type "foo-opt-type" :description "foo-opt-description"}
             :bar-opt {:type "bar-opt-type" :description "bar-opt-description"}
             :baz-opt {:type "baz-opt-type" :description "baz-opt-description"}}}))
         '(defn foo-no-req-and-opt-args
            [rpc-info & opt-params]
            (call rpc-info "foo-no-req-and-opt-args"
                  (apply hash-map opt-params)))))
  (is (= (remove-docstring
          (gen/generate-rpcmethod
           "foo-no-req-and-no-opt-args"
           {:required [], :properties {}}))
         '(defn foo-no-req-and-no-opt-args [rpc-info]
            (call rpc-info "foo-no-req-and-no-opt-args")))))

;; (run-test generate-rpcmethod-test)
