(ns product-matcher.core
  (:gen-class)
  (:require [product-matcher.io :as io]
            [clojure.string :as str]))

(def listings (io.load-all-from-file "resources/listings.txt"))

(def products (io.load-all-from-file "resources/products.txt"))

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
               (get-tr l :title)
               [(str/join " " [(get-tr p :manufacturer) (get-tr p :model)])
                (str/join " " [(get-tr p :manufacturer) (get-tr p :family) (get-tr p :model)])]))
    0 1]
   [(fn [l p] (any?
               #(.contains %1 %2)
               (get-tr l :manufacturer)
               (into  [(get-tr p :manufacturer)] (manufacturer-aliases (get-tr p :manufacturer)))))
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
    (for [l listings p products
          :when (= 1 (match-probability l p))]
      [l p]))))
