# Validating JSON schemas with learned visibly pushdown automata

This documentation assumes the reader is familiar with JSON documents and JSON schemas.
See [json.org](https://www.json.org/json-en.html) and [json-schema.org](https://json-schema.org/) for more information.

See the [full documentation](https://docskellington.github.io/ValidatingJSONSchemaWithLearnedVPA/) for more details about the project, and the [API documentation](https://docskellington.github.io/ValidatingJSONSchemaWithLearnedVPA/api/apidocs/index.html) for the classes and methods.

This project is part of a collection of projects.
The final goal is to be able to automatically construct an automaton that approximates a JSON schema, and use it to validate documents.
An automaton accepting the same set of documents than a given schema is useful in the case of streaming large documents.
A classical tool to validate a document must, in the worst case, wait for the whole document before processing it, while an automaton can process it in an efficient way symbol by symbol.
A similar use-case on XML documents has already been studied.[^1]

The other projects in the collection are:
  - [AutomataLib's fork](https://github.com/DocSkellington/automatalib): implements specific automata.
  - [LearnLib's fork](https://github.com/DocSkellington/learnlib): implements learning algorithms for automata.
  - [JSON schema tools](https://github.com/DocSkellington/JSONSchemaTools): implements a JSON schema validator and document generators.

## Theoretical background
This project implements a paper about using *visibly pushdown automata* (VPA) to validate *JSON documents* against a *JSON schema*.
The idea is to first construct an automaton that abstracts the JSON schema.
This automaton assumes a fixed order over the key-value pairs.
While this brings a rather succinct representation, it contradicts the definition of a JSON object.
<!-- TODO: add link to arxiv's paper -->

The validation algorithm relies on the learned automaton and manipulates it in a way that allows any permutation of the pairs key-value.

The project is split into three parts:
  - A part that implements the validation algorithm, as presented in the paper.
  - A part that defines the oracles that can be used in a learning experiment.
  - Two benchmarks for the learning and the validation processes.

We give here a short overview of the ideas.
The reader is invited to read the article to have a complete understanding of the algorithms.

### Validation
Let us assume an ordering on the set of keys.
That is, among all possible permutations of key-value pairs inside an object, we select one.
In the implementation, this ordering is dictated by the way the `org.json` library stores the keys.

Let us assume we have a VPA that recognizes the same set of documents than a JSON schema, with that fixed ordering.
While this VPA will correctly recognize documents that respect this order, it will fail if the document we receive does not follow it.
This is not problematic if we have the full document at hand, as we can then re-order the keys.
However, in a streaming context, where we receive the document symbol by symbol, this is not feasible.
Our new validation algorithm abstracts the transitions of the VPA that describe an object[^2], and uses that abstraction to "jump around" in the VPA's set of states when reading an object.
Once the full object is read, we search a path in the VPA that corresponds to what we read.
If we can find one, then what we read is considered valid for the schema.
If not, we reject the word as it was not valid.

The validation algorithm is implemented in the `validation` module.

### Learning
In order to construct the VPA (with a fixed ordering on the keys) required for the validation algorithm, we rely on an active automata learning algorithm, called *TTT*.
Oracles to answer membership and equivalence queries are implemented in the `learning` module.
Note that oracles for realtime one-counter automata and visibly one-counter automata are also implemented, although the validation algorithm does not support these models.

## Benchmarks
We perform benchmarks for both learning and validation, on a collection of schemas (described after).
These benchmarks output their results in a `Results` directory, as CSV files.
For more information, see [the learning benchmarks](https://docskellington.github.io/ValidatingJSONSchemaWithLearnedVPA/learningBenchmarks.html) and [the validation benchmarks](https://docskellington.github.io/ValidatingJSONSchemaWithLearnedVPA/validationBenchmarks.html) documentation.

Note that the learning benchmarks are also implemented for realtime one-counter automata, and visibly one-counter automata as a consequence of previous works.
We do not guarantee they are completely operational.
In particular, it is not possible to run the validation benchmarks with one of these automata, i.e., only VPAs can be used with our new algorithm.

### Used schemas
The schemas used for the benchmarks (and scripts to start the processes) are given in `schemas/benchmarks`.
Two of them were handcrafted, called `recursiveList` and `basicTypes`.
The first is a simple recursive schema, while the second iterates over every possible type in a JSON document.
The other schemas can be downloaded from [JSON schema store] website and were modified to remove all titles, descriptions, and default values. Types were added where missing, and all keys are required.
We also removed unsupported keywords (such as `pattern` for strings), and unneeded constraints (such as `"minItems": 0`).

A final, parametric schema is implemented directly in the code (see [the WorstCaseClassical class](benchmarks/validation/src/main/java/be/ac/umons/jsonvalidation/WorstCaseClassical.java)).
This schema is based on a conjunction of multiple disjunctions, and represents a worst case scenario for the classical validation algorithm.
See [the full documentation on the validation benchmarks](https://docskellington.github.io/ValidatingJSONSchemaWithLearnedVPA/validationBenchmarks.html) for more details.
<!-- TODO: check the URL -->

## How to use
In order to use this library, our [JSON schema library], and our forks of [AutomataLib](https://github.com/DocSkellington/automatalib) and [LearnLib](https://github.com/DocSkellington/learnlib) must be installed. See the corresponding projects for instructions.

To compile the validation and learning libraries and benchmarks, simply run (from the root of the repository):
```bash
mvn package
```

The JAR files for the benchmarks will be produced in the directories `benchmarks/learning/target` and `benchmarks/validation/target`.
The scripts in the `schemas` directory are the ones we used for the experimental results of our paper.
If you desire to test different settings, they provide examples on how to configure and run the benchmarks for any JSON schema.
You can also see the documentation of [the learning benchmarks](https://docskellington.github.io/ValidatingJSONSchemaWithLearnedVPA/learningBenchmarks.html) and of [the validation benchmarks](https://docskellington.github.io/ValidatingJSONSchemaWithLearnedVPA/validationBenchmarks.html).

If you want to use the learning or validation libraries in a project, run `mvn install` to install the libraries locally, without including the benchmarks.
Then, to use the modules, add the following code in your `pom.xml`:
```XML
<dependencies>
  ...
  <dependency>
    <groupId>be.ac.umons.validatingjsonbylearning</groupId>
    <artifactId>jsonvalidation</artifactId>
    <version>2.0</version>
  </dependency>
  <dependency>
    <groupId>be.ac.umons.validatingjsonbylearning</groupId>
    <artifactId>jsonlearning</artifactId>
    <version>2.0</version>
  </dependency>
  ...
</dependencies>
```

### Results
Python scripts to generate tables and figures from the learning and validation results are provided in the `results` directory.
They require Python 3 the following dependencies:
  1. [pandas](https://pandas.pydata.org/).
  2. [NumPy](https://numpy.org/).

While we can not give the LaTeX files that read the output of the script for the validation results (for licensing reasons), we describe them in [the documentation](https://docskellington.github.io/ValidatingJSONSchemaWithLearnedVPA/validationBenchmarks.html#figures).
This description should be enough to be able to recreate them.

The results presented in our paper can be found in `results/LearningResults` and `results/ValidationResults`.
Moreover, we provide a Bash script to call the Python scripts on these CSV files.

## How to extend
If you desire to change the way values are handled or the supported keywords for the generation and validation of JSON documents, please see the documentation of our [JSON schema library].

Changes to the teacher's oracles for the learning part must be done within the `learning` module, while changes concerning the validation must lie within the `validation` module.

## Known bugs
  - A special character inside a regular expression may lead to problems during runtime.
    For instance, a `\.` inside a `patternProperties` leads to an invalid document.

Nor this bug, nor the one from our [JSON schema library] has an influence over the benchmarks' result, as both situations do not arise in the considered schemas.

## License
The source code is distributed under Apache License 2.0.

## Maintainer
  * Gaëtan Staquet, F.R.S.-FNRS, University of Mons, and University of Antwerp.

[^1]: Kumar, V., Madhusudan, P., and Viswanathan, M., 2007, Visibly pushdown automata for streaming XML. In Proceedings of the 16th International Conference on World Wide Web (pp. 1053–1062); and Neider, D., 2008. Learning Automata for Streaming XML Documents. In Informatiktage (pp.&nbsp;23-26).
[^2]: Since the order of an array is always fixed (by definition), we only focus on objects.

[JSON schema store]: https://www.schemastore.org/json/
[JSON schema library]: https://github.com/DocSkellington/JSONSchemaTools