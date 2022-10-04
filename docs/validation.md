---
title: Validation algorithm
layout: pageMath
---

In this page, we briefly discuss the theoretical aspect of our validation algorithm, followed by some implementation details.
The complete 

## Theory
Let us assume an ordering on the set of keys.
That is, among all possible permutations of key-value pairs inside an object, we select one.
Now, let us assume we have a VPA $$A$$ that recognizes the same set of documents than a JSON schema, with that fixed ordering.
While $$A$$ will correctly recognize documents that respect this order, it will fail if the document we receive does not follow it.
This is not problematic if we have the full document at hand, as we can then re-order the keys.
However, in a streaming context, where we receive the document symbol by symbol, this is not feasible.
Our new validation algorithm abstracts the transitions of $$A$$ that describe an object (as arrays always have a fixed order, we focus solely on objects), and uses that abstraction to "jump around" in the set of states of $$A$$ when reading an object.
Once the full object is read, we search a path in the VPA that corresponds to what we read.
If we can find one, then what we read is considered valid for the schema.
If not, we reject the word as it was not valid.

### Precise type of automaton
Actually, we assume that the VPAs we consider follow a strict structure.
Namely, these VPAs are actually *One Single Entry Visibly Pushdown Automata* (1-SEVPA).
Under this structure, we have that for all states $$p$$ and call symbol $$a$$, the transition reading $$a$$ must go to the initial location and push $$(p, a)$$ on the stack.
See Alur, R., Kumar, V., Madhusudan, P., Viswanathan, M., 2005. *Congruences for visibly pushdown languages*. In Automata, Languages and Programming, 32nd International Colloquium, ICALP 2005.

