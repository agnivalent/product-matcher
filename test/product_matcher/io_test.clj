(ns product-matcher.io-test
  (:require [clojure.test :refer :all]
            [product-matcher.io :refer :all]))

(deftest loading-from-json-files
  (testing "loading products from file"
    (let [products (load-all-from-file "resources/products.txt")
          some-product (last products)]
      (is (= 743 (count products)))
      (is (contains? some-product :manufacturer))
      (is (= "XV-3" (:model some-product))))))
