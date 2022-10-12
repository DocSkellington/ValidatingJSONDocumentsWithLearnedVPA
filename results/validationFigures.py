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
This file reads a CSV file produced by a run of the VPDA learning benchmarks, and a CSV file for the memory needed to store the documents.
"""

import sys
from collections import defaultdict

import pandas
import pandas.io.formats.style
import numpy as np

filepath_validation = sys.argv[1]
filepath_memoryDocuments = sys.argv[2]
name = sys.argv[3]
df_validation = pandas.read_csv(filepath_validation)
df_memoryDocuments = pandas.read_csv(filepath_memoryDocuments)

df = df_memoryDocuments.merge(df_validation)

df["Validator memory"] += df["Memory document"]

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