### Abstraction: key graph
The abstraction we use is a special type of graph, called *key graph*.
Its vertices are triplets $$(q, k, p)$$ such that from the state $$q$$ in the 1-SEVPA, it is possible to read the key $$k$$ and a correct value (according to the schema's constraints) and reach the state $$p$$, i.e., for some value $$v$$, we have $$(p, \varepsilon) \xrightarrow{k v} (q, \varepsilon)$$.
There is an edge between two vertices $$(q_1, k_1, p_1)$$ and $$(q_2, k_2, p_2)$$ if and only if $$(p_1, \varepsilon) \xrightarrow{,} (q_2, \varepsilon)$$, i.e., we can read a comma between $$p_1$$ and $$q_2$$.
Then, instead of seeking a correct path in the 1-SEVPA, we can seek a path in the graph such that the set of keys seen in the vertices is exactly the set of keys seen in the JSON object.
On top of that, when reading the object, we mark the vertices of the graph that we did not reach.
For instance, if after reading $$k$$ and its value, we reach the state $$p$$, we can then mark all vertices $$(q, k, p')$$ such that $$p \neq p'$$.
In other words, we mark the vertices that can not be part of a path that reads the JSON object.
In the end, we seek a path that contains only unmarked vertices (i.e., vertices that can be part of a correct path) and that contains all the seen keys.
If we can find one, we consider the JSON object as valid.

#### Construction
To construct the key graph of some 1-SEVPA $$A$$, we first compute the *reachability relation* $$\mathrm{Reach}_A$$ of $$A$$.
This set is defined as the pairs of states $$(q, p)$$ such that it is possible to go from $$q$$ to $$p$$ by reading a well-matched word.
In mathematical notations,

$$\mathrm{Reach}_A = \{(q, p) \mid (q, \varepsilon) \xrightarrow{w} (p, \varepsilon) \text{ for some well-matched word $w$}\}.$$

For 1-SEVPAs, this can be computed by an adaptation of the Warshall's transitive closure of a graph.
More precisely, each iteration, our algorithm allows one more call (and its corresponding return), and computes the transitive close of the graph under this constraint on the number of calls.
Once two consecutive iterations compute exactly the same set, we are done.
The complexity is in $$\mathcal{O}({|A|^5})$$ (where $$|A|$$ gives the number of states in $$A$$).

Using $$\mathrm{Reach}_A$$, the vertices of the key graph are $$(p, k, p')$$ such that $$(q, \varepsilon) \xrightarrow{k} (q, \varepsilon)$$ and:

  1. There exists an internal symbol $$a$$ such that $$(q, \varepsilon) \xrightarrow{a} (p', \varepsilon)$$, or
  2. There exist $$(q, \varepsilon) \xrightarrow{a} (r, \gamma), (r, r') \in \mathrm{Reach}_A$$, and $$(r', \gamma) \xrightarrow{\overline{a}} (p', \varepsilon)$$.

### Validation algorithm
Let us now give the rough idea behind the validation algorithm.
The idea is similar to the determinization of VPAs, as presented in Alur, R., and Madhusudan, P., 2004. *Visibly pushdown languages*. In Proceedings of the 36th Annual ACM Symposium on Theory of Computing (pp. 202–211).
In short, we retain a set $$R$$ with all the pairs of states $$(q, p)$$ such that we can go from $$q$$ to $$p$$ by reading a well-matched word.
Upon reading a call symbol $$a$$, we push $$R$$ to a stack.
Assume we have read the well-matched word $$w$$.
When we read a return symbol $$\overline{a}$$, we pop $$R'$$ from the stack, and by using $$R$$ and $$R'$$, we can compute a set with the states that can be reached by reading $$a w \overline{a}$$, which is a well-matched word.

For JSON documents, we have two possible call symbols: `[` (to open an array) and `{` (to open an object).
In the first case, we can actually follow the determinization algorithm as we know the order of the values is always fixed.
For objects, as we want to be able to read the key-value pairs in any order, we have to push more information on the stack.
Furthermore, when reading commas, we must update the stack's top tuple.

Again, the reader is invited to read the full paper for all the details.

## Implementation

The validation algorithm and the key graph are implemented in the `validation` module.
In the implementation, the ordering over the keys is dictated by the way the `org.json` library stores the objects.

The code is structured into two packages:
  - [`be.ac.umons.jsonvalidation`](api/apidocs/be/ac/umons/jsonvalidation/) which includes the validation algorithm itself, and its stack contents.
  - [`be.ac.umons.jsonvalidation.graph`](api/apidocs/be/ac/umons/jsonvalidation/graph/) which includes the key graph, and the relations it is built upon.

Among the classes of these two packages, [`JSONSymbol`](api/apidocs/be/ac/umons/jsonvalidation/JSONSymbol.html) defines the symbols we use in a word, based on the abstractions from our [JSON schema library](https://github.com/DocSkellington/JSONSchemaTools); [`ValidationByAutomaton`](api/apidocs/be/ac/umons/jsonvalidation/ValidationByAutomaton.html) implements our validation algorithm; `ReachabilityMatrix` and its two sub-classes [`ReachabilityRelation`](api/apidocs/be/ac/umons/jsonvalidation/graph/ReachabilityRelation.html) and [`OnAcceptingPathRelation`](api/apidocs/be/ac/umons/jsonvalidation/graph/OnAcceptingPathRelation.html) implement the relations required for the construction of the key graph, finding a counterexample if the key graph is invalid, and the detection of the bin state in a 1-SEVPA; and [`KeyGraph`](api/apidocs/be/ac/umons/jsonvalidation/graph/KeyGraph.html) defines the key graph (note that [`KeyGraphToDot`](api/apidocs/be/ac/umons/jsonvalidation/graph/KeyGraphToDot.html) allows to write the key graph under the DOT format which can be used to have a visual representation of the graph).

### Remarks
Note that this specific implementation uses a little more memory than what is exactly required.
As we desire reproducible results, we use [`LinkedHashMap`](https://docs.oracle.com/javase/8/docs/api/java/util/LinkedHashMap.html) and [`LinkedHashSet`](https://docs.oracle.com/javase/8/docs/api/java/util/LinkedHashSet.html) instead of regular `HashMap` and `HashSet`.
The reason is that the linked versions guarantee an order when we iterate over the collections.
More precisely, this traversal order is exactly the same as the insertion order.
This means that, for the same schema and VPA, the algorithm will always perform exactly the same way.

Moreover, the implementation of the reachability relation assumes that we are working on a JSON document.
Typically, when we consider the call symbol `{`, we know that the only return symbol that corresponds is `}` (i.e., we do not consider `]` if we seek a correct word).