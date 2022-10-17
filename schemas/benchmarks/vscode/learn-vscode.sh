#!/bin/bash

schema="vscode.json"

# Learning
# 8 hours
timeLimit=28800
type=vpa
equivalence=exploration
n_documents=10000
can_invalid=true
max_depth=10
max_prop=5
max_items=2
ignore_additional=false
n_experiments=10

java -Xmx16g -jar jsonlearning-benchmarks-2.0-jar-with-dependencies.jar \
    ${type} ${equivalence} ${timeLimit} ${schema} ${n_documents} \
    ${can_invalid} ${max_depth} ${max_prop} ${max_items}\
    ${ignore_additional} ${n_experiments} \
    > Results/Out/out_${schema}_learning 2> Results/Out/err_${schema}_learning
