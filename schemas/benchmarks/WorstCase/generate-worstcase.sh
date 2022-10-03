#!/bin/bash

if [[ ! $1 ]]
then
    echo "This script excepts the size parameter as its first argument"
    exit 1
fi

numberElements=$1

schema="WorstCase"

# Generation of files for validation

documents=Documents/${schema}-$1/Random
mkdir -p ${documents}

n_documents_validation=5000
max_depth=20
max_prop=20
max_items=20
ignore_additional=false

java -Xmx16g -jar jsonvalidation-benchmarks-2.0-jar-with-dependencies.jar \
    generate ${schema} random ${documents} ${n_documents_validation} \
    ${max_depth} ${max_prop} ${max_items} ${ignore_additional} ${numberElements} \
    > Results/Out/out_${schema}_generating 2> Results/Out/err_${schema}_generating