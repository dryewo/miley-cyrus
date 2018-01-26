(ns {{namespace}}.ui-test
  (:require [clojure.test :refer :all]
            [{{namespace}}.test-utils :as tu]
            [mount.lite :as m]
            [clj-http.client :as http]))


(use-fixtures
  :each (fn [f]
          (tu/start-with-env-override {:http-port 8080} #'{{namespace}}.http/server)
          (f)
          (m/stop)))


(deftest test-ui
  (let [{:keys [status body]} (http/get "http://localhost:8080/ui")]
    (is (= 200 status))
    (is (re-seq #"Hello, World!" body))))
