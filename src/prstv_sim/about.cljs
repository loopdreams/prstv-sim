(ns prstv-sim.about
  (:require [markdown.core :refer [md->html]]))


;; Text
;;; Introduction




;;; Usage



;;; Counting Rules

(def testing (md->html "
## Introduction

Counting votes fairly in an election is hard. There are many different methods and systems you can use. A recent video by Veritasium, titled [&ldquo;Why Democracy is mathematically impossible&rdquo;](https://www.youtube.com/watch?v=qf7ws2DF-zk), illustrates some of the complexities well.

As an Irish citizen, I recently became curious about how votes are counted in our own electoral system. Ireland uses the **single transferable vote** (STV) version of a **proportional representation** voting system. This system is also used in a few other countries like Malta and Australia.

When voting in an election, we have a ballot that lists all the candidates. Then, we mark our **preference**, beginning with the number &rsquo;1&rsquo;, across the ballot. You can assign a preference to as many or as few candidates as you wish. For example, a ballot might look like this:

-   Candidate A (3)
-   Candidate B (1)
-   Candidate C ( )
-   Candidate D (2)
-   Candidate E ( )

In this case, Candidate B is my first preference, Candidate D my second, and so on.

This is all straight-forward enough, but the thing I was curious about was what happened to my ballot after it entered the ballot box?


## Usage

In order to answer this question for myself, I made this &rsquo;single transferable vote&rsquo; simulator. To use it, there are three steps:

**Step 1** Configure the vote parameters, including:

-   Party names/popularity
-   Candidate names/popularity
-   Number of votes

**Step 2** (Optional) Create a &rsquo;ballot&rsquo; of your own to track

**Step 3** View the results of the &rsquo;election&rsquo;

The main area of the results section is the counts table. This shows how the ballots stood at each count. As you will see, under this type of system there are *multiple* counts. After each count **ballots are redistributed** according to certain rules. These rules are covered in more detail below.

If you are unfamiliar with this kind of system you might notice a few peculiar things. For example, a candidate might receive more **first preference votes** than another candidate, but the candidate with the lower votes will be elected.

For example, consider the following result:

![Sample Vote Counts Table](./img/sample_vote_counts_table.png)

As we can see, the candidate &ldquo;Minnie Mouse&rdquo; only received 12% of the first preference votes, while &ldquo;Snow White&rdquo; received 14%. Yet, &ldquo;Minnie Mouse&rdquo; was elected while &ldquo;Snow White&rdquo; was not.

This is because &ldquo;Minnie Mouse&rdquo; received more **vote transfers** than &ldquo;Snow White&rdquo;, bringing them over the line.

Right until the penultimate count (Count 4), &ldquo;Minnie Mouse&rdquo; was still behind &ldquo;Snow White&rdquo;. However, in Count 4, &ldquo;Minnie Mouses&rsquo;&rdquo; running mate (&ldquo;Mickey Mouse&rdquo;) was eliminated. People who voted for &ldquo;Mickey Mouse&rdquo; were likely to have also voted for &ldquo;Minnie Mouse&rdquo;, and they did indeed get the majority of **vote transfers** from &ldquo;Mickey Mouse&rdquo; in this simulation and were elected.

This is what the &ldquo;Minnie Mouse&rdquo; vote transfers looked like in this particular simulation. As you can see, most came from &ldquo;Mickey Mouse&rdquo;:

![Minnie Mouse Vote Flows](./img/minnie_mouse_vote_flows.png)

And here is a view of where the &ldquo;Mickey Mouse&rdquo; vote transfers went:

![Mickey Mouse Vote Flows](./img/mickey_mouse_vote_flows.png)

This kind of voting system, where the other preferences for a candidate can **transfer** in subsequent counts, is designed to ensure a fairer, more proportional representation of the electorates preferences. Unlike, for example, the &ldquo;First Past the Post&rdquo; voting systems, these kinds of systems are more suitable for elections with multiple parties and a broader range of views/positions.


## Counting Rules

In order to determine which votes go where on each count, there are a set of rules for counters to follow.

Note: Most of the information below is taken from [this guide to proportional representation by the Department of Housing, Planning, Community and Local Government](https://assets.gov.ie/111110/03f591cc-6312-4b21-8193-d4150169480e.pdf)


### Quota

The first step of counting is establishing a **quota**. This is done using a formula. In the case of Ireland, it is calculated by taking the total number of valid ballot papers and dividing this by the total number of seats + 1. Then, one is added.

For example, if the above ballot paper related to an election where there were three seats available, and there were 50,000 valid ballot papers, then the quota would be calculated as follows:

(50,000 / 3 + 1) + 1 = 12,501

In order to be elected, a candidate must receive enough votes to surpass this quota.

If, 3 candidates had 12,501 votes each, this would equal 37,503 votes, leaving 12,497 votes remaining, which wouldn&rsquo;t be enough to reach the quota and so a fourth candidate could not be elected.


### First Count

In the first count, the ballot papers are sorted/counted according to the first preference votes. This is generally the only time that all the votes of all the candidates are examined and sorted.

Once a candidate reaches the quota, they are elected. Their **surplus** votes are then redistributed. This is where things get complicated.


### Surplus Redistribution

A surplus is redistributed on the next count provided that it can either:

-   elect the highest continuing candidate
-   bring the lowest continuing candidate level with or above the second lowest continuing candidate
-   qualify the lowest continuing candidate for recoupment of their election expenses or deposit (if applicable)

Following distribution of the surplus, the elected candidate will be left with the exact quota of votes.

In the case of this website, the last condition (about recoupment of expenses) was ignored.


### Procedure for distributing the surplus

The surplus is distributed based on the next available preference for continuing candidates contained in the **last parcel of votes that brought the elected candidate over the quota**.

This is the part that I found very difficult to understand, and which caused me to try to investigate this a bit further.

> The most complex part of the counting process relates to the distribution of surplus votes.
>
> When a candidate exceeds the quota on the first count, the second preferences on each of their ballots are examined. The votes above the quota are allocated to the remaining candidates in the field based on the ratio of second preferences which has been determined by the examination of the votes.
>
> After the first count, only the votes above the quota are examined and used to decide the ratio for the allocation of the surplus votes.

[Source - Irish Times](https://www.independent.ie/irish-news/surpluses-and-tallies-this-is-the-dummys-guide-to-an-election-count/38136738.html)

Let&rsquo;s imagine that one candidate (candidate *:A*) got 250 votes and the quota was 200, meaning there is a surplus of **50** votes to be re-distributed.

The way to determine **how** these votes will be re-distributed, at least according to the Irish system, is as follows:

1.  If the ballots for the candidate consist of **only first preference votes** (i.e., this will be the case after the first count),

then **all of the elected candidate&rsquo;s** ballots are examined to determine how the surplus will be split
proportionally

1.  If the ballots are not only first preference for that candidate, then **the last parcel of votes** that brought the

elected candidate over the quota are examined to determine the proportionality.

So, in the above case, if the candidate reached 250 votes on the first count (which counts first preferences), then
all 250 votes will be examined.

Let&rsquo;s say that there are two other candidates in the race, *:B* and *:C*. When examining the 250 votes for candidate *:A*, 200 contain a second preference for candidate *:B* and 50 contain a second preference for candidate *:C*. In other words, 4/5 of the surplus should go to *:B* and 1/5 to *:C* - *:B* gets 40 of the surplus votes and *:C* gets 10.

In the second case, where the votes that got *:A* over the line were transferred from another candidate, then the &ldquo;last parcel&rdquo;
is examined to determine the proportionality. For example, if *:A* was at 180 votes on the previous count, and received **70** votes on transfer from other candidates, these 70 will be examined, and 50 (the surplus) will be transferred based on the proportions of next preferences. For example if 35 of the 70 votes had a preference for *:B* and 35 had a preference for *:C*, then *:B* and *:C* would each get 25 of *:A*&rsquo;s surplus.


### Elimination Redistribution

If no candidate has reached the surplus, or if the surplus cannot be redistributed, then the lowest continuing candidate is **eliminated**. In this case, all their votes are examined and re-distributed according to the next preferences.


## Source Code

If you are interested in the source code behind this approach, I have written up a short explaination of how the votes were generated and how the counting worked.

"))

;;; Ballot Generation


(defn about []
  [:div {:class "prose prose-slate dark:prose-invert m-auto px-4 md:px-0"}
   [:div {:dangerouslySetInnerHTML {:__html testing}}]])
