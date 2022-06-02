package be.ac.umons.jsonvalidation;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonvalidation.oracles.JSONMembershipOracle;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.vpda.TTTLearnerVPDA;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.statistic.oracle.CounterEQOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.util.Experiment;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.automata.vpda.OneSEVPAGraphView.SevpaViewEdge;
import net.automatalib.graphs.Graph;
import net.automatalib.words.VPDAlphabet;

public abstract class VPDABenchmarks extends ABenchmarks {

    public VPDABenchmarks(Path pathToCSVFile, Duration timeout, int maxProperties, int maxItems) throws IOException {
        super(pathToCSVFile, timeout, maxProperties, maxItems);
    }

    @Override
    protected List<String> getHeader() {
        // @formatter:off
        return List.of(
            "Total time (ms)",
            "Membership queries",
            "Equivalence queries",
            "Rounds",
            "alphabet size",
            "ROCA size",
            "Internal transitions",
            "Return transitions",
            "Call transitions",
            "Diameter"
        );
        // @formatter:on
    }

    @Override
    protected void runExperiment(final Random rand, final JSONSchema schema, final String schemaName, final int nTests,
            final boolean canGenerateInvalid, final int maxDocumentDepth, final boolean shuffleKeys,
            final int currentId) throws InterruptedException, IOException, JSONSchemaException {
        final VPDAlphabet<JSONSymbol> alphabet = extractSymbolsFromSchema(schema);

        final MembershipOracle<JSONSymbol, Boolean> sul = new JSONMembershipOracle(schema);
        final CounterOracle<JSONSymbol, Boolean> membershipOracle = new CounterOracle<>(sul, "membership queries");

        final EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> eqOracle = getEquivalenceOracle(nTests,
                canGenerateInvalid, maxDocumentDepth, getMaxProperties(), getMaxItems(), schema, rand, shuffleKeys,
                alphabet);
        final CounterEQOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> equivalenceOracle = new CounterEQOracle<>(
                eqOracle, "equivalence queries");

        final TTTLearnerVPDA<JSONSymbol> learner = new TTTLearnerVPDA<>(alphabet, membershipOracle,
                AcexAnalyzers.LINEAR_FWD);
        final Experiment<OneSEVPA<?, JSONSymbol>> experiment = new Experiment<>(learner, equivalenceOracle, alphabet);
        experiment.setLogModels(false);
        experiment.setProfile(true);

        final ExperimentResults results = runExperiment(experiment);

        final List<Object> statistics = new LinkedList<>();
        if (results.finished) {
            final OneSEVPA<?, JSONSymbol> learnedVPDA = experiment.getFinalHypothesis();

            statistics.add(results.timeInMillis);
            statistics.add(membershipOracle.getStatisticalData().getCount());
            statistics.add(equivalenceOracle.getStatisticalData().getCount());
            statistics.add(experiment.getRounds().getCount());
            statistics.add(alphabet.size());
            statistics.add(learnedVPDA.size());
            statistics.add(learnedVPDA.numberOfInternalTransitions());
            statistics.add(learnedVPDA.numberOfReturnTransitions());
            statistics.add(learnedVPDA.numberOfCallTransitions());
            statistics.add(computeDiameter(learnedVPDA));

            writeModelToDot(learnedVPDA, schemaName, currentId, "VPDA");
        } else if (results.error) {
            for (int i = statistics.size(); i < nColumns; i++) {
                statistics.add("Error");
            }
        } else {
            for (int i = statistics.size(); i < nColumns; i++) {
                statistics.add("Timeout");
            }
        }

        csvPrinter.printRecord(statistics);
        csvPrinter.flush();
    }

    private <L> int computeDiameter(OneSEVPA<L, JSONSymbol> vpda) {
        System.out.println("Computing diameter");
        final Graph<L, SevpaViewEdge<L, JSONSymbol>> graph = vpda.graphView();
        int maximalLength = 0;
        for (L start : graph.getNodes()) {
            for (L target : graph.getNodes()) {
                System.out.println("From " + start + " to " + target);
                int minimalLength = shortestPathLength(vpda.getInitialLocation(), graph, start, target, 0,
                        new HashSet<>());
                System.out.println(minimalLength);
                maximalLength = Math.max(maximalLength, minimalLength);
            }
        }
        return maximalLength;
    }

    private <L> int shortestPathLength(L initial, Graph<L, SevpaViewEdge<L, JSONSymbol>> graph, L current, L target,
            int length, Set<L> seenLocations) {
        if (Objects.equals(current, target)) {
            return length;
        }

        if (!seenLocations.add(current)) {
            return Integer.MAX_VALUE;
        }

        int minimal = Integer.MAX_VALUE;
        for (L successor : graph.getAdjacentTargets(current)) {
            minimal = Math.min(shortestPathLength(initial, graph, successor, target, length + 1, seenLocations),
                    minimal);
        }
        minimal = Math.min(shortestPathLength(initial, graph, initial, target, length, seenLocations), minimal);

        seenLocations.remove(current);
        return minimal;
    }

    protected abstract EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> getEquivalenceOracle(
            int numberTests, boolean canGenerateInvalid, int maxDocumentDepth, int maxProperties, int maxItems,
            JSONSchema schema, Random random, boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet);
}
