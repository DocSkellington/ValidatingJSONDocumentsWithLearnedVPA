package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.Traverser;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.validation.PairSourceToReached;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
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
public class ReachabilityGraph<L> {
    private final OneSEVPA<L, JSONSymbol> automaton;
    private final ImmutableGraph<NodeInGraph<L>> graph;
    private final Map<JSONSymbol, List<NodeInGraph<L>>> keyToNodes = new HashMap<>();
    private final Map<JSONSymbol, Set<L>> keyToLocations = new HashMap<>();
    private final List<NodeInGraph<L>> startingNodes = new LinkedList<>();
    private final boolean cyclic;
    private final boolean duplicateKeys;

    public static <L> ReachabilityGraph<L> graphFor(OneSEVPA<L, JSONSymbol> automaton) {
        final ReachabilityRelation<L> commaRelation = ReachabilityRelation.computeCommaRelation(automaton);
        final ReachabilityRelation<L> internalRelation = ReachabilityRelation.computeInternalRelation(automaton);
        final ReachabilityRelation<L> wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation);

        return new ReachabilityGraph<>(automaton, commaRelation, internalRelation, wellMatchedRelation);
    }

    public ReachabilityGraph(OneSEVPA<L, JSONSymbol> automaton, final ReachabilityRelation<L> commaRelation, final ReachabilityRelation<L> internalRelation, final ReachabilityRelation<L> wellMatchedRelation) {
        this.automaton = automaton;
        final Set<L> binLocations = wellMatchedRelation.identifyBinLocations(automaton);

        final ReachabilityRelation<L> unionRelation = internalRelation.union(wellMatchedRelation);

        final ReachabilityRelation<L> keyValueRelation = unionRelation.compose(unionRelation);

        // @formatter:off
        final ImmutableGraph.Builder<NodeInGraph<L>> builder = GraphBuilder
            .directed()
            .allowsSelfLoops(true)
            .nodeOrder(ElementOrder.insertion())
            .incidentEdgeOrder(ElementOrder.stable())
            .<NodeInGraph<L>>immutable()
        ;
        List<JSONSymbol> notKeySymbols = List.of(
            JSONSymbol.commaSymbol,
            JSONSymbol.integerSymbol,
            JSONSymbol.numberSymbol,
            JSONSymbol.stringSymbol,
            JSONSymbol.enumSymbol,
            JSONSymbol.trueSymbol,
            JSONSymbol.falseSymbol,
            JSONSymbol.nullSymbol
        );
        final Alphabet<JSONSymbol> keyAlphabet = automaton.getInputAlphabet().getInternalAlphabet().stream()
            .filter(symbol -> !notKeySymbols.contains(symbol))
            .collect(Alphabets.collector());
        // @formatter:on

        // We create the nodes
        final Map<Pair<InRelation<L>, JSONSymbol>, NodeInGraph<L>> relationToNode = new HashMap<>();
        for (final InRelation<L> inRel : keyValueRelation) {
            if (binLocations.contains(inRel.getStart()) || binLocations.contains(inRel.getTarget())) {
                continue;
            }

            // @formatter:off
            final boolean binStateOnPath = inRel.getLocationsSeenBetweenStartAndTarget().stream()
                .filter(location -> binLocations.contains(location))
                .findAny().isPresent();
            // @formatter:on
            if (binStateOnPath) {
                continue;
            }

            for (JSONSymbol key : keyAlphabet) {
                L target = automaton.getInternalSuccessor(inRel.getStart(), key);
                if (internalRelation.areInRelation(target, inRel.getTarget()) || wellMatchedRelation.areInRelation(target, inRel.getTarget())) {
                    final NodeInGraph<L> node = new NodeInGraph<>(inRel, key, automaton, binLocations);
                    relationToNode.put(Pair.of(inRel, key), node);
                    builder.addNode(node);

                    if (keyToNodes.containsKey(key)) {
                        keyToNodes.get(key).add(node);
                        keyToLocations.get(key).add(node.getStartLocation());
                    } else {
                        final List<NodeInGraph<L>> listNode = new LinkedList<>();
                        listNode.add(node);
                        keyToNodes.put(key, listNode);
                        final Set<L> setLocations = new HashSet<>();
                        setLocations.add(node.getStartLocation());
                        keyToLocations.put(key, setLocations);
                    }

                    if (inRel.getStart().equals(automaton.getInitialLocation())) {
                        startingNodes.add(node);
                    }
                }
            }
        }
        // We create the edges
        for (final InRelation<L> source : keyValueRelation) {
            if (binLocations.contains(source.getStart()) || binLocations.contains(source.getTarget())) {
                continue;
            }
            for (final InRelation<L> target : keyValueRelation) {
                if (binLocations.contains(target.getStart()) || binLocations.contains(target.getTarget())) {
                    continue;
                }
                if (commaRelation.areInRelation(source.getTarget(), target.getStart())) {
                    for (JSONSymbol keyInSource : keyAlphabet) {
                        NodeInGraph<L> sourceNode = relationToNode.get(Pair.of(source, keyInSource));
                        if (sourceNode == null) {
                            continue;
                        }
                        for (JSONSymbol keyInTarget : keyAlphabet) {
                            NodeInGraph<L> targetNode = relationToNode.get(Pair.of(target, keyInTarget));
                            if (targetNode != null) {
                                builder.putEdge(sourceNode, targetNode);
                            }
                        }
                    }
                }
            }
        }

        this.graph = builder.build();

        boolean cyclic = false, duplicateKeys = false;
        for (NodeInGraph<L> start : startingNodes) {
            Set<NodeInGraph<L>> seenNodes = new HashSet<>();
            Set<JSONSymbol> symbols = new HashSet<>();
            for (NodeInGraph<L> node : Traverser.forGraph(graph).depthFirstPreOrder(start)) {
                if (!seenNodes.add(node)) {
                    cyclic = true;
                }
                if (!symbols.add(node.getSymbol())) {
                    duplicateKeys = true;
                }
            }
        }

        this.cyclic = cyclic;
        this.duplicateKeys = duplicateKeys;

        propagateIsOnPathToAcceptingForLocations(automaton.getLocations());
    }

    public boolean isValid() {
        return !cyclic && !duplicateKeys;
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

    @Nullable
    NodeInGraph<L> getNode(InRelation<L> inRelation) {
        for (NodeInGraph<L> node : nodes()) {
            if (node.getInRelation().equals(inRelation)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Test whether the given triplet can read a return symbol popping the symbol
     * corresponding to the locationBeforeCall.
     * 
     * This assumes that the return symbol is }.
     * 
     * @param inRelation         The triplet
     * @param locationBeforeCall The location from which the call symbol to pop was
     *                           pushed
     * @return
     */
    public boolean isAcceptingForLocation(InRelation<L> inRelation, L locationBeforeCall) {
        return getNode(inRelation).isAcceptingForLocation(automaton.getLocationId(locationBeforeCall));
    }

    private List<NodeInGraph<L>> getNodesForKey(JSONSymbol key) {
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
     * Adds a new layer in the stack of each node.
     */
    public void addLayerInStack() {
        for (NodeInGraph<L> node : nodes()) {
            node.addLayerInStack();
        }
    }

    /**
     * Removes the top layer from the stack of each node.
     */
    public void popLayerInStack() {
        for (NodeInGraph<L> node : nodes()) {
            node.popLayerInStack();
        }
    }

    /**
     * Given the list of locations actually reached, marks all the nodes in the
     * graph that were not reached.
     * 
     * That is, it allows to remove a path in the graph that was not followed while
     * reading the input.
     * 
     * The markings are done at the top level of the stack.
     * 
     * The function expects that the size of {@code locationsReached} is equal to
     * the number of nodes having {@code lastKeyProcessed} as their symbol.
     * 
     * @param sourceToReachedLocations The locations reached
     * @param lastKeyProcessed         The last key that was read
     */
    public void markNodesToReject(final Set<PairSourceToReached<L>> sourceToReachedLocations,
            final JSONSymbol lastKeyProcessed) {
        final List<NodeInGraph<L>> nodesForKey = getNodesForKey(lastKeyProcessed);

        for (NodeInGraph<L> node : nodesForKey) {
            if (!sourceToReachedLocations.contains(node.toPairLocations())) {
                node.markRejected();
            }
        }
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
     * @return The set of locations from which the VPA can read the closing curly
     *         brace
     */
    public Set<L> getLocationsWithReturnTransitionOnUnmarkedPathsWithAllKeysSeen(Set<JSONSymbol> seenKeys,
            Collection<L> locationsBeforeCall) {
        final Set<L> locationsReadingClosing = new HashSet<>();
        for (NodeInGraph<L> initial : startingNodes) {
            depthFirstExploreForAcceptingNodes(initial, new LinkedList<>(), locationsReadingClosing, seenKeys,
                    locationsBeforeCall);
        }
        return locationsReadingClosing;
    }

    private void depthFirstExploreForAcceptingNodes(final NodeInGraph<L> current,
            final LinkedList<JSONSymbol> seenKeysInExploration, final Set<L> locationsReadingClosing,
            final Set<JSONSymbol> seenKeysInAutomaton, final Collection<L> locationsBeforeCall) {
        // The path has a node that is rejected
        if (current.isRejected()) {
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
                    seenKeysInAutomaton, locationsBeforeCall);
        }

        seenKeysInExploration.removeFirst();
    }
}