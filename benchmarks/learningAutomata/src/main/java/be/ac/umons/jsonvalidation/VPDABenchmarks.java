package be.ac.umons.jsonvalidation;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
        final List<L> nodes = List.copyOf(graph.getNodes());
        final List<List<Integer>> distances = floydWarshall(graph, nodes);

        return distances.stream()
            .map(list -> list.stream()
                .max(Comparator.naturalOrder())
                .orElseThrow()
            )
            .max(Comparator.naturalOrder())
            .orElseThrow()
        ;
    }

    private <L> List<List<Integer>> floydWarshall(Graph<L, SevpaViewEdge<L, JSONSymbol>> graph, List<L> nodes) {
        final List<List<Integer>> distances = new ArrayList<>(graph.size());

        for (L source : nodes) {
            final List<Integer> dist = new ArrayList<>(nodes.size());
            final Collection<L> adjacent = graph.getAdjacentTargets(source);
            for (L target : nodes) {
                if (source == target) {
                    dist.add(0);
                }
                else if (adjacent.contains(target)) {
                    dist.add(1);
                }
                else {
                    dist.add(Integer.MAX_VALUE);
                }
            }
            distances.add(dist);
        }

        for (int k = 0 ; k < nodes.size() ; k++) {
            for (int i = 0 ; i < nodes.size() ; i++) {
                for (int j = 0 ; j < nodes.size() ; j++) {
                    final int sumByK;
                    if (distances.get(i).get(k) == Integer.MAX_VALUE || distances.get(k).get(j) == Integer.MAX_VALUE) {
                        sumByK = Integer.MAX_VALUE;
                    }
                    else {
                        sumByK = distances.get(i).get(k) + distances.get(k).get(j);
                    }
                    if (distances.get(i).get(j) > sumByK) {
                        distances.get(i).set(j, sumByK);
                    }
                }
            }
        }

        return distances;
    }

    protected abstract EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> getEquivalenceOracle(
            int numberTests, boolean canGenerateInvalid, int maxDocumentDepth, int maxProperties, int maxItems,
            JSONSchema schema, Random random, boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet);
}
