(ns hhs.product.core
  (:require [io.pedestal.interceptor.chain :as chain]
            [clojure.string :as s]))

;;------------------------------------------
;; Helper method for generating response
;; data
;;------------------------------------------
(defn gen-resp
  "Generate a resonse with the given status and body"
  [s b]
  {:status s
   :body b})

;;-----------------------------------------
;; Non public UID generator
;; Dummy implementation just returns "hhsuser"x
;;-----------------------------------------
(defn- get-uid
  [token]
  (when (and (string? token) (not (empty? token)))
    {"uid" (str "hhsuser")}))



;;---------------------------------------
;; Merge all parameters (form, query and path) into one
;; map. Keys will correspond to param names
;;
;;---------------------------------------
(defn request-params
  "Extract path, query and form parameters inot a single map. "
  [context]
  (let [params (merge (-> context :request :form-params)
                      (-> context :request :query-params)
                      (-> context :request :path-params))
        data-found (or (params :id)
                       (params :name)
                       (params :description)
                       (params :price)
                       (params :rating))]

    (if (and (not (empty? params)) data-found)
      (let [flds (if-let [fls (:flds params)]
                   (map s/trim (s/split fls #",") )
                   (vector))
            params (assoc params :flds flds)]
        
        (assoc context :tx-data params))
      (chain/terminate
       (assoc context
              :respomse (gen-resp 400
                                  "One of the following contact details is obligatory: 
                                   address, email or mobile number"))))))




;;----------------------------------------
;; Authentication interceptor
;; This version only checks for the precense of a token
;; in the request, it does not validate the token.
(def auth
  {:name ::auth
   :enter (fn [context]
             (let [token (-> context :request :headers (get "token"))]
               (if-let [uid (and (not (nil? token)) (get-uid token))]
                 (assoc context [:request :tx-data :user] uid)
                 (chain/terminate))))
   :error (fn [context ex-info]
            (assoc context :response (gen-resp 500
                                               (.getMessage ex-info))))})


;;----------------------------------------
;; Intercpetor checks if a valid product ID was
;; submitted in form, query or path params
;; If processing continues
;; if not terminates chain processing with HTTP 400 BAD REQUEST
;; :query-params - after ? - https://domain.cxy/path?queryparma=value
;; :path-params - after domain https://domain.cxy/products/:id
;;----------------------------------------
(def product-id
  {:name ::product-id
   :enter (fn [context]
            (if-let [pid (or (-> context :request :path-params :id)
                             (-> context :request :query-params :id)
                             (-> context :request :form-params :id))]
              (request-params context)
              (chain/terminate (assoc context :response (gen-resp 400
                                                                  "invalid product id") ) )))})
