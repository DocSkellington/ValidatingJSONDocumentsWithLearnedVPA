#!/bin/bash

schema="vim-addon-info.json"

# Validation
documents=Documents/${schema}/Random
n_experiments=10

vpdas="Results/Learning/RANDOM/DOT/VPA/${schema}*"
for vpda in ${vpdas[@]}
do
    java -Xmx16g -jar validationbenchmarks-1.1-jar-with-dependencies.jar \
        validate ${schema} ${vpda} ${documents} ${n_experiments} \
        > Results/Out/out_${schema}_validating 2> Results/Out/err_${schema}_validating
done