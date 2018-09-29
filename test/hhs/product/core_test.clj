(ns hhs.product.core-test
  (:require [clojure.test :refer :all]
            [hhs.product.core :as core]))


(def ctx  {:request {:headers {"token" "123"
                               "user-agent" "curl/7.47.0"
                               "accept" "*/*"
                               "host" "localhost:8890"}
                     :params {}
                     :path-info ""
                     :path-params {:pathparam1 "products"
                                   :pathparam2 "101"}
                     :query-params {:query-param1 "101"
                                    :query-param2 "laptop"
                                    :flds "id,name,description"
                                    :id 101}
                     :query-string ""
                     :form-params {:form-param1 "Book"
                                   :form-param2 "Tablet"}
                     :edn-params {}}
           :tx-data {:form-param1 "Book"
                     :form-param2 "Tablet"
                     :query-param1 "101"
                     :query-param2 "laptop"
                     :id 101
                     :pathparam1 "customer"
                     :pathparam2 "name"
                     :flds []}})

(deftest test-gen-resp
  "Test generation of response object"
  ;; Passes
  (is (= {:status 200 :body "Product saved!"}
         (core/gen-resp 200 "Product saved!")))

  ;; nil
  (is (= {:status nil
          :body nil}
         (core/gen-resp nil nil))))


(deftest test-get-uid
  (is (= {"uid" "hhsuser"} (core/get-uid "123")))

  (is (= nil (core/get-uid nil)))

  (is (= nil (core/get-uid ""))))



(deftest test-request-parameters
  ;; Should not be nil
  (is (not= {} (core/request-params ctx))))

(deftest test-auth
  ;;Auth interceptor must add uid to context under the key :user
  ;;The uid is generated using th get-uid function and saved under
  ;; [:request :tx-data :user]
  (is (= {"uid" "hhsuser"}
         (-> ctx
             ((:enter core/auth))
             (get-in [:request :tx-data :user]))))

  ;; If parameter to the auth interceptor is nill then we should get
  ;; status 401
  (is (= {:response  {:status 401
                      :body "Authentication token not found!"}}
         ((:enter core/auth) nil))))
