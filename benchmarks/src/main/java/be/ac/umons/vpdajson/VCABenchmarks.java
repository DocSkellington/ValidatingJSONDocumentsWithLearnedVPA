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
import be.ac.umons.learningjson.oracles.JSONPartialEquivalenceOracle;
import be.ac.umons.learningjson.oracles.VCAJSONEquivalenceOracle;
import de.learnlib.algorithms.lstar.vca.LStarVCA;
import de.learnlib.algorithms.lstar.vca.StratifiedObservationTableWithCounterValues;
import de.learnlib.algorithms.lstar.vca.VCAExperiment;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.cache.roca.ROCAHashCacheOracle;
import de.learnlib.filter.statistic.oracle.ROCACounterOracle;
import de.learnlib.filter.statistic.oracle.vca.VCACounterEQOracle;
import de.learnlib.oracle.equivalence.roca.RestrictedAutomatonCounterEQOracle;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.oca.ROCA;
import net.automatalib.words.VPDAlphabet;

public class VCABenchmarks extends ABenchmarks {

    public VCABenchmarks(final Path pathToCSVFile, final Duration timeout) throws IOException {
        super(pathToCSVFile, timeout);
    }

    @Override
    protected List<String> getHeader() {
        // @formatter:off
        return Arrays.asList(
            "Total time (ms)",
            "Membership queries",
            "Partial equivalence queries",
            "Equivalence queries",
            "Rounds",
            "|R|",
            "|S|",
            "Result alphabet size",
            "Result VCA size"
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

        EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> partialEqOracle = new JSONPartialEquivalenceOracle(
                nTests, MAX_PROPERTIES, MAX_ITEMS, schema, rand, shuffleKeys, alphabet);
        RestrictedAutomatonCounterEQOracle<JSONSymbol> partialEquivalenceOracle = new RestrictedAutomatonCounterEQOracle<>(
                partialEqOracle, "partial equivalence queries");

        EquivalenceOracle.VCAEquivalenceOracle<JSONSymbol> eqOracle = new VCAJSONEquivalenceOracle(nTests,
                MAX_PROPERTIES, MAX_ITEMS, schema, rand, shuffleKeys, alphabet);
        VCACounterEQOracle<JSONSymbol> equivalenceOracle = new VCACounterEQOracle<>(eqOracle, "equivalence queries");

        LStarVCA<JSONSymbol> lstar_vca = new LStarVCA<>(membershipOracle, partialEquivalenceOracle, alphabet);
        VCAExperiment<JSONSymbol> experiment = new VCAExperiment<>(lstar_vca, equivalenceOracle, alphabet);
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
            ROCA<?, JSONSymbol> learntVCA = experiment.getFinalHypothesis();
            StratifiedObservationTableWithCounterValues<JSONSymbol, Boolean> table = lstar_vca.getObservationTable();

            results.add(watch.elapsed().toMillis());
            results.add(membershipOracle.getStatisticalData().getCount());
            results.add(partialEquivalenceOracle.getStatisticalData().getCount());
            results.add(equivalenceOracle.getStatisticalData().getCount());
            results.add(experiment.getRounds().getCount());
            results.add(table.numberOfShortPrefixRows());
            results.add(table.numberOfSuffixes());
            results.add(alphabet.size());
            results.add(learntVCA.size());

            writeModelToDot(learntVCA, schemaName, currentId, "VCA");
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
