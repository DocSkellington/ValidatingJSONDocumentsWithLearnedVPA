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
package be.ac.umons.jsonvalidation.random;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;
import java.util.Set;

import org.json.JSONObject;

import be.ac.umons.jsonlearning.random.VPDAJSONEquivalenceOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.VPDABenchmarks;
import de.learnlib.api.oracle.EquivalenceOracle;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.VPDAlphabet;

/**
 * Benchmarks based on JSON documents and Schemas.
 * 
 * @author Gaëtan Staquet
 */
public class RandomVPDABenchmarks extends VPDABenchmarks {

    private final Set<JSONObject> documentsToTest;

    public RandomVPDABenchmarks(final Path pathToCSVFile, final Path pathToDotFiles, final Duration timeout,
            int maxProperties, int maxItems, final Set<JSONObject> documentsToTest)
            throws IOException {
        super(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems);
        this.documentsToTest = documentsToTest;
    }

    @Override
    protected EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> getEquivalenceOracle(int numberTests,
            boolean canGenerateInvalid, int maxDocumentDepth, int maxProperties, int maxItems, JSONSchema schema,
            Random random, boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet) {
        return new VPDAJSONEquivalenceOracle(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems,
                schema, random, shuffleKeys, alphabet, documentsToTest);
    }
}
