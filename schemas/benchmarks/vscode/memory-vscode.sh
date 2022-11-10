#!/bin/bash

schema="vscode.json"
documents=Documents/${schema}/Random

java -verbose:gc -jar jsonvalidation-benchmarks-2.0-jar-with-dependencies.jar\
    memory ${schema} ${documents}