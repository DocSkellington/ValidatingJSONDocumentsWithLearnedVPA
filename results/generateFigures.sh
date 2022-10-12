#!/bin/bash
#
# ValidatingJSONDocumentsWithLearnedVPA - Validation of JSON documents, using a
# learned VPA.
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
#

mkdir -p learning
mkdir -p preprocessing
mkdir -p validation

# Learning results
python3 learningFigures.py LearningResults/EXPLORATION/28800s-basicTypes.json-VPA-10000-true-10-10-2-false-10-28-09-2022-08-51.csv 288000 basicTypes
python3 learningFigures.py LearningResults/EXPLORATION/28800s-recursiveList.json-VPA-10000-true-10-10-2-false-10-21-09-2022-15-46.csv 288000 recursiveList
python3 learningFigures.py LearningResults/EXPLORATION/28800s-vscode.json-VPA-10000-true-10-5-2-false-10-28-09-2022-08-51.csv 288000 vscode

python3 learningFigures.py LearningResults/RANDOM/86400s-proxies.json-VPA-10000-true-6-8-2-false-10-20-09-2022-10-57.csv 86400 proxies
python3 learningFigures.py LearningResults/RANDOM/86400s-vim-addon-info.json-VPA-10000-true-7-9-2-false-10-28-09-2022-14-38.csv 86400 vim
python3 learningFigures.py LearningResults/RANDOM/604800s-codecov.json-VPA-10000-true-6-15-2-false-10-21-09-2022-10-05.csv 604800 codecov

# Preprocessing results
python3 preprocessingFigures.py ValidationResults/basicTypes.json-basicTypes.json-0.dot-1-preprocessing-05-10-2022-11-39.csv basicTypes
python3 preprocessingFigures.py ValidationResults/recursiveList.json-recursiveList.json-0.dot-1-preprocessing-05-10-2022-11-39.csv recursiveList
python3 preprocessingFigures.py ValidationResults/vscode.json-vscode.json-0.dot-1-preprocessing-05-10-2022-11-39.csv vscode

python3 preprocessingFigures.py ValidationResults/proxies.json-proxies.json-0.dot-1-preprocessing-05-10-2022-11-33.csv proxies
python3 preprocessingFigures.py ValidationResults/vim-addon-info.json-vim-addon-info.json-1.dot-1-preprocessing-05-10-2022-11-39.csv vim
python3 preprocessingFigures.py ValidationResults/codecov.json-codecov.json-0.dot-1-preprocessing-05-10-2022-11-40.csv codecov

python3 preprocessingFigures.py ValidationResults/WorstCase-HandWritten-1-preprocessing-05-10-2022-16-20.csv worstcase

# Validation results
python3 validationFigures.py ValidationResults/basicTypes.json-basicTypes.json-0.dot-1-validation-30-09-2022-09-40.csv ValidationResults/basicTypes.json-memory-05-10-2022-11-51.csv basicTypes
python3 validationFigures.py ValidationResults/recursiveList.json-recursiveList.json-0.dot-1-validation-30-09-2022-08-29.csv ValidationResults/recursiveList.json-memory-05-10-2022-11-55.csv recursiveList
python3 validationFigures.py ValidationResults/vscode.json-vscode.json-0.dot-1-validation-03-10-2022-11-59.csv ValidationResults/vscode.json-memory-05-10-2022-12-01.csv vscode

python3 validationFigures.py ValidationResults/proxies.json-proxies.json-0.dot-1-validation-30-09-2022-08-27.csv ValidationResults/proxies.json-memory-05-10-2022-12-42.csv proxies
python3 validationFigures.py ValidationResults/vim-addon-info.json-vim-addon-info.json-1.dot-1-validation-03-10-2022-11-59.csv ValidationResults/vim-addon-info.json-memory-06-10-2022-15-24.csv vim
python3 validationFigures.py ValidationResults/codecov.json-codecov.json-0.dot-1-validation-03-10-2022-16-32.csv ValidationResults/codecov.json-memory-05-10-2022-12-07.csv codecov

python3 validationFigures.py ValidationResults/WorstCase-HandWritten-1-validation-05-10-2022-16-20.csv ValidationResults/WorstCase-10-memory-06-10-2022-13-39.csv worstcase
