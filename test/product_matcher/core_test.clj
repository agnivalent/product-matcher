(ns product-matcher.core-test
  (:require [clojure.test :refer :all]
            [product-matcher.core :refer :all]
            [product-matcher.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]))

(deftest validate-file-format
  (testing "testing that results file format is right using online validator"
    (is
     (do
       (doall
        (io/write-all-to-file "resources/results.txt"
                              (match-all listings products)))
       (str/starts-with? (:out (sh "curl" "-XPOST" "-F" "file=@resources/results.txt" "https://challenge-check.sortable.com/validate"))
                         "Looks good!")))))
