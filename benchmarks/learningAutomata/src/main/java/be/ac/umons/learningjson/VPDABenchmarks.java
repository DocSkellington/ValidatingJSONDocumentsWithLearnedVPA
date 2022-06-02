package be.ac.umons.learningjson;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.Traverser;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.learningjson.oracles.JSONMembershipOracle;
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
import net.automatalib.words.Alphabet;
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
            "Diameter",
            "Number SCC"
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
            final ImmutableGraph<Integer> vpdaGraph = vpdaToGraph(learnedVPDA);
            final List<Set<Integer>> SCCs = findSCCs(vpdaGraph);

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
            statistics.add(SCCs.size());

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
        final Graph<L, SevpaViewEdge<L, JSONSymbol>> graph = vpda.graphView();
        int maximalLength = 0;
        for (L start : graph.getNodes()) {
            for (L target : graph.getNodes()) {
                int minimalLength = shortestPathLength(vpda.getInitialLocation(), graph, start, target, 0,
                        new HashSet<>());
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

    private <L, I> ImmutableGraph<Integer> vpdaToGraph(OneSEVPA<L, I> vpda) {
        // @formatter:off
        ImmutableGraph.Builder<Integer> builder = GraphBuilder
            .directed()
            .allowsSelfLoops(true)
            .nodeOrder(ElementOrder.insertion())
            .incidentEdgeOrder(ElementOrder.stable())
            .<Integer>immutable()
        ;
        // @formatter:on

        final Alphabet<I> internalAlphabet = vpda.getInputAlphabet().getInternalAlphabet();
        final Alphabet<I> returnAlphabet = vpda.getInputAlphabet().getReturnAlphabet();
        final Alphabet<I> callAlphabet = vpda.getInputAlphabet().getCallAlphabet();

        final Map<L, Integer> locationToNode = new HashMap<>();
        for (L location : vpda.getLocations()) {
            final int node;
            if (locationToNode.containsKey(location)) {
                node = locationToNode.get(locationToNode);
            } else {
                node = locationToNode.size();
                locationToNode.put(location, node);
            }
            builder.addNode(node);
        }

        for (L sourceLocation : vpda.getLocations()) {
            final int sourceNode = locationToNode.get(sourceLocation);
            for (I intSym : internalAlphabet) {
                final L targetLocation = vpda.getInternalSuccessor(sourceLocation, intSym);
                final int targetNode = locationToNode.get(targetLocation);
                builder.putEdge(sourceNode, targetNode);
            }

            for (int i = 0; i < callAlphabet.size(); i++) {
                builder.putEdge(sourceNode, locationToNode.get(vpda.getInitialLocation()));
            }

            for (I retSym : returnAlphabet) {
                for (I callSym : callAlphabet) {
                    for (L locationBeforeCall : vpda.getLocations()) {
                        final int stackSym = vpda.encodeStackSym(locationBeforeCall, callSym);
                        final L targetLocation = vpda.getReturnSuccessor(sourceLocation, retSym, stackSym);
                        final int targetNode = locationToNode.get(targetLocation);
                        builder.putEdge(sourceNode, targetNode);
                    }
                }
            }
        }

        return builder.build();
    }

    private List<Set<Integer>> findSCCs(ImmutableGraph<Integer> graph) {
        Deque<Integer> stack = new LinkedList<>();
        Set<Integer> seen = new HashSet<>();

        for (Integer sourceNode : graph.nodes()) {
            for (Integer node : Traverser.forGraph(graph).depthFirstPostOrder(sourceNode)) {
                if (!seen.add(node)) {
                    stack.addFirst(node);
                }
            }
        }

        com.google.common.graph.Graph<Integer> transposedGraph = Graphs.transpose(graph);

        Map<Integer, Integer> nodeToComponent = new HashMap<>();
        Set<Integer> componentsRoot = new HashSet<>();
        while (!stack.isEmpty()) {
            int sourceNode = stack.removeFirst();
            boolean newComponent = false;
            for (Integer node : Traverser.forGraph(transposedGraph).depthFirstPreOrder(sourceNode)) {
                if (!nodeToComponent.containsKey(node)) {
                    nodeToComponent.put(node, sourceNode);
                    newComponent = true;
                }
            }
            if (newComponent) {
                componentsRoot.add(sourceNode);
            }
        }
        System.out.println(nodeToComponent);
        System.out.println(componentsRoot);

        Map<Integer, Set<Integer>> SCCs = new HashMap<>(componentsRoot.size());

        for (Map.Entry<Integer, Integer> nodeComponent : nodeToComponent.entrySet()) {
            int componentRoot = nodeComponent.getValue();
            int node = nodeComponent.getKey();

            if (!SCCs.containsKey(componentRoot)) {
                SCCs.put(componentRoot, new HashSet<>());
            }
            SCCs.get(componentRoot).add(node);
        }

        return new ArrayList<>(SCCs.values());
    }

    protected abstract EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> getEquivalenceOracle(
            int numberTests, boolean canGenerateInvalid, int maxDocumentDepth, int maxProperties, int maxItems,
            JSONSchema schema, Random random, boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet);
}
