(ns solutions.fight
  (:use [clojure.data.json :only (read-json)])
  (:import (java.net URL URLEncoder)
           (java.io BufferedReader InputStreamReader)))

(def google-search-base
  "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=")

(defn url-encode [q]
  (URLEncoder/encode q "UTF-8"))

(defn request [address]
  (let [url (URL. address)]
    (with-open [stream (. url (openStream))]
      (let [buf (BufferedReader. (InputStreamReader. stream))]
        (apply str (line-seq buf))))))

(use 'clojure.contrib.pprint)

(defn estimated-hits-for
  [term]
  (let [http-response (request (str google-search-base (url-encode term)))
        json-response (read-json http-response)]
    (Long/parseLong (get-in json-response [:responseData :cursor :estimatedResultCount]))))

(defn fight
  [term1 term2]
  (let [r1 (future (estimated-hits-for term1))
        r2 (future (estimated-hits-for term2))]
    (future {term1 @r1 term2 @r2})))

(def fight-results
  (agent {}))

(defn add-estimate
  [estimates term]
  (assoc estimates term (estimated-hits-for term)))

