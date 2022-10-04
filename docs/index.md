---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults

layout: home
---

This documentation assumes the reader is familiar with JSON documents and JSON schemas.
See [json.org](https://www.json.org/json-en.html) and [json-schema.org](https://json-schema.org/) for more information.
Moreover, reader should be acquainted with the notion of visibly pushdown automata (see, for instance, Alur, R., and Madhusudan, P., 2004. *Visibly pushdown languages*. In Proceedings of the 36th Annual ACM Symposium on Theory of Computing (pp. 202–211)), and automata learning (see, for instance, Vaandrager, F., 2017. *Model learning*. In Communications of the ACM, 60(2), pp.86-95.)).

See the [API documentation](api/apidocs/index.html) if you are interested in the classes and methods.

## Motivation
This project is part of a collection of projects.
The final goal is to be able to automatically construct an automaton that approximates a JSON schema, and use it to validate documents.
An automaton accepting the same set of documents than a given schema is useful in the case of streaming large documents.
A classical tool to validate a document must, in the worst case, wait for the whole document before processing it, while an automaton can process it in an efficient way symbol by symbol.
A similar use-case on XML documents has already been studied in Kumar, V., Madhusudan, P., and Viswanathan, M., 2007. *Visibly pushdown automata for streaming XML*. In Proceedings of the 16th International Conference on World Wide Web (pp. 1053–1062); and Neider, D., 2008. *Learning Automata for Streaming XML Documents*. In Informatiktage (pp.&nbsp;23-26).

The other projects in the collection are:
  - [AutomataLib's fork](https://github.com/DocSkellington/automatalib): implements specific automata.
  - [LearnLib's fork](https://github.com/DocSkellington/learnlib): implements learning algorithms for automata.
  - [JSON schema tools](https://github.com/DocSkellington/JSONSchemaTools): implements a JSON schema validator and document generators.

### Theoretical background
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

See [the validation page](validation.html) for details about our validation algorithm.

## Important pages
  1. [Documentation of the validation algorithm and data structures](validation.html)
  2. [Documentation of the learning benchmarks](learningBenchmarks.html)
  3. [Documentation of the validation benchmarks](validationBenchmarks.html)
  4. [API documentation](api/apidocs/index.html)