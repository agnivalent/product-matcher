(ns product-matcher.core-test
  (:require [clojure.test :refer :all]
            [product-matcher.core :refer :all]
            [product-matcher.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]))

(defn- count-matches [pwm]
  (count (reduce #(into %1 (:listings %2)) [] pwm)))

(deftest validate-file-format
  (let [pwm (match-all listings products)]
    (testing "there's enough matches, but not too much"
      (is (> (count-matches) 6000))
      (is (< (count-matches) 10000)))
    (testing "results file format is right using online validator"
      (is
       (do
         (doall
          (io/write-all-to-file "resources/results.txt"
                                pwm))
         (str/starts-with? (:out (sh "curl" "-XPOST" "-F" "file=@resources/results.txt" "https://challenge-check.sortable.com/validate"))
                           "Looks good!"))))))
