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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.base.Stopwatch;
import com.google.common.testing.GcFinalization;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonvalidation.graph.KeyGraph;
import be.ac.umons.jsonvalidation.graph.KeyGraphToDot;
import be.ac.umons.jsonvalidation.graph.OnAcceptingPathRelation;
import be.ac.umons.jsonvalidation.graph.ReachabilityRelation;
import de.learnlib.api.logging.LearnLogger;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

public class ValidationBenchmarks {
    private static final LearnLogger LOGGER = LearnLogger.getLogger(ValidationBenchmarks.class);

    private final CSVPrinter preprocessingCSVPrinter;
    private final CSVPrinter validationCSVPrinter;
    private final int nPreprocessingColumns;
    private final int nValidationColumns;
    private final JSONSchema schema;
    private final DefaultOneSEVPA<JSONSymbol> vpa;
    private final Path pathToDocuments;
    private final int nExperiments;

    public ValidationBenchmarks(final Path pathToPreprocessingCSVFile, final Path pathToValidationCSVFile,
            final JSONSchema schema, final DefaultOneSEVPA<JSONSymbol> vpa, final Path pathToDocuments,
            final int nExperiments) throws IOException {
        this.preprocessingCSVPrinter = new CSVPrinter(new FileWriter(pathToPreprocessingCSVFile.toFile()),
                CSVFormat.DEFAULT);
        this.validationCSVPrinter = new CSVPrinter(new FileWriter(pathToValidationCSVFile.toFile()), CSVFormat.DEFAULT);
        this.schema = schema;
        this.vpa = vpa;
        this.pathToDocuments = pathToDocuments;
        this.nExperiments = nExperiments;

        final List<String> preprocessingHeader = getPreprocessingHeader();
        this.nPreprocessingColumns = preprocessingHeader.size();
        preprocessingCSVPrinter.printRecord(preprocessingHeader);
        preprocessingCSVPrinter.flush();
        ;

        final List<String> validationHeader = getValidationHeader();
        this.nValidationColumns = validationHeader.size();
        validationCSVPrinter.printRecord(validationHeader);
        validationCSVPrinter.flush();
    }

    private List<String> getPreprocessingHeader() {
        // @formatter:off
        return List.of(
            "Success",
            "Reachability time",
            "Reachability memory",
            "Reachability size",
            "OnPath time",
            "OnPath memory",
            "OnPath size",
            "Graph time",
            "Graph compute memory",
            "Graph store memory",
            "Graph size"
        );
        // @formatter:on
    }

    private List<String> getValidationHeader() {
        // @formatter:off
        return List.of(
            "Document ID",
            "Length document",
            "Depth document",
            "Automaton time (ms)",
            "Automaton memory",
            "Automaton output",
            "Paths max time",
            "Paths total time",
            "Paths number",
            "Successor object max time",
            "Successor object total time",
            "Successor object number",
            "Successor array max time",
            "Successor array total time",
            "Successor array number",
            "Validator time (ms)",
            "Validator memory",
            "Validator output"
        );
        // @formatter:on
    }

