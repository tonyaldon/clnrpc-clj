.PHONY: pytest cljtest test rpcmethods

CLN_TAG=v24.02

cljtest:
	clojure -X:test

pytest:
	pytest pytest

test: cljtest pytest

rpcmethods:
	clojure -X clnrpc-utils/generate-rpcmethods :tag '"$(CLN_TAG)"'
