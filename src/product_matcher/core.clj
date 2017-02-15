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

(defn any? [bool-fn this thats]
  "does an invocation of bool-fn for this and any of thats return true?"
  (reduce #(or %1 %2)
          false
          (map #(bool-fn this %) thats)))


(def matching-functions
  "a collection of matching functions. Boolean function (does listing match
  product?), then a value onwhich overall probability is multiplien if no match,
  then if they match."
  [[(fn [l p]
      ;; match if a listing title begins with a full model name (with manufacturer
      ;; and possibly family)
      (any?
       str/starts-with?
       (get-tr l :title)
       [(str/join " " [(get-tr p :manufacturer) (get-tr p :model)])
        (str/join " " [(get-tr p :manufacturer) (get-tr p :family) (get-tr p :model)])]))
    0 1]
   [(fn [l p]
      ;; match if a product's manufacturer or it's aliases are encountered in
      ;; listing's manufacturer
      (any?
       #(.contains %1 %2)
       (get-tr l :manufacturer)
       (into  [(get-tr p :manufacturer)]
              (manufacturer-aliases (get-tr p :manufacturer)))))
    0.5 1]])

(defn match-probability [listing product]
  "counts overall probability that listing matches product using functions in
matching-functions and their weights."
  (reduce * (map #(do
                    (if ((get % 0) listing product)
                        (get % 2)
                        (get % 1)))
                 matching-functions)))

;; (defn match-all [listings products]
;;   (for [l listings p products
;;         :when (let [probability (match-probability l p)]
;;                 ;; (if (= 1 probability) (print "Matches found: 123123    \r"))
;;                 (= 1 probability))]
;;     [l p]))

(defn match-all [listings products]
  (pmap (fn [product]
         (assoc product
                :listings
                (reduce (fn [product-listings listing]
                          (if (= 1 (match-probability listing product))
                            (println "MATCH" product listing)
                            (conj product-listings listing)))
                        []
                        listings)))
       products))

(defn -main
  [& args]
  (println "Finding matches. This can take a while...")
  ;; (print "Matches found: 0 \r")
  (time
   (doall
    (match-all listings products))))
