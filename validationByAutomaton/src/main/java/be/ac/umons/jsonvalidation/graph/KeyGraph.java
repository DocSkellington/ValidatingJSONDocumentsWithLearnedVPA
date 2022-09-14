package be.ac.umons.jsonvalidation.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.Traverser;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.PairSourceToReached;
import de.learnlib.api.logging.LearnLogger;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;

/**
 * A reachability graph is a directed acyclic graph constructed from
 * {@link ReachabilityRelation}'s comma, internal, and well-matched relations.
 * 
 * Its nodes are the triplets from the composition of the internal relation
 * and the well-matched relation. That is, a node in the graph gives the
 * information that we can go from a state {@code q} in the VPA to a state
 * {@code p} by reading a key {@code k} and its corresponding value.
 * 
 * There is an edge going from {@code (q, k, q')} to {@code (p, k', p')} if and
 * only if the comma relation contains {@code (q', ,, p)}.
 * 
 * Once created, it is guaranteed that the graph is never modified.
 * 
 * The class also maintains a list giving the nodes containing the initial
 * state of the VPA as the starting state, and two maps:
 * <ul>
 * <li>One giving, for all {@code k}, the nodes {@code (s, k, s')}.</li>
 * <li>One giving, for all {@code k}, the locations {@code s} such that
 * {@code (s, k, s')} is a node.</li>
 * </ul>
 * 
 * @author Gaëtan Staquet
 */
public class KeyGraph<L> {
    private static final LearnLogger LOGGER = LearnLogger.getLogger(KeyGraph.class);

    private final OneSEVPA<L, JSONSymbol> automaton;
    private final ImmutableGraph<NodeInGraph<L>> graph;
    private final Map<JSONSymbol, List<NodeInGraph<L>>> keyToNodes = new HashMap<>();
    private final Map<JSONSymbol, Set<L>> keyToLocations = new HashMap<>();
    private final List<NodeInGraph<L>> startingNodes = new LinkedList<>();
    private final boolean hasPathWithDuplicateKeys;
    private final Word<JSONSymbol> witnessInvalid;

    public static <L> KeyGraph<L> graphFor(OneSEVPA<L, JSONSymbol> automaton, boolean checkGraph, boolean computeWitnesses) {
        final ReachabilityRelation<L> reachabilityRelation = ReachabilityRelation.computeReachabilityRelation(automaton, computeWitnesses);
        final WitnessRelation<L> witnessRelation = WitnessRelation.computeWitnessRelation(automaton, reachabilityRelation, computeWitnesses);
        if (reachabilityRelation.size() == 0) {
            return null;
        }

        return new KeyGraph<>(automaton, reachabilityRelation, witnessRelation, checkGraph);
    }

    public KeyGraph(OneSEVPA<L, JSONSymbol> automaton, final ReachabilityRelation<L> reachabilityRelation, final WitnessRelation<L> witnessRelation, boolean checkGraph) {
        this.automaton = automaton;

        this.graph = constructGraph(reachabilityRelation, witnessRelation);
        propagateIsOnPathToAcceptingForLocations(automaton.getLocations());

        if (checkGraph) {
            final List<NodeInGraph<L>> pathWithDuplicateKeys = hasPathWithDuplicateKeys();
            this.hasPathWithDuplicateKeys = !pathWithDuplicateKeys.isEmpty();
            witnessInvalid = constructWitnessDuplicate(pathWithDuplicateKeys, reachabilityRelation, witnessRelation);
        } else {
            hasPathWithDuplicateKeys = false;
            witnessInvalid = null;
        }
        LOGGER.info("Initialization of graph done");
    }

    private Alphabet<JSONSymbol> getKeyAlphabet() {
        final Alphabet<JSONSymbol> internalAlphabet = automaton.getInputAlphabet().getInternalAlphabet();
        final Alphabet<JSONSymbol> primitiveValuesAlphabet = JSONSymbol.primitiveValuesAlphabet;
        // @formatter:off
        return internalAlphabet.stream()
            .filter(symbol -> !primitiveValuesAlphabet.contains(symbol) && symbol != JSONSymbol.commaSymbol)
            .collect(Alphabets.collector());
        // @formatter:on
    }

