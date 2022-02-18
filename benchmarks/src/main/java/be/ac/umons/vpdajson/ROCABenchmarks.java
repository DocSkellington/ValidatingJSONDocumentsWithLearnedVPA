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
import be.ac.umons.learningjson.oracles.JSONCounterValueOracle;
import be.ac.umons.learningjson.oracles.JSONMembershipOracle;
import be.ac.umons.learningjson.oracles.JSONPartialEquivalenceOracle;
import be.ac.umons.learningjson.oracles.ROCAJSONEquivalenceOracle;
import de.learnlib.algorithms.lstar.roca.LStarROCA;
import de.learnlib.algorithms.lstar.roca.ObservationTableWithCounterValuesROCA;
import de.learnlib.algorithms.lstar.roca.ROCAExperiment;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.cache.roca.CounterValueHashCacheOracle;
import de.learnlib.filter.cache.roca.ROCAHashCacheOracle;
import de.learnlib.filter.statistic.oracle.CounterValueCounterOracle;
import de.learnlib.filter.statistic.oracle.ROCACounterOracle;
import de.learnlib.filter.statistic.oracle.roca.ROCACounterEQOracle;
import de.learnlib.oracle.equivalence.roca.RestrictedAutomatonCounterEQOracle;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.oca.ROCA;
import net.automatalib.words.VPDAlphabet;

public class ROCABenchmarks extends ABenchmarks {

    public ROCABenchmarks(final Path pathToCSVFile, final Duration timeout) throws IOException {
        super(pathToCSVFile, timeout);
    }

    @Override
    protected List<String> getHeader() {
        // @formatter:off
        return Arrays.asList(
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
            "|R|",
            "|S|",
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

        MembershipOracle.ROCAMembershipOracle<JSONSymbol> sul = new JSONMembershipOracle(schema);
        ROCAHashCacheOracle<JSONSymbol> sulCache = new ROCAHashCacheOracle<>(sul);
        ROCACounterOracle<JSONSymbol> membershipOracle = new ROCACounterOracle<>(sulCache, "membership queries");

        MembershipOracle.CounterValueOracle<JSONSymbol> counterValue = new JSONCounterValueOracle();
        CounterValueHashCacheOracle<JSONSymbol> counterValueCache = new CounterValueHashCacheOracle<>(counterValue);
        CounterValueCounterOracle<JSONSymbol> counterValueOracle = new CounterValueCounterOracle<>(counterValueCache,
                "counter value queries");

        EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> partialEqOracle = new JSONPartialEquivalenceOracle(
                nTests, MAX_PROPERTIES, MAX_ITEMS, schema, rand, shuffleKeys, alphabet);
        RestrictedAutomatonCounterEQOracle<JSONSymbol> partialEquivalenceOracle = new RestrictedAutomatonCounterEQOracle<>(
                partialEqOracle, "partial equivalence queries");

        EquivalenceOracle.ROCAEquivalenceOracle<JSONSymbol> eqOracle = new ROCAJSONEquivalenceOracle(nTests,
                MAX_PROPERTIES, MAX_ITEMS, schema, rand, shuffleKeys, alphabet);
        ROCACounterEQOracle<JSONSymbol> equivalenceOracle = new ROCACounterEQOracle<>(eqOracle, "equivalence queries");

        LStarROCA<JSONSymbol> lstar_roca = new LStarROCA<>(membershipOracle, counterValueOracle,
                partialEquivalenceOracle, alphabet);
        ROCAExperiment<JSONSymbol> experiment = new ROCAExperiment<>(lstar_roca, equivalenceOracle, alphabet);
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
            e.printStackTrace(System.err);
            handler.cancel(true);
            error = true;
            finished = false;
        }
        watch.stop();
        executor.shutdownNow();

        List<Object> results = new LinkedList<>();
        if (finished) {
            ROCA<?, JSONSymbol> learntROCA = experiment.getFinalHypothesis();
            ObservationTableWithCounterValuesROCA<JSONSymbol> table = lstar_roca.getObservationTable();

            results.add(watch.elapsed().toMillis());
            results.add(getProfilerTime(ROCAExperiment.COUNTEREXAMPLE_PROFILE_KEY));
            results.add(getProfilerTime(LStarROCA.COUNTEREXAMPLE_DFA_PROFILE_KEY));
            results.add(getProfilerTime(ROCAExperiment.LEARNING_ROCA_PROFILE_KEY));
            results.add(getProfilerTime(LStarROCA.CLOSED_TABLE_PROFILE_KEY));
            results.add(getProfilerTime(LStarROCA.FINDING_PERIODIC_DESCRIPTIONS));
            results.add(membershipOracle.getStatisticalData().getCount());
            results.add(counterValueOracle.getStatisticalData().getCount());
            results.add(partialEquivalenceOracle.getStatisticalData().getCount());
            results.add(equivalenceOracle.getStatisticalData().getCount());
            results.add(experiment.getRounds().getCount());
            results.add(table.numberOfShortPrefixRows());
            results.add(table.numberOfSuffixes());
            results.add(alphabet.size());
            results.add(learntROCA.size());

            writeModelToDot(learntROCA, schemaName, currentId, "ROCA");
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
