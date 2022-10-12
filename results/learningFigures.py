#!/bin/env python3

# ValidatingJSONDocumentsWithLearnedVPA - Learning a visibly pushdown automaton
# from a JSON schema, and using it to validate JSON documents.
#
# Copyright 2022 University of Mons, University of Antwerp
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


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
        "Time (s)": [total_time_mean["Total time (ms)"]],
        "Membership": [numbers_mean["Membership queries"]],
        "Equivalence": [numbers_mean["Equivalence queries"]],
        "$|\\automaton|$": [numbers_mean["VPDA size"]],
        "$|\Sigma|$": [numbers_mean["alphabet size"]],
        "$|\delta_{c}|$": [numbers_mean["Call transitions"]],
        "$|\delta_{r}|$": [numbers_mean["Return transitions"]],
        "$|\delta_i|$": [numbers_mean["Internal transitions"]],
        "Diameter": [numbers_mean["Diameter"]],
    }
)

styler = statistics_df.style
styler.format(precision=1)
styler.hide(level=0, axis="index")

styler.to_latex(
    f"learning/{name}.tex",
)