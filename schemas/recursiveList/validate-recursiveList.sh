#!/bin/bash

schema="recursiveList.json"

# Validation
documents=Documents/${schema}/Random
n_experiments=10

vpdas="Results/Learning/EXPLORATION/DOT/VPA/${schema}*"
for vpda in ${vpdas[@]}
do
    java -Xmx16g -jar jsonvalidation-benchmarks-2.0-jar-with-dependencies.jar \
        validate ${schema} ${vpda} ${documents} ${n_experiments} \
        > Results/Out/out_${schema}_validating 2> Results/Out/err_${schema}_validating
done