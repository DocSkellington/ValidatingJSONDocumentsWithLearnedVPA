#!/bin/env python3

"""
This file reads a CSV file produced by a run of the VPDA learning benchmarks.
"""

import sys
from collections import defaultdict

import pandas
import numpy as np

filepath = sys.argv[1]
timelimit_in_s = int(sys.argv[2])
name = sys.argv[3]
df = pandas.read_csv(filepath)

# Retrieving number of timeouts and errors
column = "Total time (ms)"
n_timeouts = len(df[df[column] == "Timeout"])
n_errors = len(df[df[column] == "Error"])

# Number of queries, sizes of sets/functions, and diameter
columns = [
    "Membership queries",
    "Equivalence queries",
    "Rounds",
    "alphabet size",
    "VPDA size",
    "Internal transitions",
    "Return transitions",
    "Call transitions",
    "Diameter",
]
numbers = df[columns]
numbers = numbers.drop(numbers[numbers[columns[0]] == "Timeout"].index)
numbers = numbers.drop(numbers[numbers[columns[0]] == "Error"].index)
for column in columns:
    numbers[column] = pandas.to_numeric(numbers[column])

numbers_mean = numbers.mean()

# Whether the VPDA contains a bin state
columns = [
    "Bin state",
]
booleans = df[columns]
booleans = booleans.drop(booleans[booleans[columns[0]] == "Timeout"].index)
booleans = booleans.drop(booleans[booleans[columns[0]] == "Error"].index)
number_true_booleans = booleans.sum(axis=0)

# Time
columns = [
    "Total time (ms)",
]
total_time = df[columns]
total_time = total_time.replace(to_replace="Timeout", value=timelimit_in_s * 1000) # * 1000 because we store milliseconds
total_time = total_time.drop(total_time[total_time[columns[0]] == "Error"].index)
for column in columns:
    total_time[column] = pandas.to_numeric(total_time[column])
    # From milliseconds to seconds
    total_time[column] = total_time[column].transform(lambda x: x / 1000)

total_time_mean = total_time.mean()

statistics_df = pandas.DataFrame(
    {
        f"TO ({timelimit_in_s}s)": [n_timeouts],
        "MO (16GB)": [n_errors],
        "Time (s)": [total_time_mean["Total time (ms)"]],
        "Membership": [numbers_mean["Membership queries"]],
        "Equivalence": [numbers_mean["Equivalence queries"]],
        "$|\\automaton|$": [numbers_mean["VPDA size"]],
        "$|\Sigma|$": [numbers_mean["alphabet size"]],
        "$|\delta_i|$": [numbers_mean["Internal transitions"]],
        "$|\delta_{r}|$": [numbers_mean["Return transitions"]],
        "$|\delta_{c}|$": [numbers_mean["Call transitions"]],
        "Diameter": [numbers_mean["Diameter"]],
    }
)

styler = statistics_df.style
styler.format(precision=2)
styler.hide(level=0, axis="index")

styler.to_latex(
    f"figures/{name}.tex",
)