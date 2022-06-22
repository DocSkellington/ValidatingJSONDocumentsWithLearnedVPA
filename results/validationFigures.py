#!/bin/env python3

"""
This file reads a CSV file produced by a run of the VPDA learning benchmarks.
"""

import sys
from collections import defaultdict

import pandas
import pandas.io.formats.style
import numpy as np

filepath = sys.argv[1]
name = sys.argv[2]
df = pandas.read_csv(filepath)

document_description = [
    "Length document",
    "Depth document",
]

numerical_columns = [
    "Automaton time (ms)",
    "Automaton memory",
    "Validator time (ms)",
    "Validator memory"
]
operations = ["mean", "median", "min", "max"]
numerical_agg = {key: operations for key in numerical_columns}

boolean_columns = [
    "Automaton output",
    "Validator output"
]
boolean_agg = {key: ["sum"] for key in boolean_columns}

df_grouped = df.groupby(by="Document ID")

descriptions = df_grouped[document_description].min()
values = df_grouped.agg(numerical_agg)
outputs = df_grouped.agg(boolean_agg)

print(descriptions)
print(values)
print(outputs)

whole_df = pandas.concat([descriptions, values, outputs], axis=1)

print(whole_df)
whole_df.sort_values(by=["Length document"], inplace=True)

whole_grouped = whole_df.groupby(by="Length document").mean().reset_index()

rename_dict = {
    "Length document": "LengthDocument",
    "Depth document": "DepthDocument",
}
rename_dict.update({
    ("{} time (ms)".format(algo), operation): "{}Time{}".format(algo, operation.capitalize())
    for operation in operations
    for algo in ["Automaton", "Validator"]
})
rename_dict.update({
    ("{} memory".format(algo), operation): "{}Memory{}".format(algo, operation.capitalize())
    for operation in operations
    for algo in ["Automaton", "Validator"]
})
rename_dict.update({
    ("{} output".format(algo), "sum"): "{}Output".format(algo)
    for algo in ["Automaton", "Validator"]
})

whole_grouped.rename(rename_dict, axis="columns", inplace=True)

print(whole_grouped)

with open("validation/" + name + ".dat", "w") as f:
    whole_grouped.to_string(f, index=False)