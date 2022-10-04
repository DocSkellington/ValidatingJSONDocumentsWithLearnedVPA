---
title: Validation benchmarks 
layout: pageMath
---

In this page, we discuss the benchmarks comparing the classical validation algorithm (see [our implementation](https://github.com/DocSkellington/JSONSchemaTools)), and our new algorithm based on learned VPAs.
For some details on our validation algorithm, please refer to the [validation page](validation.html).

## Schemas
The schemas considered for the [learning benchmarks](learningBenchmarks.html#schemas) are also used for the validation benchmarks.
On top of those six schemas, we add a new one, called *WorstCase*, specifically created to highlight the differences between the classical algorithm and ours.

### WorstCase schema
The idea behind the *WorstCase* schema is to force the classical algorithm to explore multiple parts of the documents in vain.
More precisely, we create a conjunction that contains multiple disjunctions.
Only the last part of each disjunction is present in all disjunctions, i.e., once we apply the conjunction of disjunctions, this is the only possible schema.

The *WorstCase* schema is described by the following context-free grammar, with $$\ell \geq 1$$:

$$
  \begin{array}{lcll}
    S &:=& S_1 \land S_2 \land \dotsb \land S_{\ell}&\\
    S_i &:=& R_i \lor R_{i+1} \lor \dotsb \lor R_{\ell} &\forall i \in \{1, \dotsc, \ell\}\\
    R_i &:=& \{ k_i \verb!s!, k_{i+1} \verb!s!, \dotso, k_{\ell} \verb!s! \} &\forall i \in \{1, \dotsc, \ell\}
  \end{array}
$$

Notice the difference between $$S_i$$ and $$S_{i+1}$$: $$R_i$$ is removed from $$S_i$$ to get $$S_{i+1}$$.
Hence an equivalent grammar is $$S := R_\ell$$.
On the one hand, the classical validator has to explore each $$S_i$$ in the conjunction, and each $$R_j$$ in the related disjunction, before finally considering $$S_\ell$$ and thus $$R_{\ell}$$.
On the other hand, as we learn minimal 1-SEVPAs, we can here easily construct a 1-SEVPA directly for the simplified grammar $$S := R_{\ell}$$.
This example is a worst-case for the classical validator, while being easy to handle for our algorithm, as shown in the empirical results (see our paper).

{% include note.html content="Notice that the depth of any valid document is one, for this schema." %}

### Generation of documents
In order to compare both algorithms, we need documents to validate.
Thanks to our [JSON schema library](https://github.com/DocSkellington/JSONSchemaTools), we can easily generate those needed documents.
More precisely, we generate $$X$$ documents thanks to a random valid generator, and $$X$$ documents with a random invalid generator (note that the invalid generator can still produce valid documents), with $$X$$ a parameter fixed at runtime.

The *WorstCase* schema follows a different generation principle.
Let $$\ell$$ be the schema parameter.
Then, we generate all possible subsets of $$\{1, \dotsc, \ell\}$$ and, for each subset $$S$$, we construct two documents:
  1. One where the set of keys is exactly $$S$$.
  2. One where the set of keys is $$S$$ and an additional properties (with our abstraction, the symbol `"\S"`).

## Comparison of validation algorithms
Once we have the documents, we can validate them using both algorithms.

### Preprocessing step
Since our validation algorithm requires an additional data structure, we first need to compute the key graph.
To do so, at the start of each experiment (a repetition of the validation of the whole set of documents), we measure the time and memory needed to compute the following, with $$A$$ the considered VPA:
  1. The reachability relation $$\mathrm{Reach}_A$$.
  2. A relation derived from $$A$$ that is used to determine the bin location of $$A$$.
  3. The key graph. We also measure the memory needed to store the graph, on top of the memory to compute it.

### Validating documents
Once the key graph is computed and algorithms initialized, we can validate every document.
Since we want to measure the time and the memory needed for an execution, we actually have to execute each algorithm twice.
The first time, we **clean the memory** (by requesting that Java's collects and destroys all unused objects) to get a close approximation of the memory required.
The second time, we **do not clean the memory**, which allows us to measure the time required to process a document.

For our new algorithm, on top of the time needed for the whole validation process, we measure the time taken when seeking paths in the key graph, as well as the time consumed by the computation of the new set of states when closing an object and an array.
Once more, the reader is invited to read the article for theoretical details.

Finally, to produce figures that show a fair comparison between both algorithms, we need to know how much memory is required to store the whole document.
Indeed, the classical algorithm expects the complete document, unlike our algorithm.
This measurement is not done alongside the validation, as explained below.

## Benchmarks
Let us now discuss the inputs and outputs of our validation benchmarks.
The JAR file produced when packaging the validation benchmarks can perform multiple tasks, depending on the first argument.
Its value and the subsequent arguments are now detailed.
In the following, the lines in bold describe mandatory arguments.
Moreover, arguments are described **in the order they are expected**.

Note that the scripts we used for our benchmarks are provided in the `schemas/benchmarks` directory, alongside the schemas.

{% include important.html content="For reason detailed below, the schema name *WorstCase* (with this exact case) is considered as a key word and can not be used for a new schema." %}

### Generating documents
#### Input
To generate documents, **the first argument must be `GENERATE`**.
The remaining arguments are as follows:

{:start="2"}
  2. **The path to the file containing the schema**.
  3. **The type of generator to use: RANDOM or EXPLORATION**. In our benchmarks, we only used RANDOM.
  4. **The path to the folder in which to write the documents**.
  5. **The number of documents to generate *for the valid and invalid generators***.
    This is the value for $$X$$ explained [above](#generation-of-documents).
    So, the total number of documents is twice this value.
  6. **The maximal depth of the generated documents**.
  7. **The maximal number of properties inside an object for the generated documents**.
  8. **The maximal number of items inside an array for the generated documents**.
  9. **A Boolean**. If true, the schema associated to missing `additionalProperties` is considered as the `false` schema, reducing the number of valid documents.
    If false, such a schema is considered as a `true` schema, following the specifications.
  
Moreover, if the path to the schema is *WorstCase* (the case matters), a new mandatory argument is added:

{:start="10"}
  10. **The value of $$\ell$$**, i.e., the number of disjunctions inside the conjunction.

#### Output
Each document will be written in a file in the directory `Documents/SCHEMA/GENERATOR` where `SCHEMA` is replaced by the actual schema name, and `GENERATOR` by `RANDOM` or `EXPLORATION`, according to the input arguments.
During execution, the process will print its progress, and an error message, if a document can not be generated.

### Validation
#### Input
To validate a set of documents on both algorithms, **the first argument must be `VALIDATE`**.
The remaining arguments are as follows, in this order:

{:start="2"}
  2. **The path to the file containing the schema**.

If the path to the schema is *WorstCase*, the next argument is

{:start="3"}
  3. **The value of $$\ell$$**.

Otherwise, it is

{:start="3"}
  3. **The path to the file containing the VPA**.

The remaining two arguments must be present in both cases:

{:start="4"}
  4. **The path to the directory containing the documents to test**.
  5. **The number of times the whole process must be performed**.
    This includes both preprocessing and validation of all the documents steps.

#### Output
Two CSV files will be produced in the directory `Results/Validation`.
This file containing the word `preprocessing` in its name has the following columns:

  1. Whether it was possible to construct the key graph for the provided 1-SEVPA.
  2. The time needed to compute the reachability relation.
  3. The memory needed to compute the reachability relation.
  4. The number of elements inside the reachability relation.
  5. The time needed to compute the relation used to detect the bin state.
  6. The memory needed to compute the relation used to detect the bin state.
  7. The number of elements in this relation.
  8. The time needed to compute the key graph, excluding the time for the relations.
  9. The memory needed to **compute** the key graph, excluding the memory for the relations.
  10. The memory needed to **store** the key graph, once it is computed.
  11. The number of vertices in the key graph.

This file containing the word `validation` in its name has the following columns:

  1. The document's name.
  2. The length of the document, in the number of alphabet symbols.
  3. The depth of the document.
  4. The time taken by our algorithm.
  5. The memory taken by our algorithm.
  6. The output of our algorithm.
  7. Among all computations of a set that contains all the paths in the key graph that match with the JSON object we are closing (i.e., it is a computation step when reading `}`), the time taken by the longest computation.
  8. The total time taken by all computations of this set.
  9. The number of times this set was computed.
  10. The maximal time taken when creating the new set of states after reading a `}` and the object contains a list one key.
  11. The total time taken by all computations of this set.
  12. The number of times this set was computed.
  13. The maximal time taken when creating the new set of states after reading a `]`, or when reading a `}` and the object is empty.
  14. The total time taken by all computations of this set.
  15. The number of times this set was computed.
  16. The time taken by the classical algorithm.
  17. The memory taken by the classical algorithm.
  18. The output of the classical algorithm.

Moreover, information will be printed in the standard output to show the current progress.
If the provided scripts are used as-is, Java will also print information about the garbage collector executions.

#### Figures
Finally, let us highlight the fact that we provide Python scripts to generate figures to display both preprocessing and validation results.
The validation results are given in a data file that is to be used with some graphic generator programs, such as LaTeX (with the PGFPlots library).
For licensing reasons, we can not provide the LaTeX files we use in our paper.
So, we give here the main points, in case you desire to recreate them, using [PGFPlots](https://ctan.org/pkg/pgfplots):

  1. We create an axis environment where we setup the labels and ticks for both X- and Y-axes:

      ```latex
\begin{axis}[
  xlabel=Document size,
  ylabel=Time (ms),
  grid=major,
  scaled ticks=false,
  xtick={0, 20000, 40000, 60000},
  xticklabels={0, $20$K, $40$K, $60$K},
]
...
      ```

  2. We call the function `\addplot+`, setup the color and shape of the marks, and load the data from the file, selecting the appropriate column(s):

      ```latex
\addplot+[
  blue,
  mark=x,
  only marks,
]
  table [
    y={AutomatonTimeMean},
  ]
    {vscode.dat}
;
\addplot+[
  red,
  only marks,
  mark=o,
]
  table [
    y={ValidatorTimeMean},
  ]
    {vscode.dat}
;
      ```

### Depth of a schema
Scripts for this kind of execution are not provided.

#### Input
If you desire to know the maximal depth of the documents described by a schema (to determine an appropriate upper bound for the learning process or to generate documents, for instance), **the first argument must be `DEPTH`**.
The only remaining argument is:

{:start="2"}
  2. **The path to the file containing the schema**.

Note that this does **not** support the *WorstCase* schema, for which the maximal depth is always one.

#### Output
A single number will be printed in the standard output.

### Memory to store documents
#### Input
Finally, the last major part of the JAR file is to measure the memory required to hold a document.
**The first argument must be `MEMORY`**.
The remaining arguments are:

{:start="2"}
  2. **The path to the file containing the schema**.
  3. **The path to the folder containing the documents**.

Moreover, if the path to the schema is *WorstCase* (the case matters), a new mandatory argument is added:

{:start="4"}
  4. **The value of $$\ell$$**.

#### Output
A CSV file will be produced in the directory `Results/Validation`.
This file has the following columns:

  1. The document's name.
  2. The memory required to store the document.
  3. The length of the document, in the number of alphabet symbols.

### Reproducible results
As pointed out in the [remarks of the validation documentation](validation.html#remarks), we guarantee reproducible results.

For the generation of documents, we fix the seed of the random generator at the start of the execution, meaning that we always get the same documents.
Since the validation algorithms are not random, we immediately have the reproducible results thanks to our use of `LinkedHashSet` and `LinkedHashMap`.