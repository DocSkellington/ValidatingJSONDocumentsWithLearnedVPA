#!/bin/bash

schema="WorstCase"
numberElements=10
documents=Documents/${schema}-$numberElements/Random

java -verbose:gc -jar jsonvalidation-benchmarks-2.0-jar-with-dependencies.jar\
    memory ${schema} ${documents}