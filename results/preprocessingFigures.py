#!/bin/env python3

"""
This file reads a CSV file produced by a run of the VPDA learning benchmarks.
"""

import sys
from collections import defaultdict

import pandas
import numpy as np

filepath = sys.argv[1]
name = sys.argv[2]
df = pandas.read_csv(filepath)

# Numerical values
columns = [
    "Reachability time",
    "Reachability memory",
    "Reachability size",
    "OnPath time",
    "OnPath memory",
    "OnPath size",
    "Graph time",
    "Graph memory",
    "Graph size",
]
numbers = df[columns]
numbers = numbers.drop(numbers[numbers[columns[0]] == "Timeout"].index)
numbers = numbers.drop(numbers[numbers[columns[0]] == "Error"].index)
for column in columns:
    numbers[column] = pandas.to_numeric(numbers[column])

numbers_mean = numbers.mean()

# Whether the key graph is correct
columns = [
    "Success",
]
booleans = df[columns]
booleans = booleans.drop(booleans[booleans[columns[0]] == "Timeout"].index)
booleans = booleans.drop(booleans[booleans[columns[0]] == "Error"].index)
number_true_booleans = booleans.sum(axis=0)

statistics_df = pandas.DataFrame(
    {
        "$\\accRelation$ time (s)": [numbers_mean["Reachability time"]],
        "$\\accRelation$ memory (kB)": [numbers_mean["Reachability memory"]],
        "$|\\accRelation|$": [numbers_mean["Reachability size"]],
        "$\\keyGraph$ time (s)": [numbers_mean["OnPath time"] + numbers_mean["Graph time"]],
        "$\\keyGraph$ memory (kB)": [numbers_mean["OnPath memory"] + numbers_mean["Graph memory"]],
        "$|\\keyGraph|$": [numbers_mean["Graph size"]]
    }
)

styler = statistics_df.style
styler.format(precision=1)
styler.hide(level=0, axis="index")

styler.to_latex(
    f"preprocessing/{name}.tex",
)