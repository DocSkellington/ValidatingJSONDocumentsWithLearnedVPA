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
 * A key graph is a directed graph constructed from {@link ReachabilityRelation}
 * for a 1-SEVPA recognizing JSON documents.
 * 
 * <p>
 * The nodes of the graph are triplets {@code (p, k, q)} such that, from the
 * location {@code p}, it is possible to reach the location {@code q} by reading
 * a word {@code k v} with v a well-matched word.
 * That is, the triplets store the fact that there is a key-value pair that can
 * go from p to q.
 * </p>
 * 
 * <p>
 * There is an edge going from {@code (q, k, q')} to {@code (p, k', p')} if and
 * only if there is an internal transition reading the comma symbol from the
 * location {@code q'} to the location {@code p'}.
 * </p>
 * 
 * <p>
 * Once created, it is guaranteed that the graph is never modified.
 * </p>
 * 
 * <p>
 * If the 1-SEVPA is correctly built, the key graph is acyclic and, on every
 * path in the graph, each key is seen at most once.
 * However, it may happen that the graph contains a cycle or a path on which the
 * same key is seen twice (typically, if the learning process is stopped before
 * obtaining a valid hypothesis).
 * This implementation correctly handles this case, i.e., it is guaranteed that
 * all the algorithms eventually stop even if the graph is not "correct".
 * </p>
 * 
 * <p>
 * The class also maintains a list giving the nodes containing the initial
 * location of the VPA to mark the paths' start points, and two maps:
 * <ul>
 * <li>One giving, for all {@code k}, the nodes {@code (s, k, s')}.</li>
 * <li>One giving, for all {@code k}, the locations {@code s} such that
 * {@code (s, k, s')} is a node.</li>
 * </ul>
 * </p>
 * 
 * @param <L> Location type
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

    /**
     * Constructs the key graph for the provided automaton.
     * 
     * <p>
     * This function computes the reachability relation of the VPA.
     * </p>
     * 
     * <p>
     * Since the key graph may contain paths where one key is seen twice, it is
     * possible to check that the graph is actually valid.
     * If that check must be performed, witnesses of the reachability relation are
     * computed.
     * </p>
     * 
     * @param <L>        Location type
     * @param automaton  The 1-SEVPA
     * @param checkGraph Whether to check that the graph is valid, i.e., there is no
     *                   path on which we can see the same key twice.
     * @return The key graph
     */
    public static <L> KeyGraph<L> graphFor(final OneSEVPA<L, JSONSymbol> automaton, final boolean checkGraph) {
        final ReachabilityRelation<L> reachabilityRelation = ReachabilityRelation.computeReachabilityRelation(automaton,
                checkGraph);
        final OnAcceptingPathRelation<L> onAcceptingRelation = OnAcceptingPathRelation.computeRelation(automaton,
                reachabilityRelation, checkGraph);
        if (reachabilityRelation.size() == 0) {
            return null;
        }

        return new KeyGraph<>(automaton, reachabilityRelation, onAcceptingRelation, checkGraph);
    }

    /**
     * Constructs the key graph using the VPA, its {@link ReachabilityRelation}, and
     * its {@link OnAcceptingPathRelation}.
     * 
     * @see #graphFor(OneSEVPA, boolean)
     * @param automaton               The 1-SEVPA
     * @param reachabilityRelation    Its reachability relation
     * @param onAcceptingPathRelation Its relation that indicates whether a location
     *                                is on an accepting path
     * @param checkGraph              If true, checks that the graph does not
     *                                contain a path where a key is seen multiple
     *                                times
     */
    public KeyGraph(final OneSEVPA<L, JSONSymbol> automaton, final ReachabilityRelation<L> reachabilityRelation,
            final OnAcceptingPathRelation<L> onAcceptingPathRelation, final boolean checkGraph) {
        this.automaton = automaton;

        this.graph = constructGraph(reachabilityRelation, onAcceptingPathRelation);
        propagateIsOnPathToAcceptingForLocations(automaton.getLocations());

        if (checkGraph) {
            final List<NodeInGraph<L>> pathWithDuplicateKeys = hasPathWithDuplicateKeys();
            this.hasPathWithDuplicateKeys = !pathWithDuplicateKeys.isEmpty();
            witnessInvalid = constructWitnessDuplicate(pathWithDuplicateKeys, reachabilityRelation,
                    onAcceptingPathRelation);
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

    private ImmutableGraph<NodeInGraph<L>> constructGraph(final ReachabilityRelation<L> reachabilityRelation,
            final OnAcceptingPathRelation<L> onAcceptingRelation) {
        final L binLocation = onAcceptingRelation.identifyBinLocation(automaton);

        final ReachabilityRelation<L> valueReachabilityRelation = reachabilityRelation
                .computePotentialValueReachabilityRelation(automaton, false);

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

                for (final InReachabilityRelation<L> inValueRelation : valueReachabilityRelation
                        .getLocationsAndInfoInRelationWithStart(locationAfterKey)) {
                    if (Objects.equals(inValueRelation.getTarget(), binLocation)) {
                        continue;
                    }

                    final L locationAfterValue = inValueRelation.getTarget();
                    final NodeInGraph<L> node = new NodeInGraph<>(startLocation, locationAfterValue, key, automaton,
                            binLocation);
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
        for (final NodeInGraph<L> start : startingNodes) {
            final List<NodeInGraph<L>> seenNodes = new LinkedList<>();
            if (hasPathWithDuplicateKeys(start, seenNodes, new LinkedHashSet<>())) {
                return seenNodes;
            }
        }
        return Collections.emptyList();
    }

    private boolean hasPathWithDuplicateKeys(final NodeInGraph<L> currentNode, final List<NodeInGraph<L>> seenNodes,
            final Set<JSONSymbol> keys) {
        // We have a loop and we see the same key twice
        if (seenNodes.contains(currentNode)) {
            seenNodes.add(currentNode);
            return true;
        }

        seenNodes.add(currentNode);
        // We have a duplicate key
        if (!keys.add(currentNode.getSymbol())) {
            return true;
        }

        for (final NodeInGraph<L> successor : graph.successors(currentNode)) {
            if (hasPathWithDuplicateKeys(successor, seenNodes, keys)) {
                return true;
            }
        }
        keys.remove(currentNode.getSymbol());
        seenNodes.remove(seenNodes.size() - 1);
        return false;
    }

    private Word<JSONSymbol> constructWitnessDuplicate(final List<NodeInGraph<L>> path,
            final ReachabilityRelation<L> reachabilityRelation,
            final OnAcceptingPathRelation<L> onAcceptingPathRelation) {
        if (isValid()) {
            return null;
        }

        final L target = path.get(path.size() - 1).getTargetLocation();
        assert onAcceptingPathRelation.getWitnessToIntermediate(target) != null : target;
        assert onAcceptingPathRelation.getWitnessFromIntermediate(target) != null;

        final WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.append(onAcceptingPathRelation.getWitnessToIntermediate(target));
        int i = 0;
        for (final NodeInGraph<L> node : path) {
            builder.append(node.getSymbol());
            final L intermediate = automaton.getInternalSuccessor(node.getStartLocation(), node.getSymbol());
            builder.append(reachabilityRelation.getWitness(intermediate, node.getTargetLocation()));
            if (++i < path.size()) {
                builder.append(JSONSymbol.commaSymbol);
            }
        }
        builder.append(onAcceptingPathRelation.getWitnessFromIntermediate(target));

        return builder.toWord();
    }

    /**
     * Whether the graph does not contain a path where the same key is seen multiple
     * times.
     * 
     * @return True if and only if the graph does not contain a path where the same
     *         key is seen multiple times
     */
    public boolean isValid() {
        return !hasPathWithDuplicateKeys;
    }

    /**
     * If the graph is invalid, provides a witness.
     * 
     * @return A witness that the key graph is invalid
     */
    public Word<JSONSymbol> getWitnessInvalid() {
        return witnessInvalid;
    }

    private void propagateIsOnPathToAcceptingForLocations(final Collection<L> locations) {
        for (final NodeInGraph<L> node : Traverser.forGraph(graph).depthFirstPostOrder(startingNodes)) {
            final Set<NodeInGraph<L>> successors = graph.successors(node);
            for (final NodeInGraph<L> successor : successors) {
                for (final L location : locations) {
                    final int locationId = automaton.getLocationId(location);
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

    /**
     * The size of the key graph, i.e., the number of vertices.
     * 
     * @return The number of vertices
     */
    public int size() {
        return nodes().size();
    }

    @Nullable
    private NodeInGraph<L> getNode(final L sourceLocation, final JSONSymbol key, final L targetLocation) {
        return getNode(PairSourceToReached.of(sourceLocation, targetLocation), key);
    }

    @Nullable
    private NodeInGraph<L> getNode(final PairSourceToReached<L> pairSourceToReached, final JSONSymbol key) {
        for (final NodeInGraph<L> node : nodes()) {
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
    private boolean isAcceptingForLocation(final NodeInGraph<L> node, final L locationBeforeCall) {
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
    boolean isAcceptingForLocation(final L sourceLocation, final JSONSymbol key, final L targetLocation,
            final L locationBeforeCall) {
        return isAcceptingForLocation(getNode(sourceLocation, key, targetLocation), locationBeforeCall);
    }

    /**
     * Gets all the nodes that can read the given key.
     * 
     * @param key The key
     * @return A list with the nodes
     */
    public List<NodeInGraph<L>> getNodesForKey(final JSONSymbol key) {
        return keyToNodes.getOrDefault(key, Collections.emptyList());
    }

    /**
     * Gets all the locations for which there exists an internal transition reading
     * the given key.
     * 
     * @param key The key
     * @return
     */
    public Set<L> getLocationsReadingKey(final JSONSymbol key) {
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
    public Set<L> getLocationsWithReturnTransitionOnUnmarkedPathsWithAllKeysSeen(final Set<JSONSymbol> seenKeys,
            final Collection<L> locationsBeforeCall, final Collection<NodeInGraph<L>> rejectedNodes) {
        final Set<L> locationsReadingClosing = new LinkedHashSet<>();
        for (final NodeInGraph<L> initial : startingNodes) {
            depthFirstExploreForAcceptingNodes(initial, new LinkedHashSet<>(), locationsReadingClosing, seenKeys,
                    locationsBeforeCall, rejectedNodes);
        }
        return locationsReadingClosing;
    }

    private void depthFirstExploreForAcceptingNodes(final NodeInGraph<L> current,
            final Set<JSONSymbol> seenKeysInExploration, final Set<L> locationsReadingClosing,
            final Set<JSONSymbol> seenKeysInAutomaton, final Collection<L> locationsBeforeCall,
            final Collection<NodeInGraph<L>> rejectedNodes) {
        // The path has a node that is rejected
        if (rejectedNodes.contains(current)) {
            return;
        }

        // We know we will never be able to reach a state from which we can read a
        // return symbol matching the locations before the call
        // @formatter:off
        final boolean canReachAnAcceptingLocation = locationsBeforeCall.stream()
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
        // We have a problem in the graph. We just ignore this path
        if (!seenKeysInExploration.add(key)) {
            return;
        }

        // @formatter:off
        final boolean acceptingForOneLocation = locationsBeforeCall.stream()
            .map(locationBeforeCall -> automaton.getLocationId(locationBeforeCall))
            .filter(locationId -> current.isAcceptingForLocation(locationId))
            .findAny().isPresent()
        ;
        // @formatter:on

        if (acceptingForOneLocation) {
            // All the keys seen from the input must be on the path and vice-versa
            if (seenKeysInAutomaton.size() == seenKeysInExploration.size()) {
                final boolean missingKey = seenKeysInExploration.stream()
                        .filter(seenKey -> !seenKeysInAutomaton.contains(seenKey))
                        .findAny().isPresent();

                if (!missingKey) {
                    locationsReadingClosing.add(current.getTargetLocation());
                }
            }
        }

        final Set<NodeInGraph<L>> successors = graph.successors(current);
        for (final NodeInGraph<L> successor : successors) {
            depthFirstExploreForAcceptingNodes(successor, seenKeysInExploration, locationsReadingClosing,
                    seenKeysInAutomaton, locationsBeforeCall, rejectedNodes);
        }

        seenKeysInExploration.remove(key);
    }
}
