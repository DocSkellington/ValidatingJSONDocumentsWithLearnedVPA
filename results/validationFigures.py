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
    "Validator memory",
    "Paths max time",
    "Paths total time",
    "Paths number",
    "Successor object max time",
    "Successor object total time",
    "Successor object number",
    "Successor array max time",
    "Successor array total time",
    "Successor array number",
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
number_different_outputs=(outputs["Automaton output", "sum"] - outputs["Validator output", "sum"]).abs()
print(number_different_outputs[number_different_outputs != 0])

whole_df = pandas.concat([descriptions, values, outputs, number_different_outputs], axis=1)
whole_df.rename({0: "DifferenceOutputs"}, axis="columns", inplace=True)

whole_df.sort_values(by=["Length document"], inplace=True)

whole_df["Count"] = whole_df.groupby(by="Length document")["Length document"].transform("count")

whole_grouped = whole_df.groupby(by="Length document").mean().reset_index()
whole_grouped["Count"] = whole_grouped["Count"].astype(int)

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
rename_dict.update({
    ("Paths {}".format(type_paths), operation): "Paths{}{}".format(type_paths.capitalize().replace(" ", ""), operation.capitalize())
    for operation in operations
    for type_paths in ["max time", "total time", "number"]
})
rename_dict.update({
    ("Successor {} {}".format(type_object, type_operation), operation): "Successor{}{}{}".format(type_object.capitalize(), type_operation.capitalize().replace(" ", ""), operation.capitalize())
    for operation in operations
    for type_object in ["object", "array"]
    for type_operation in ["max time", "total time", "number"]
})

whole_grouped.rename(rename_dict, axis="columns", inplace=True)

with open("validation/" + name + ".dat", "w") as f:
    whole_grouped.to_string(f, index=False)