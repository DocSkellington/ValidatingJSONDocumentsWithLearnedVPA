#!/bin/bash

schema="basicTypes.json"

# Validation
documents=Documents/${schema}/Random
n_experiments=1

vpda=Results/Learning/EXPLORATION/DOT/VPA/${schema}-0.dot
java -Xmx16g -Xlog:gc -jar jsonvalidation-benchmarks-2.0-jar-with-dependencies.jar \
    validate ${schema} ${vpda} ${documents} ${n_experiments} \
    > Results/Out/out_${schema}_validating 2> Results/Out/err_${schema}_validating