#!/bin/bash

schema="WorstCase"
numberElements=10

# Validation
documents=Documents/${schema}-$numberElements/Random
n_experiments=1

java -Xmx16g -Xlog:gc -jar jsonvalidation-benchmarks-2.0-jar-with-dependencies.jar \
    validate ${schema} ${numberElements} ${documents} ${n_experiments} \
    > Results/Out/out_${schema}_validating 2> Results/Out/err_${schema}_validating