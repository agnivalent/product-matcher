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

(def manufacturer-aliases
  {"fujifilm" ["fuji"]})

(defn any? [bool-fn this thats]
  (reduce #(or %1 %2)
          false
          (map #(bool-fn this %) thats)))

(def matching-functions
  [[(fn [l p] (any?
               str/starts-with?
               (str (get-tr l :title))
               [(str/join " " [(get-tr p :manufacturer) (get-tr p :model)])
                (str/join " " [(get-tr p :manufacturer) (get-tr p :family) (get-tr p :model)])]))
    0 1]
   [(fn [l p] (.contains
               (str (get-tr l :manufacturer))
               (str (get-tr p :manufacturer))))
    0.5 1]
   ])

(defn match-probability [listing product]
  (reduce * (map #(do
                    (if ((get % 0) listing product)
                        (get % 2)
                        (get % 1)))
                 matching-functions)))

(defn -main
  [& args]
  (time
   (doall
    (map println
         (for [l listings p products
               :when (= 1 (match-probability l p))]
           [l p])))))
