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
package be.ac.umons.vpdajson;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Stopwatch;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import be.ac.umons.jsonroca.JSONSymbol;
import be.ac.umons.vpdajson.algorithm.GrowingVPDAlphabet;
import be.ac.umons.vpdajson.oracles.JSONEquivalenceOracle;
import be.ac.umons.vpdajson.oracles.JSONMembershipOracle;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.lstar.roca.LStarROCA;
import de.learnlib.algorithms.lstar.roca.ROCAExperiment;
import de.learnlib.algorithms.ttt.vpda.TTTLearnerVPDA;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.oracle.CounterEQOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.util.Experiment;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.VPDAlphabet.SymbolType;
import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;

/**
 * Benchmarks based on JSON documents and Schemas.
 * 
 * @author Gaëtan Staquet
 */
public class JSONBenchmarks {
    private final CSVPrinter csvPrinter;
    private final int nColumns;
    private final Duration timeout;

    public JSONBenchmarks(final Path pathToCSVFile, final Duration timeout) throws IOException {
        csvPrinter = new CSVPrinter(new FileWriter(pathToCSVFile.toFile()), CSVFormat.DEFAULT);
        // @formatter:off
        List<String> header = Arrays.asList(
            "Total time (ms)",
            "ROCA counterexample time (ms)",
            "DFA counterexample time (ms)",
            "Learning DFA time (ms)",
            "Table time (ms)",
            "Finding descriptions (ms)",
            "Membership queries",
            "Counter value queries",
            "Partial equivalence queries",
            "Equivalence queries",
            "Rounds",
            "Result alphabet size",
            "Result ROCA size"
        );
        // @formatter:on
        this.nColumns = header.size();
        csvPrinter.printRecord(header);
        this.timeout = timeout;
        csvPrinter.flush();
    }

    public void runBenchmarks(final Random rand, final Schema schema, final String schemaName, final SchemaStore schemaStore, final int nTests,
            final int nRepetitions, final boolean shuffleKeys) throws GenerationException, InterruptedException, IOException {
        for (int i = 0; i < nRepetitions; i++) {
            System.out.println((i + 1) + "/" + nRepetitions);
            runExperiment(rand, schema, schemaName, schemaStore, nTests, shuffleKeys, i);
        }
    }

    private void runExperiment(final Random rand, final Schema schema, final String schemaName, final SchemaStore schemaStore, final int nTests, final boolean shuffleKeys, final int currentId)
            throws GenerationException, InterruptedException, IOException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        SimpleProfiler.reset();

        GrowingVPDAlphabet<JSONSymbol> alphabet = extractSymbolsFromSchema(schema);
        System.out.println(alphabet);

        MembershipOracle<JSONSymbol, Boolean> sul = new JSONMembershipOracle(schema);
        CounterOracle<JSONSymbol, Boolean> membershipOracle = new CounterOracle<>(sul, "membership queries");

        EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> eqOracle = new JSONEquivalenceOracle(nTests, schema,
                schemaStore, rand, shuffleKeys);
        CounterEQOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> equivalenceOracle = new CounterEQOracle<>(eqOracle, "equivalence queries");

        TTTLearnerVPDA<JSONSymbol> learner = new TTTLearnerVPDA<>(alphabet, membershipOracle, AcexAnalyzers.LINEAR_FWD);
        Experiment<OneSEVPA<?, JSONSymbol>> experiment = new Experiment<>(learner, equivalenceOracle, alphabet);
        experiment.setLogModels(false);
        experiment.setProfile(true);

