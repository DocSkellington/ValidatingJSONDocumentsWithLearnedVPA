#!/bin/bash

schema="chutzpah.json"

# Learning
# 8hours
timeLimit=28800
type=vpa
equivalence=random
n_documents=10000
can_invalid=true
max_depth=4
max_prop=31
max_items=2
ignore_additional=false
n_experiments=10

java -Xmx16g -jar jsonlearning-benchmarks-2.0-jar-with-dependencies.jar \
    ${type} ${equivalence} ${timeLimit} ${schema} ${n_documents} \
    ${can_invalid} ${max_depth} ${max_prop} ${max_items}\
    ${ignore_additional} ${n_experiments} \
    > Results/Out/out_${schema}_learning 2> Results/Out/err_${schema}_learning
