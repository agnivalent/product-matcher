(ns product-matcher.io
  (:require [clojure.data.json :as json]))

(defn read-json-stream [stream]
  "return lazy sequense of entities (as hashes) from JSON stream (should be one entity per line)"
  (map
   #(json/read-str % :key-fn keyword)
   (line-seq stream)))

(defn load-all-from-file [file-name]
  (with-open [stream (clojure.java.io/reader file-name)]
    (doall
     (read-json-stream stream))))