    private ImmutableGraph<NodeInGraph<L>> constructGraph(ReachabilityRelation<L> reachabilityRelation, WitnessRelation<L> witnessRelation) {
        final L binLocation = witnessRelation.identifyBinLocation(automaton);

        final ReachabilityRelation<L> valueReachabilityRelation = ReachabilityRelation
                .computeValueReachabilityRelation(automaton, reachabilityRelation);

        // @formatter:off
        final ImmutableGraph.Builder<NodeInGraph<L>> builder = GraphBuilder
            .directed()
            .allowsSelfLoops(true)
            .nodeOrder(ElementOrder.insertion())
            .incidentEdgeOrder(ElementOrder.stable())
            .<NodeInGraph<L>>immutable()
        ;
        // @formatter:on
        final Alphabet<JSONSymbol> keyAlphabet = getKeyAlphabet();

        LOGGER.info("Creating nodes");
        // We create the nodes
        final List<NodeInGraph<L>> nodes = new LinkedList<>();
        for (final L startLocation : automaton.getLocations()) {
            if (Objects.equals(startLocation, binLocation)) {
                continue;
            }

            for (final JSONSymbol key : keyAlphabet) {
                final L locationAfterKey = automaton.getInternalSuccessor(startLocation, key);
                if (locationAfterKey == null || Objects.equals(locationAfterKey, binLocation)) {
                    continue;
                }
                for (final InfoInRelation<L> inValueRelation : valueReachabilityRelation.getLocationsAndInfoInRelationWithStart(locationAfterKey)) {
                    if (Objects.equals(inValueRelation.getTarget(), binLocation)) {
                        continue;
                    }

                    final L locationAfterValue = inValueRelation.getTarget();
                    final NodeInGraph<L> node = new NodeInGraph<>(startLocation, locationAfterValue, key, automaton, binLocation);
                    builder.addNode(node);
                    nodes.add(node);

                    if (keyToNodes.containsKey(key)) {
                        keyToNodes.get(key).add(node);
                        keyToLocations.get(key).add(node.getStartLocation());
                    } else {
                        final List<NodeInGraph<L>> listNode = new LinkedList<>();
                        listNode.add(node);
                        keyToNodes.put(key, listNode);
                        final Set<L> setLocations = new LinkedHashSet<>();
                        setLocations.add(node.getStartLocation());
                        keyToLocations.put(key, setLocations);
                    }

                    if (startLocation == automaton.getInitialLocation()) {
                        startingNodes.add(node);
                    }
                }
            }
        }
        LOGGER.info("Nodes created");

        // We create the edges
        for (final NodeInGraph<L> startNode : nodes) {
            final L locationBeforeComma = startNode.getTargetLocation();
            for (final NodeInGraph<L> targetNode : nodes) {
                final L locationAfterComma = targetNode.getStartLocation();
                if (automaton.getInternalSuccessor(locationBeforeComma, JSONSymbol.commaSymbol) == locationAfterComma) {
                    builder.putEdge(startNode, targetNode);
                }
            }
        }

        LOGGER.info("Graph created");
        return builder.build();
    }

    private List<NodeInGraph<L>> hasPathWithDuplicateKeys() {
        for (NodeInGraph<L> start : startingNodes) {
            List<NodeInGraph<L>> seenNodes = new LinkedList<>();
            if (hasPathWithDuplicateKeys(start, seenNodes, new LinkedHashSet<>())) {
                return seenNodes;
            }
        }
        return Collections.emptyList();
    }

    private boolean hasPathWithDuplicateKeys(NodeInGraph<L> currentNode, List<NodeInGraph<L>> seenNodes,
            Set<JSONSymbol> keys) {
        // We have a loop
        if (seenNodes.contains(currentNode)) {
            seenNodes.add(currentNode);
            return true;
        }

        seenNodes.add(currentNode);
        // We have a duplicate key
        if (!keys.add(currentNode.getSymbol())) {
            return true;
        }

        for (NodeInGraph<L> successor : graph.successors(currentNode)) {
            if (hasPathWithDuplicateKeys(successor, seenNodes, keys)) {
                return true;
            }
        }
        keys.remove(currentNode.getSymbol());
        seenNodes.remove(seenNodes.size() - 1);
        return false;
    }

