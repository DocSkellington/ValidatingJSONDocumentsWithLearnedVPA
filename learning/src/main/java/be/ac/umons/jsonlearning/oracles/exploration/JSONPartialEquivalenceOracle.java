/* Copyright (C) 2021 – University of Mons, University Antwerpen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ac.umons.jsonlearning.oracles.exploration;

import java.util.Random;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;

/**
 * Partial equivalence oracle for JSON documents.
 * 
 * It tests whether the provided ROCA accepts the same sets of JSON documents
 * than the JSON Schema, up to a tree depth that depends on the counter limit.
 * 
 * @author Gaëtan Staquet
 */
public class JSONPartialEquivalenceOracle extends AbstractExplorationJSONConformance<DFA<?, JSONSymbol>>
        implements EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> {

    public JSONPartialEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxProperties, int maxItems,
            JSONSchema schema, Random random, boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, 0, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
    }

    @Override
    public void setCounterLimit(int counterLimit) {
        setMaximalDocumentDepth(counterLimit);
    }
}