    public void runBenchmarks() throws JSONException, IOException, JSONSchemaException {
        for (int experimentId = 0; experimentId < nExperiments; experimentId++) {
            LOGGER.info((experimentId + 1) + " / " + nExperiments);

            ValidationByAutomaton<Location> automaton = constructAutomaton(vpa);
            if (automaton == null) {
                return;
            }

            File[] listFiles = pathToDocuments.toFile().listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                LOGGER.info("File " + (i + 1) + " / " + listFiles.length);
                final File file = listFiles[i];
                if (file.isFile()) {
                    final JSONObject document = new JSONObject(new JSONTokener(new FileReader(file)));
                    runExperiment(automaton, schema, document, file.getName());
                }
            }
        }
    }

    private ValidationByAutomaton<Location> constructAutomaton(DefaultOneSEVPA<JSONSymbol> vpa) throws IOException {
        System.gc();
        long memoryAtStart = getMemoryUse();
        final Stopwatch watch = Stopwatch.createStarted();

        final ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(vpa, false);

        final long memoryForReachability = getMemoryUse() - memoryAtStart;
        final long timeReachability = watch.stop().elapsed().toMillis();

        System.gc();
        memoryAtStart = getMemoryUse();
        watch.reset().start();
        final OnAcceptingPathRelation<Location> onAcceptingPathRelation = OnAcceptingPathRelation.computeRelation(vpa,
                reachabilityRelation, false);

        final long memoryForAcceptingPath = getMemoryUse() - memoryAtStart;
        final long timeAcceptingPath = watch.stop().elapsed().toMillis();

        System.gc();
        memoryAtStart = getMemoryUse();
        watch.reset().start();
        final KeyGraph<Location> graph = new KeyGraph<>(vpa, reachabilityRelation, onAcceptingPathRelation, false);

        final long memoryToComputeGraph = getMemoryUse() - memoryAtStart;
        System.gc();
        final long memoryToStoreGraph = getMemoryUse() - memoryAtStart;
        final long timeGraph = watch.stop().elapsed().toMillis();

        final StringBuilder builder = new StringBuilder();
        KeyGraphToDot.write(graph, builder);
        LOGGER.logModel(builder);

        final List<Object> statistics = new ArrayList<>(nPreprocessingColumns);
        if (!graph.isValid()) {
            LOGGER.error("The automaton can not be used for our algorithm");

            statistics.add(false);
        } else {
            statistics.add(true);
        }
        statistics.add(timeReachability);
        statistics.add(memoryForReachability);
        statistics.add(reachabilityRelation.size());

        statistics.add(timeAcceptingPath);
        statistics.add(memoryForAcceptingPath);
        statistics.add(onAcceptingPathRelation.size());

        statistics.add(timeGraph);
        statistics.add(memoryToComputeGraph);
        statistics.add(memoryToStoreGraph);
        statistics.add(graph.size());

        preprocessingCSVPrinter.printRecord(statistics);
        preprocessingCSVPrinter.flush();

        if (graph.isValid()) {
            return new ValidationByAutomaton<>(vpa, graph);
        } else {
            return null;
        }
    }

    private void runExperiment(final ValidationByAutomaton<Location> automaton,
            final JSONSchema schema, final JSONObject document, final String documentName)
            throws IOException, JSONSchemaException {
        final Word<JSONSymbol> word = WordConversion.fromJSONDocumentToJSONSymbolWord(document, false, new Random());
        assert word.length() != 0;

        // First, we measure the memory
        automaton.resetTimeAndNumber();
        LOGGER.info("Starting own validator for memory");
        GcFinalization.awaitFullGc();
        Pair<Boolean, Long> automatonResult = runValidationByAutomaton(automaton, word, true);
        final boolean automatonOutput = automatonResult.getFirst();
        final long automatonMemory = automatonResult.getSecond();

        GcFinalization.awaitFullGc();
        boolean validatorOutput;
        boolean validatorError;
        ClassicalValidator validator = new ClassicalValidator(true);
        LOGGER.info("Starting classical validator for memory");
        try {
            validatorOutput = validator.validate(schema, document);
            validatorError = false;
        } catch (JSONSchemaException e) {
            validatorOutput = false;
            validatorError = true;
        }
        final long validatorMemory = validator.getMaxMemoryUsed();

        // Second, we measure the time
        LOGGER.info("Starting own validator for time");
        final Stopwatch watch = Stopwatch.createStarted();
        automatonResult = runValidationByAutomaton(automaton, word, false);
        final long automatonTime = watch.stop().elapsed().toMillis();
        assert automatonResult.getFirst() == automatonOutput;

        validator = new ClassicalValidator(false);
        LOGGER.info("Starting classical validator for time");
        watch.reset().start();
        try {
            validator.validate(schema, document);
            assert !validatorError;
        } catch (JSONSchemaException e) {
            assert validatorError;
        }
        final long validatorTime = watch.stop().elapsed().toMillis();

        final List<Object> statistics = new ArrayList<>(nValidationColumns);

        statistics.add(documentName);

        statistics.add(word.length());
        statistics.add(depthDocument(word));

        statistics.add(automatonTime);
        statistics.add(automatonMemory);
        statistics.add(automatonOutput);

        statistics.add(automaton.getMaximalTimePathsKeyGraph());
        statistics.add(automaton.getTotalTimePathsKeyGraph());
        statistics.add(automaton.getNumberOfTimesPathsKeyGraphComputed());

        statistics.add(automaton.getMaximalTimeSuccessorObject());
        statistics.add(automaton.getTotalTimeSuccessorObject());
        statistics.add(automaton.getNumberOfTimesSuccessorObject());

        statistics.add(automaton.getMaximalTimeSuccessorArray());
        statistics.add(automaton.getTotalTimeSuccessorArray());
        statistics.add(automaton.getNumberOfTimesSuccessorArray());

        statistics.add(validatorTime);
        statistics.add(validatorMemory);
        if (validatorError) {
            statistics.add("Error");
        } else {
            statistics.add(validatorOutput);
        }

        validationCSVPrinter.printRecord(statistics);
        validationCSVPrinter.flush();
    }

    private Pair<Boolean, Long> runValidationByAutomaton(final ValidationByAutomaton<Location> automaton,
            final Word<JSONSymbol> word, boolean measureMemory) {
        final long memoryStart;
        long maxMemory;

        if (measureMemory) {
            memoryStart = getMemoryUse();
            maxMemory = memoryStart;
        } else {
            memoryStart = maxMemory = 0;
        }

        if (word.isEmpty() || !word.getSymbol(0).equals(JSONSymbol.openingCurlyBraceSymbol)) {
            return Pair.of(false, 0L);
        }
        JSONSymbol currentSymbol = word.getSymbol(0);
        ValidationState<Location> validationState = automaton.getInitialState();
        for (int i = 1; i < word.size(); i++) {
            if (measureMemory) {
                System.gc();
            }
            final JSONSymbol nextSymbol = word.getSymbol(i);

            validationState = automaton.getSuccessor(validationState, currentSymbol, nextSymbol);

            currentSymbol = nextSymbol;

            if (measureMemory) {
                maxMemory = Math.max(maxMemory, getMemoryUse());
            }
        }

        if (measureMemory) {
            System.gc();
        }
        validationState = automaton.getSuccessor(validationState, currentSymbol, null);
        if (measureMemory) {
            maxMemory = Math.max(maxMemory, getMemoryUse());
        }

        return Pair.of(automaton.isAccepting(validationState), maxMemory - memoryStart);
    }

    private int depthDocument(final Word<JSONSymbol> word) {
        int depth = 0;
        int maxDepth = 0;
        for (JSONSymbol symbol : word) {
            if (symbol.equals(JSONSymbol.openingCurlyBraceSymbol) || symbol.equals(JSONSymbol.openingBracketSymbol)) {
                depth++;
                maxDepth = Math.max(maxDepth, depth);
            } else if (symbol.equals(JSONSymbol.closingCurlyBraceSymbol)
                    || symbol.equals(JSONSymbol.closingBracketSymbol)) {
                depth--;
                assert depth >= 0;
            }
        }
        return maxDepth;
    }

    public static long getMemoryUse() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
    }
}
