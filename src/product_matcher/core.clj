(ns product-matcher.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.string :as str]))

(defn read-json-stream [stream]
  "return lazy sequense of entities (as hashes) from JSON stream (should be one entity per line)"
  (map
   #(json/read-str % :key-fn keyword)
   (line-seq stream)))

(defn load-all-from-file [file-name]
  (with-open [stream (clojure.java.io/reader file-name)]
    (doall
     (read-json-stream stream))))

(def listings (load-all-from-file "resources/listings.txt"))

(def products (load-all-from-file "resources/products.txt"))

(defn get-tr [hash keyword]
  (str/trim (str/lower-case (get hash keyword ""))))

(defn match-probability [listing product]
  (if (str/starts-with?
       (str (get-tr listing :title))
       (str (get-tr product :manufacturer) " " (get-tr product :family) " " (get-tr product :model)))
    1
    0))

()

(defn -main
  [& args]
  (time
   (print "Matches: "
          (count
           (doall
            (for [l listings p products
                  :when (= 1 (match-probability l p))]
              [l p]))))))