    private Word<JSONSymbol> constructWitnessDuplicate(final List<NodeInGraph<L>> path,
            final ReachabilityRelation<L> reachabilityRelation, final WitnessRelation<L> witnessRelation) {
        if (isValid()) {
            return null;
        }

        final L start = path.get(0).getStartLocation();
        final L target = path.get(path.size() - 1).getTargetLocation();
        assert witnessRelation.getWitnessToStart(start, target) != null : start + " " + target;
        assert witnessRelation.getWitnessFromTarget(start, target) != null;

        final WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.append(witnessRelation.getWitnessToStart(start, target));
        int i = 0;
        for (final NodeInGraph<L> node : path) {
            builder.append(node.getSymbol());
            final L intermediate = automaton.getInternalSuccessor(node.getStartLocation(), node.getSymbol());
            builder.append(reachabilityRelation.getWitness(intermediate, node.getTargetLocation()));
            if (++i < path.size()) {
                builder.append(JSONSymbol.commaSymbol);
            }
        }
        builder.append(witnessRelation.getWitnessFromTarget(start, target));

        return builder.toWord();
    }

    public boolean isValid() {
        return !hasPathWithDuplicateKeys;
    }

    public Word<JSONSymbol> getWitnessCycle() {
        return witnessInvalid;
    }

    private void propagateIsOnPathToAcceptingForLocations(Collection<L> locations) {
        Iterable<NodeInGraph<L>> exploration = Traverser.forGraph(graph).depthFirstPostOrder(startingNodes);

        for (NodeInGraph<L> node : exploration) {
            Set<NodeInGraph<L>> successors = graph.successors(node);
            for (NodeInGraph<L> successor : successors) {
                for (L location : locations) {
                    int locationId = automaton.getLocationId(location);
                    if (successor.isOnPathToAcceptingForLocation(locationId)) {
                        node.setOnPathToAcceptingLocation(locationId);
                    }
                }
            }
        }
    }

    ImmutableGraph<NodeInGraph<L>> getGraph() {
        return graph;
    }

    Set<NodeInGraph<L>> nodes() {
        return this.graph.nodes();
    }

    Set<EndpointPair<NodeInGraph<L>>> edges() {
        return this.graph.edges();
    }

    public int size() {
        return nodes().size();
    }

    @Nullable
    NodeInGraph<L> getNode(L sourceLocation, JSONSymbol key, L targetLocation) {
        return getNode(PairSourceToReached.of(sourceLocation, targetLocation), key);
    }

