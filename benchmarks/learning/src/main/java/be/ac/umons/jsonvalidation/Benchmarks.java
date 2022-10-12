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

package be.ac.umons.jsonvalidation;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

import org.json.JSONObject;
import org.json.JSONTokener;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import be.ac.umons.jsonvalidation.exploration.ExplorationROCABenchmarks;
import be.ac.umons.jsonvalidation.exploration.ExplorationVCABenchmarks;
import be.ac.umons.jsonvalidation.exploration.ExplorationVPDABenchmarks;
import be.ac.umons.jsonvalidation.random.RandomROCABenchmarks;
import be.ac.umons.jsonvalidation.random.RandomVCABenchmarks;
import be.ac.umons.jsonvalidation.random.RandomVPDABenchmarks;
import de.learnlib.api.logging.LearnLogger;

public class Benchmarks {
    private static final LearnLogger LOGGER = LearnLogger.getLogger(Benchmarks.class);

    private enum AutomatonType {
        VCA,
        ROCA,
        VPA
    }

    private enum EquivalenceType {
        RANDOM,
        EXPLORATION
    }

    public static void main(String[] args) throws InterruptedException, IOException, JSONSchemaException {
        final AutomatonType automatonType = AutomatonType.valueOf(args[0].toUpperCase());
        final EquivalenceType equivalenceType = EquivalenceType.valueOf(args[1].toUpperCase());
        final int timeLimit = Integer.valueOf(args[2]);

        final Duration timeout = Duration.ofSeconds(timeLimit);
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
        final LocalDateTime now = LocalDateTime.now();

        final Path filePath = Paths.get(args[3]);
        final String schemaName = filePath.getFileName().toString();
        final int nTests = Integer.valueOf(args[4]);
        final boolean canGenerateInvalid = Boolean.valueOf(args[5]);
        final int maxDocumentDepth = Integer.valueOf(args[6]);
        final int maxProperties = Integer.valueOf(args[7]);
        final int maxItems = Integer.valueOf(args[8]);
        final boolean ignoreAdditionalProperties = Boolean.valueOf(args[9]);
        final int nRepetitions = Integer.valueOf(args[10]);

        final Set<JSONObject> documentsToTest = new LinkedHashSet<>();
        for (int i = 11; i < args.length; i++) {
            JSONObject document = new JSONObject(new JSONTokener(new FileReader(args[i])));
            documentsToTest.add(document);
        }

        final JSONSchemaStore schemaStore = new JSONSchemaStore(ignoreAdditionalProperties);
        final JSONSchema schema;
        try {
            URL url = filePath.toUri().toURL();
            schema = schemaStore.load(url.toURI());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        LOGGER.info("Starting JSON benchmarks with the following parameters:");
        LOGGER.info("Schema name: " + schemaName);
        LOGGER.info("Number of tests for equivalence: " + nTests);
        LOGGER.info("Type of equivalence: " + equivalenceType);
        LOGGER.info("Can generate invalid documents: " + canGenerateInvalid);
        LOGGER.info("Max depth of generated document: " + maxDocumentDepth);
        LOGGER.info("maximum number of properties in an object: " + maxProperties);
        LOGGER.info("maximum number of items in an array: " + maxItems);
        LOGGER.info("Are true additional properties ignored: " + ignoreAdditionalProperties);
        LOGGER.info("Number of repetitions: " + nRepetitions);

        Path pathToDotFiles = Paths.get(System.getProperty("user.dir"), "Results", "Learning",
                equivalenceType.toString(), "DOT", automatonType.toString());
        pathToDotFiles.toFile().mkdirs();

        Path pathToCSVFolder = Paths.get(System.getProperty("user.dir"), "Results", "Learning",
                equivalenceType.toString());
        pathToCSVFolder.toFile().mkdirs();
        // @formatter:off
        Path pathToCSVFile = pathToCSVFolder.resolve("" + timeLimit + "s-"
            + schemaName + "-"
            + automatonType + "-"
            + nTests + "-"
            + canGenerateInvalid + "-"
            + maxDocumentDepth + "-"
            + maxProperties + "-"
            + maxItems + "-"
            + ignoreAdditionalProperties + "-"
            + nRepetitions + "-"
            + dtf.format(now) + ".csv"
        );
        // @formatter:on
        ABenchmarks benchmarks;
        if (automatonType == AutomatonType.VPA) {
            if (equivalenceType == EquivalenceType.RANDOM) {
                benchmarks = new RandomVPDABenchmarks(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems,
                        documentsToTest);
            } else {
                benchmarks = new ExplorationVPDABenchmarks(pathToCSVFile, pathToDotFiles, timeout, maxProperties,
                        maxItems);
            }
        } else if (automatonType == AutomatonType.VCA) {
            if (equivalenceType == EquivalenceType.RANDOM) {
                benchmarks = new RandomVCABenchmarks(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems,
                        documentsToTest);
            } else {
                benchmarks = new ExplorationVCABenchmarks(pathToCSVFile, pathToDotFiles, timeout, maxProperties,
                        maxItems);
            }
        } else {
            if (equivalenceType == EquivalenceType.RANDOM) {
                benchmarks = new RandomROCABenchmarks(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems,
                        documentsToTest);
            } else {
                benchmarks = new ExplorationROCABenchmarks(pathToCSVFile, pathToDotFiles, timeout, maxProperties,
                        maxItems);
            }
        }
        benchmarks.runBenchmarks(schema, schemaName, nTests, canGenerateInvalid, maxDocumentDepth, nRepetitions,
                false);
    }
}