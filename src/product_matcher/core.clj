(ns product-matcher.core
  (:gen-class)
  (:require [clojure.data.json :as json]))

;; lazy seq of all listings from listings.txt
(defn listings [stream]
  (map
   #(json/read-str % :key-fn keyword)
   (line-seq stream)))

(with-open [stream (clojure.java.io/reader "resources/listings.txt")]
  (do
    (pprint (take 5 (listings stream)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
