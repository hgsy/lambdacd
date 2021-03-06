(ns lambdacd.util-test
  (:use [lambdacd.util])
  (:require [clojure.test :refer :all]
            [conjure.core :as c]
            [me.raynes.fs :as fs]
            [clojure.java.io :as io]))

(deftest range-test
  (testing "that range produces a range from a value+1 with a defined length"
    ; TODO: the plus-one is like that because the user wants it, probably shouldn't be like this..
    (is (= '(6 7 8) (range-from 5 3)))))

(defn some-function [] {})


(deftest map-if-test
  (testing "that is applies a function to all elements that match a predicate"
    (is (= []      (map-if (identity true) inc [])))
    (is (= [4 3 5] (map-if #(< % 5) inc [3 2 4])))
    (is (= [3 2 5] (map-if #(= 4 %) inc [3 2 4])))))

; ======== START DUPLICATE (TO MAKE SURE COMPATIBILITY WITH-TEMP STILL WORKS) ======

(defn- throw-if-not-exists [f]
  (if (not (fs/exists? f))
    (throw (IllegalStateException. (str f " does not exist")))
    "some-value-from-function"))

(deftest with-temp-test
  (testing "that a tempfile is deleted after use"
    (let [f (create-temp-file)]
      (is (= "some-value-from-function" (with-temp f (throw-if-not-exists f))))
      (is (not (fs/exists? f)))))
  (testing "that a tempfile is deleted when body throws"
    (let [f (create-temp-file)]
      (is (thrown? Exception (with-temp f (throw (Exception. "oh no!")))))
      (is (not (fs/exists? f)))))
  (testing "that a temp-dir is deleted after use"
    (let [d (create-temp-dir)]
      (fs/touch (fs/file d "somefile"))

      (is (= "some-value-from-function" (with-temp d (throw-if-not-exists d))))

      (is (not (fs/exists? (fs/file d "somefile"))))
      (is (not (fs/exists? d)))))
  (testing "that it can deal with circular symlinks"
    (let [f (create-temp-dir)]
      (is (= "some-value-from-function"
             (with-temp f (let [link-parent (io/file f "foo" "bar")]
                            (fs/mkdirs link-parent)
                            (fs/sym-link (io/file link-parent "link-to-the-start") f)
                            "some-value-from-function"
                            ))))
      (is (not (fs/exists? f))))))

; ======== END DUPLICATE (TO MAKE SURE COMPATIBILITY WITH-TEMP STILL WORKS) ======
