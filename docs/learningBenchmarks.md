---
title: Learning VPAs benchmarks
layout: pageMath
---

In this page, we first present membership and equivalence oracles for VPAs.
We then discuss our benchmarks, including the schemas we used.
As the learning oracles rely on principles defined for the validation algorithm, please first read the [validation documentation page](validation.html).

## Learning oracles
In order ot learn a VPA, we need to define a teacher that answers membership and equivalence queries for JSON documents.
In this implementation, we use a JSON schema to decide whether a document is valid, and to be able to generate random documents, using our [JSON schema library](https://github.com/DocSkellington/JSONSchemaTools).
Note that if you can provide a set of documents that must be accepted and a set of documents to be rejected, it is possible to define new oracles to learn a 1-SEVPA.
That is, depending on the situation, the learning process can be used to avoid writing a JSON schema and, instead, to infer one from the examples.
The resulting automaton can then be used in our validation algorithm.

{% include important.html content="In this work, we assume that a JSON document is always a JSON object. This is coherent with what we observed in a large number of documents and schemas." %}

{% include note.html content="Let us highlight the fact that we allow the learning experiment to be stopped before obtaining a valid hypothesis. It is then possible to retrieve the last hypothesis, even if it does not accept the target language. This is useful to obtain an approximation for large schemas." %}


### Membership
When the learner asks a membership query over a word $$w$$, we perform the following checks:

  1. Is $$w$$ a valid JSON document?
  2. Do the keys in $$w$$ follow the fixed ordering?
  3. Does the classical validator accept $$w$$?

If one of those checks fails, the oracle returns false, i.e., the document is not valid.
The reason for the second check is to force the learner to always respect the order, in order to keep the VPA as small as possible.

### Equivalence
For equivalence oracles over a 1-SEVPA $$A$$, we perform the following checks:

  1. Is there a loop reading an internal symbol $$a$$ over the initial state?
    If it is the case, we can construct a counterexample by concatenating $$a$$ with any word that is accepted by $$A$$.
    Indeed, any word that begins with an internal symbol can not be a valid JSON document.
  2. Using a generator from our JSON schema library, is there a valid document that is rejected by $$A$$?
    If it is the case, that document is a counterexample.
  3. Using a generator, is there an invalid document that is accepted by $$A$$?
    If so, it is a counterexample.
  4. Is there a word generated completely at random that is accepted by $$A$$ and not by the classical validator (or vice-versa)?
    If so, this word is a counterexample.
  5. Is there a path in the key graph for $$A$$ on which we see the same key multiple times?
    If so, it means that there is a path in $$A$$ that sees that key twice, and it is possible to construct a counterexample that witnesses this repetition.

{% include note.html content="It is possible to provide a set of documents to be tested, on top of those randomly generated. See the executable's arguments documentation below. " %}

### Partial support for one-counter automata
As a continuation of [our previous work](https://github.com/DocSkellington/LStar-ROCA-Benchmarks), oracles are defined for realtime one-counter automata and visibly one-counter automata.
However, we do not guarantee that these oracles work as intended (i.e., there may be bugs) as they are not a priority.
Moreover, the resulting automata can not be used to validate documents using our new algorithm.

## Benchmarks
Let us now discuss our learning benchmarks, and their arguments.

### Schemas
For our benchmarks, we used the following schemas:

  - A schema called *basicTypes* that accepts documents containing each type of values, i.e., an object, an array, a string, a number, an integer, a Boolean, and an enumeration.
  - A schema called *recursiveList* that accepts documents defined recursively.
      Each object contains a string and can contain an array
      whose single element satisfies the whole schema, i.e., this is a recursive list.
  - [A schema that defines how snippets must be described in *Visual Studio Code*](https://raw.githubusercontent.com/Yash-Singh1/vscode-snippets-json-schema/main/schema.json).
  - [A recursive schema that defines how the metadata files for *VIM plugins* must be written](https://json.schemastore.org/vim-addon-info.json).
  - [A schema that defines how *Azure Functions Proxies* files must look like](https://json.schemastore.org/proxies.json]).
  - [A schema that defines the configuration file for a code coverage tool called *codecov*](https://json.schemastore.org/codecov.json).

The schemas downloaded from [*JSON schema store*](https://www.schemastore.org/json/) were modified to remove some keywords (such as `description`, `title`, and so on).
Moreover, all keys are mandatory (i.e., we added the `required` keyword everywhere).
The schemas can be consulted on the repository.

### Input
Our benchmarks support the following arguments, in this order.
The lines in bold are mandatory.

  1. **The type of the automaton: VPA, ROCA, or VPA**.
  2. **The type of equivalence oracle to use: RANDOM or EXPLORATION**. If RANDOM is selected, the documents are generated by a random generator, and with an exhaustive generator if EXPLORATION is selected.
  3. **The time limit, in seconds**.
  4. **The path to the file containing the schema**.
  5. **The number of documents to generate by equivalence query**.
  6. **Whether to generate invalid documents in equivalence queries**.
  7. **The maximal depth of the generated documents**.
  8. **The maximal number of properties inside an object for the generated documents**.
  9. **The maximal number of items inside an array for the generated documents**.
  10. **A Boolean**. If true, the schema associated to missing `additionalProperties` is considered as the `false` schema, reducing the number of valid documents.
    If false, such a schema is considered as a `true` schema, following the specifications.
  11. **The number of experiments to conduct**.
  12. The remaining arguments must be paths to documents that must be explicitly tested during equivalence queries, on top of the randomly generated documents.

If you desire to know the maximal depth of the documents described by the schema, the validation benchmarks executable has a [parameter to compute it](validationBenchmarks.html#depth-of-a-schema).

### Output
The learning process outputs in the standard output stream some information to let the user know how far in the benchmarks we are.
Namely, a counter with the current experiment identifier, the counterexamples, and some computation key points (such as the iterations in the computation of the reachability relation) are printed.
Each line starts with a timestamp.

More importantly, multiple files are produced with the results:

  1. A CSV file that describes each experiment (the columns are given later).
    The name of the file depends on the values provided for the input arguments.
  2. A DOT file that contains the VPA learned during each experiment.
    For the experiment number $$i$$ and the schema `s.json`, the file name is `s.json-i.dot`.

The columns of an CSV file depend on the type of learned automata.
We give here the columns for VPAs:

  1. The time (in milliseconds) taken from the start of the learning experiment until the end.
  2. The number of membership queries.
  3. The number of equivalence queries.
  4. The number of rounds (with *TTT*, it should be equal to the number of equivalence queries).
  5. Whether the automaton contains a bin state.
  6. The size of the alphabet.
  7. The size of the VPA, without the bin state.
  8. The number of internal transitions, once the bin state is removed.
  9. The number of return transitions, once the bin state is removed.
  10. The number of call transitions, once the bin state is removed.
  11. The diameter of the VPA seen as a graph, once the bin state is removed.

A Python script called `learningFigures.py` is given in the `results` directory on the repository.
This script reads a CSV file for VPA and writes a table, as a LaTeX file.
The values in the table are computed as the average of all experiments, and are printed with one decimal position.
The columns are:

  1. The time (in seconds) to learn a VPA.
  2. The number of membership queries.
  3. The number of equivalence queries.
  4. The size of the automaton.
  5. The size of the alphabet.
  6. The number of call transitions.
  7. The number of return transitions.
  8. The number of internal transitions.
  9. The diameter of the automaton.

### Reproducible results
As pointed out in the [remarks of the validation documentation](validation.html#remarks), we guarantee reproducible results.
For the learning part, this is done by fixing the seed of the random generator at the start of an experiment.
More precisely, if 10 experiments must be conducted, the seeds are, in this order, 0, 1, 2, ..., 9.
{% include note.html content="This means performing 10 times a single experiment (by launching the executable 10 times), the results will always be the same (up to some variations on the time and memory). On the other hand, performing 10 experiments in a single execution of the JAR file may produce different results." %}