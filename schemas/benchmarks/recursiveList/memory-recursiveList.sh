#!/bin/bash

schema="recursiveList.json"
documents=Documents/${schema}/Random

java -Xlog:gc -jar jsonvalidation-benchmarks-2.0-jar-with-dependencies.jar\
    memory ${schema} ${documents}