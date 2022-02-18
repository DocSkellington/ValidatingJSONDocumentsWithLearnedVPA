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

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Stopwatch;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.learningjson.oracles.JSONMembershipOracle;
import be.ac.umons.learningjson.oracles.VPDAJSONEquivalenceOracle;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.vpda.TTTLearnerVPDA;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.statistic.oracle.CounterEQOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.util.Experiment;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.VPDAlphabet;

/**
 * Benchmarks based on JSON documents and Schemas.
 * 
 * @author Gaëtan Staquet
 */
public class VPDABenchmarks extends ABenchmarks {

    public VPDABenchmarks(final Path pathToCSVFile, final Duration timeout) throws IOException {
        super(pathToCSVFile, timeout);
    }

    @Override
    protected List<String> getHeader() {
        // @formatter:off
        return Arrays.asList(
            "Total time (ms)",
            "Membership queries",
            "Equivalence queries",
            "Rounds",
            "Result alphabet size",
            "Result ROCA size"
        );
        // @formatter:on
    }

    @Override
    protected void runExperiment(final Random rand, final JSONSchema schema, final String schemaName, final int nTests,
            final boolean shuffleKeys, final int currentId)
            throws InterruptedException, IOException, JSONSchemaException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        SimpleProfiler.reset();

        VPDAlphabet<JSONSymbol> alphabet = extractSymbolsFromSchema(schema);

        MembershipOracle<JSONSymbol, Boolean> sul = new JSONMembershipOracle(schema);
        CounterOracle<JSONSymbol, Boolean> membershipOracle = new CounterOracle<>(sul, "membership queries");

        EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> eqOracle = new VPDAJSONEquivalenceOracle(nTests,
                MAX_PROPERTIES, MAX_ITEMS, schema, rand, shuffleKeys, alphabet);
        CounterEQOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> equivalenceOracle = new CounterEQOracle<>(
                eqOracle, "equivalence queries");

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
            OneSEVPA<?, JSONSymbol> learntVPDA = experiment.getFinalHypothesis();

            results.add(watch.elapsed().toMillis());
            results.add(membershipOracle.getStatisticalData().getCount());
            results.add(equivalenceOracle.getStatisticalData().getCount());
            results.add(experiment.getRounds().getCount());
            results.add(alphabet.size());
            results.add(learntVPDA.size());

            writeModelToDot(learntVPDA, schemaName, currentId, "VPDA");
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
}
