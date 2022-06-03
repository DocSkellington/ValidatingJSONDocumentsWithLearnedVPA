#!/bin/bash

# Learning
# 4hours
timeLimit=14400
type=vpa
equivalence=exploration
n_documents=10000
can_invalid=true
max_depth=20
n_experiments=10
shuffle=false
max_prop=20
max_items=20
ignore_additional=false

declare -a files=(
    "babelrc.json"
    "basicTypes.json"
    "codecov.json"
    "docker-compose.json"
    "jsonresume.json"
    "minecraft-biome.json"
    "recursiveList.json"
    "vscode.json"
)

# Validation
n_documents_validation=10000

for schema in ${files[@]}
do
    documents=${schema}-documents/Random
    mkdir -p ${documents}

    java -Xmx16g -jar learningautomata-2.0-jar-with-dependencies.jar \
        ${type} ${equivalence} ${timeLimit} ${schema} ${n_documents} \
        ${can_invalid} ${max_depth} ${n_experiments} ${shuffle} \
        ${max_prop} ${max_items} ${ignore_additional} \
        > Results/Out/out_${schema}_learning 2> Results/Out/err_${schema}_learning

    vpdas="Results/JSON/Dot/${schema}*"

    java -Xmx16g -jar validationbenchmarks-1.1-jar-with-dependencies.jar \
        generate ${schema} random ${documents} ${n_documents_validation} \
        ${can_invalid} ${max_depth} ${max_prop} ${max_items} ${ignore_additional} \
        > Results/Out/out_${schema}_generating 2> Results/Out/err_${schema}_generating

    java -Xmx16g -jar validationbenchmarks-1.1-jar-with-dependencies.jar \
        validate ${schema} ${vpdas} ${documents} ${n_experiments} \
        > Results/Out/out_${schema}_validating 2> Results/Out/err_${schema}_validating
done