    @Nullable
    NodeInGraph<L> getNode(PairSourceToReached<L> pairSourceToReached, JSONSymbol key) {
        for (NodeInGraph<L> node : nodes()) {
            if (Objects.equals(node.getSymbol(), key) && Objects.equals(node.getPairLocations(), pairSourceToReached)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Tests whether the node in the graph can read a return symbol that pops a
     * stack symbol that was pushed by reading { from {@code locationBeforeCall}.
     * 
     * This assumes the return symbol is } (meaning the push symbol has to be {).
     * 
     * @param node               The node in the graph
     * @param locationBeforeCall The location before the call
     * @return
     */
    boolean isAcceptingForLocation(NodeInGraph<L> node, L locationBeforeCall) {
        return node.isAcceptingForLocation(automaton.getLocationId(locationBeforeCall));
    }

    /**
     * Tests whether the node in the graph corresponding to the given pair of states
     * and the key can read a return symbol that pops a stack symbol that was pushed
     * by reading { from {@code locationBeforeCall}.
     * 
     * This assumes the return symbol is } (meaning the push symbol has to be {).
     * 
     * @param sourceLocation     The source location
     * @param key                The key
     * @param targetLocation     The reached location
     * @param locationBeforeCall The location before the call
     * @return
     */
    public boolean isAcceptingForLocation(L sourceLocation, JSONSymbol key, L targetLocation, L locationBeforeCall) {
        return isAcceptingForLocation(getNode(sourceLocation, key, targetLocation), locationBeforeCall);
    }

    /**
     * Tests whether the node in the graph corresponding to the given pair of states
     * and the key can read a return symbol that pops a stack symbol that was pushed
     * by reading { from {@code locationBeforeCall}.
     * 
     * This assumes the return symbol is } (meaning the push symbol has to be {).
     * 
     * @param pairSourceToReached The pair of source-to-reached locations
     * @param key                 The key
     * @param locationBeforeCall  The location before the call
     * @return
     */
    public boolean isAcceptingForLocation(PairSourceToReached<L> pairSourceToReached, JSONSymbol key,
            L locationBeforeCall) {
        return isAcceptingForLocation(getNode(pairSourceToReached, key), locationBeforeCall);
    }

    /**
     * Gets all the nodes that can read the given key.
     * 
     * @param key The key
     * @return A list with the nodes
     */
    public List<NodeInGraph<L>> getNodesForKey(JSONSymbol key) {
        return keyToNodes.getOrDefault(key, Collections.emptyList());
    }

    /**
     * Gets all the locations for which there exists an internal transition reading
     * the given key.
     * 
     * @param key The key
     * @return
     */
    public Set<L> getLocationsReadingKey(JSONSymbol key) {
        return keyToLocations.getOrDefault(key, Collections.emptySet());
    }

    /**
     * Gets all the locations in the VPA such that it is possible to read a closing
     * curly brace and there is a path in the graph such that none of its node is
     * marked as rejected and all the keys seen while processing the input are
     * exactly seen on the path.
     * 
     * This allows to know the set of locations that are actually reached in the VPA
     * after reading the current object and from which we can read a closing curly
     * brace.
     * 
     * @param seenKeys            The set of keys seen while reading the input
     * @param locationsBeforeCall The locations of the VPA before reading the
     *                            opening curly brace that opened the current object
     * @param rejectedNodes       A collection of nodes in the graph that are marked
     *                            as rejected
     * @return The set of locations from which the VPA can read the closing curly
     *         brace
     */
    public Set<L> getLocationsWithReturnTransitionOnUnmarkedPathsWithAllKeysSeen(Set<JSONSymbol> seenKeys,
            Collection<L> locationsBeforeCall, Collection<NodeInGraph<L>> rejectedNodes) {
        final Set<L> locationsReadingClosing = new LinkedHashSet<>();
        for (NodeInGraph<L> initial : startingNodes) {
            depthFirstExploreForAcceptingNodes(initial, new LinkedList<>(), locationsReadingClosing, seenKeys,
                    locationsBeforeCall, rejectedNodes);
        }
        return locationsReadingClosing;
    }

    private void depthFirstExploreForAcceptingNodes(final NodeInGraph<L> current,
            final LinkedList<JSONSymbol> seenKeysInExploration, final Set<L> locationsReadingClosing,
            final Set<JSONSymbol> seenKeysInAutomaton, final Collection<L> locationsBeforeCall,
            final Collection<NodeInGraph<L>> rejectedNodes) {
        // The path has a node that is rejected
        if (rejectedNodes.contains(current)) {
            return;
        }

        // We know we will never be able to reach a state from which we can read a
        // return symbol matching the locations before the call
        // @formatter:off
        boolean canReachAnAcceptingLocation = locationsBeforeCall.stream()
            .filter(location -> current.isOnPathToAcceptingForLocation(automaton.getLocationId(location)))
            .findAny().isPresent();
        // @formatter:on
        if (!canReachAnAcceptingLocation) {
            return;
        }

        // The path contains a node reading a key that was not seen in the automaton
        final JSONSymbol key = current.getSymbol();
        if (!seenKeysInAutomaton.contains(key)) {
            return;
        }
        seenKeysInExploration.addFirst(key);

        boolean acceptingForOneLocation = false;
        for (final L locationBeforeCall : locationsBeforeCall) {
            if (current.isAcceptingForLocation(automaton.getLocationId(locationBeforeCall))) {
                acceptingForOneLocation = true;
                break;
            }
        }

        if (acceptingForOneLocation) {
            // All the keys seen from the input must be on the path and vice-versa
            if (seenKeysInAutomaton.size() == seenKeysInExploration.size()) {
                boolean allKeys = true;
                for (final JSONSymbol seenKey : seenKeysInExploration) {
                    if (!seenKeysInAutomaton.contains(seenKey)) {
                        allKeys = false;
                        break;
                    }
                }

                if (allKeys) {
                    locationsReadingClosing.add(current.getTargetLocation());
                }
            }
        }

        final Set<NodeInGraph<L>> successors = graph.successors(current);
        for (final NodeInGraph<L> successor : successors) {
            depthFirstExploreForAcceptingNodes(successor, seenKeysInExploration, locationsReadingClosing,
                    seenKeysInAutomaton, locationsBeforeCall, rejectedNodes);
        }

        seenKeysInExploration.removeFirst();
    }
}