        final Future<Void> handler = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                experiment.run();
                return null;
            }
        });

        boolean finished;
        boolean error = false;
        Stopwatch watch = Stopwatch.createStarted();
        try {
            handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            finished = true;
        } catch (TimeoutException e) {
            handler.cancel(true);
            finished = false;
        } catch (ExecutionException e) {
            handler.cancel(true);
            error = true;
            finished = false;
            e.printStackTrace(System.err);
        }
        watch.stop();
        executor.shutdownNow();

        List<Object> results = new LinkedList<>();
        if (finished) {
            OneSEVPA<?, JSONSymbol> learntROCA = experiment.getFinalHypothesis();

            results.add(watch.elapsed().toMillis());
            results.add(getProfilerTime(ROCAExperiment.COUNTEREXAMPLE_PROFILE_KEY));
            results.add(getProfilerTime(LStarROCA.COUNTEREXAMPLE_DFA_PROFILE_KEY));
            results.add(getProfilerTime(ROCAExperiment.LEARNING_ROCA_PROFILE_KEY));
            results.add(getProfilerTime(LStarROCA.CLOSED_TABLE_PROFILE_KEY));
            results.add(getProfilerTime(LStarROCA.FINDING_PERIODIC_DESCRIPTIONS));
            results.add(membershipOracle.getStatisticalData().getCount());
            results.add(equivalenceOracle.getStatisticalData().getCount());
            results.add(experiment.getRounds().getCount());
            results.add(alphabet.size());
            results.add(learntROCA.size());

            Path pathToDOTFolder = Paths.get(System.getProperty("user.dir"), "Results", "JSON", "Dot");
            pathToDOTFolder.toFile().mkdirs();
            Path pathToDotFile = pathToDOTFolder.resolve(schemaName + "-" + String.valueOf(currentId) + ".dot");
            FileWriter writer = new FileWriter(pathToDotFile.toFile());
            GraphDOT.write(learntROCA, writer);
        } else if (error) {
            for (int i = results.size(); i < nColumns; i++) {
                results.add("Error");
            }
        } else {
            for (int i = results.size(); i < nColumns; i++) {
                results.add("Timeout");
            }
        }

        csvPrinter.printRecord(results);
        csvPrinter.flush();
    }

    private long getProfilerTime(String key) {
        Counter counter = SimpleProfiler.cumulated(key);
        if (counter == null) {
            return 0;
        } else {
            return counter.getCount();
        }
    }

    private static GrowingVPDAlphabet<JSONSymbol> extractSymbolsFromSchema(final Schema schema) {
        GrowingVPDAlphabet<JSONSymbol> alphabet = new GrowingVPDAlphabet<>();
        alphabet.addNewSymbol(JSONSymbol.toSymbol("{"), SymbolType.CALL);
        alphabet.addNewSymbol(JSONSymbol.toSymbol(":{"), SymbolType.CALL);
        alphabet.addNewSymbol(JSONSymbol.toSymbol("["), SymbolType.CALL);
        alphabet.addNewSymbol(JSONSymbol.toSymbol(":["), SymbolType.CALL);

        alphabet.addNewSymbol(JSONSymbol.toSymbol("}"), SymbolType.RETURN);
        alphabet.addNewSymbol(JSONSymbol.toSymbol(":}"), SymbolType.RETURN);
        alphabet.addNewSymbol(JSONSymbol.toSymbol("]"), SymbolType.RETURN);
        alphabet.addNewSymbol(JSONSymbol.toSymbol(":]"), SymbolType.RETURN);

        alphabet.addNewSymbol(JSONSymbol.toSymbol(","), SymbolType.INTERNAL);
        alphabet.addNewSymbol(JSONSymbol.toSymbol(":"), SymbolType.INTERNAL);
        alphabet.addNewSymbol(JSONSymbol.toSymbol("\"\\S\""), SymbolType.INTERNAL);
        alphabet.addNewSymbol(JSONSymbol.toSymbol("\"\\I\""), SymbolType.INTERNAL);
        alphabet.addNewSymbol(JSONSymbol.toSymbol("\"\\D\""), SymbolType.INTERNAL);
        alphabet.addNewSymbol(JSONSymbol.toSymbol("true"), SymbolType.INTERNAL);
        alphabet.addNewSymbol(JSONSymbol.toSymbol("false"), SymbolType.INTERNAL);

        extractSymbolsFromSchema(schema, alphabet);

        return alphabet;
    }

    private static void extractSymbolsFromSchema(final Schema schema, GrowingVPDAlphabet<JSONSymbol> alphabet) {
        for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
            String key = entry.getKey();
            alphabet.addNewSymbol(JSONSymbol.toSymbol("\"" + key + "\""), SymbolType.INTERNAL);
            if (entry.getValue().getProperties().size() != 0) {
                extractSymbolsFromSchema(entry.getValue(), alphabet);
            }
        }
    }
}
