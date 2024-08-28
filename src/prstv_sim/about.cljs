(ns prstv-sim.about
  (:require [markdown.core :refer [md->html]]))


;; Text
;;; Introduction




;;; Usage



;;; Counting Rules

(def testing (md->html "


# Introduction

As an Irish citizen, I recently became curious about how votes are counted in our electoral system. Ireland uses the **single transferable vote** (STV) version of a **proportional representation** voting system. This system is also used in a few other countries like Malta and Australia.

When voting in an election, we will have a ballot that lists all the candidates. Then, we mark our **preference**, beginning with the number &rsquo;1&rsquo;, across the ballot. You can assign a preference to as many or as little candidates as you wish. For example, a ballot might look like this:


```
    - Candidate A [3]
    - Candidate B [1]
    - Candidate C [ ]
    - Candidate D [2]
    - Candidate E [ ]
    - ...
```

In this case, Candidate B is my first preference, Candidate D my second, and so on.

This is all straight-forward enough, but the thing I was curious about was what happened to my ballot after it entered the ballot box?



# Usage

In order to answer this question, I made this &rsquo;single transferable vote&rsquo; simulator. To use it, you have to:

1.  Configure the vote parameters, including:
    -   Party names/popularity
    -   Candidate names/popularity
    -   Number of votes

2.  (Optional) Create a &rsquo;ballot&rsquo; of your own to track

3.  View the results of the &rsquo;election&rsquo;

The main area of the results section is the counts table. This shows how the ballots stood at each count. As you will see, under this type of system there are *multiple* counts. After each count **ballots are redistributed** according to certain rules. These rules are covered in more detail below.



# Counting Rules



# Vote Generation

```clojure

(defn hello []
  \"World\")
```


"))

;;; Ballot Generation

(defn about []
  [:div {:class "max-w-prose dark:text-white text-sm md:text-base"}
   [:div {:dangerouslySetInnerHTML {:__html testing}}]])
