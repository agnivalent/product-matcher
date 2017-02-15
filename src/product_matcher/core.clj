(ns product-matcher.core
  (:gen-class)
  (:require [product-matcher.io :as io]
            [clojure.string :as str]))

(def listings
  (do
    (println "Loading listings...")
    (time
     (io/load-all-from-file "resources/listings.txt"))))

(def products
  (do
    (println "Loading products...")
    (time
     (io/load-all-from-file "resources/products.txt"))))

(defn get-tr [hash key]
  "get the value from the hash and normalize it (trim+lowercase)"
  (str/trim (str/lower-case (get hash key ""))))

(def manufacturer-aliases
  "aliases for product manufacturers"
  {"fujifilm" ["fuji"]})

(def matching-functions
  "a collection of matching functions. Boolean function (does listing match
  product?), then a value onwhich overall probability is multiplien if no match,
  then if they match."
  [[(fn [l p]
      ;; match if a listing title begins with a full model name (with manufacturer
      ;; and possibly family)
      (re-find (re-pattern (str "^" (get-tr p :manufacturer)
                                "( " (if (:family p) (get-tr p :family) "\\w+") ")? "
                                (get-tr p :model)))
               (get-tr l :title)))
    0 1]
   [(fn [l p]
      ;; match if a product's manufacturer or it's aliases are encountered in
      ;; listing's manufacturer
      (let [p-m (get-tr p :manufacturer)]
        (re-find (re-pattern
                  (str "(" (str/join "|" (into  [p-m] (manufacturer-aliases p-m))) ")"))
                 (get-tr l :manufacturer))))
    0.5 1]])

(defn match-probability [listing product]
  "counts overall probability that listing matches product using functions in
matching-functions and their weights."
  (reduce * (map #(do
                    (if ((get % 0) listing product)
                        (get % 2)
                        (get % 1)))
                 matching-functions)))

(defn match-all [listings products]
  (pmap (fn [product]
         (assoc product
                :listings
                (reduce (fn [product-listings listing]
                          (if (= 1 (match-probability listing product))
                            ;; (println "MATCH" product listing)
                            (conj product-listings listing)
                            product-listings))
                        []
                        listings)))
        products))

(defn -main
  [& args]
  (println "Finding matches. This can take a while...")
  ;; (print "Matches found: 0 \r")
  (time
   (do (doall
        (io/write-all-to-file "resources/results.txt"
                              (match-all listings products)))
       (shutdown-agents))))
