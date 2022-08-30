# Validating JSON schemas using pushdown automata

## Theoretical background
This project implements an currently in-work paper about using pushdown automata to validate JSON documents against a JSON schema.
The idea is to first construct an automaton that abstracts the JSON schema.
This automaton assumes a fixed order over the key-value pairs.
While this brings a rather succinct representation, it contradicts the definition of a JSON object.

The validation algorithm relies on the learned automaton and manipulates it in a way that allows any permutation of the pairs key-value.

The project is split into three parts:
  - A part that implements the validation algorithm.
  - A part that defines the oracles that can be used in a learning experiment.
  - Two benchmarks for the learning and the validation processes.

### Learning a pushdown automaton from a JSON schema

### Validating a JSON document

### Benchmarks

#### Used schemas
All schemas can be donwloaded from ... and were modified to remove all titles, descriptions, and default values. Types were added where missing, and all keys are required.
Removed unsupported keywords (such as `pattern` for strings), and unneeded constraints (such as `"minItems": 0`).

## How to use
In order to use this library, our [JSON schema library](https://github.com/DocSkellington/JSONSchemaTools), and our forks of [AutomataLib](https://github.com/DocSkellington/automatalib) and [LearnLib](https://github.com/DocSkellington/learnlib) must be installed. See the corresponding projects for instructions.

First, to install the validation library, run the following commands (from the root of the repository):
```bash
cd validationByAutomaton/
mvn install
```

Then, to install the learning library (which depends on the validation library):
```bash
cd ../learning
mvn install
```

Finally, the benchmarks can be compiled and packaged. For instance, the learning benchmarks can be packaged as follows (from the root of the repository):
```bash
cd benchmarks/learningAutomata
mvn package
```

An executable JAR file can be found in the `target` directory.

## License
The source code is distributed under Apache License 2.0.

## Maintainer
  * Gaëtan Staquet, F.R.S.-FNRS, University of Mons, and University of Antwerp.