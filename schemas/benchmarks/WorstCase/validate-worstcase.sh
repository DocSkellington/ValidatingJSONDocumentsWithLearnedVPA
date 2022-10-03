#!/bin/bash

if [[ ! $1 ]]
then
    echo "This script excepts the size parameter as its first argument"
    exit 1
fi

numberElements=$1

schema="WorstCase"

# Validation
documents=Documents/${schema}-$1/Random
n_experiments=1

java -Xmx16g -Xlog:gc -jar jsonvalidation-benchmarks-2.0-jar-with-dependencies.jar \
    validate ${schema} ${numberElements} ${documents} ${n_experiments} \
    > Results/Out/out_${schema}_validating 2> Results/Out/err_${schema}_validating