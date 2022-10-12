/*
 * ValidatingJSONDocumentsWithLearnedVPA - Learning a visibly pushdown automaton
 * from a JSON schema, and using it to validate JSON documents.
 *
 * Copyright 2022 University of Mons, University of Antwerp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.ac.umons.jsonvalidation.random;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;
import java.util.Set;

import org.json.JSONObject;

import be.ac.umons.jsonlearning.random.JSONPartialEquivalenceOracle;
import be.ac.umons.jsonlearning.random.VCAJSONEquivalenceOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.VCABenchmarks;
import de.learnlib.api.oracle.EquivalenceOracle.RestrictedAutomatonEquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.VCAEquivalenceOracle;
import net.automatalib.words.VPDAlphabet;

public class RandomVCABenchmarks extends VCABenchmarks {

    private final Set<JSONObject> documentsToTest;

    public RandomVCABenchmarks(final Path pathToCSVFile, final Path pathToDotFiles, final Duration timeout,
            int maxProperties, int maxItems, final Set<JSONObject> documentsToTest)
            throws IOException {
        super(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems);
        this.documentsToTest = documentsToTest;
    }

    @Override
    protected RestrictedAutomatonEquivalenceOracle<JSONSymbol> getRestrictedAutomatonEquivalenceOracle(int nTests,
            boolean canGenerateInvalid, int maxProperties, int maxItems, JSONSchema schema, Random rand,
            boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet) {
        return new JSONPartialEquivalenceOracle(nTests, canGenerateInvalid, maxProperties, maxItems, schema, rand,
                shuffleKeys, alphabet);
    }

    @Override
    protected VCAEquivalenceOracle<JSONSymbol> getEquivalenceOracle(int nTests, boolean canGenerateInvalid,
            int maxDocumentDepth, int maxProperties, int maxItems, JSONSchema schema, Random rand, boolean shuffleKeys,
            VPDAlphabet<JSONSymbol> alphabet) {
        return new VCAJSONEquivalenceOracle(nTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems,
                schema, rand, shuffleKeys, alphabet, documentsToTest);
    }
}
