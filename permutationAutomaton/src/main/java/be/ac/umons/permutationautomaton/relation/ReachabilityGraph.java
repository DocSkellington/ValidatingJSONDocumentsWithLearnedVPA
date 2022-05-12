package be.ac.umons.permutationautomaton.relation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;

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
public class ReachabilityGraph {
    private final ImmutableGraph<NodeInGraph> graph;
    private final Map<JSONSymbol, List<NodeInGraph>> keyToNodes = new HashMap<>();
    private final Map<JSONSymbol, List<Location>> keyToLocations = new HashMap<>();
    private final List<NodeInGraph> startingNodes = new LinkedList<>();

    public ReachabilityGraph(DefaultOneSEVPA<JSONSymbol> automaton) {
        final ReachabilityRelation commaRelation = ReachabilityRelation.computeCommaRelation(automaton);
        final ReachabilityRelation internalRelation = ReachabilityRelation.computeInternalRelation(automaton);
        final ReachabilityRelation wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation);

        final ReachabilityRelation unionRelation = internalRelation.union(wellMatchedRelation);

        final ReachabilityRelation keyValueRelation = unionRelation.compose(unionRelation);

        // @formatter:off
        final ImmutableGraph.Builder<NodeInGraph> builder = GraphBuilder
            .directed()
            .allowsSelfLoops(false)
            .nodeOrder(ElementOrder.insertion())
            .incidentEdgeOrder(ElementOrder.stable())
            .<NodeInGraph>immutable()
        ;
        // @formatter:on

        // We create the nodes
        final Map<InRelation, NodeInGraph> relationToNode = new HashMap<>();
        for (final InRelation inRel : keyValueRelation) {
            final NodeInGraph node = new NodeInGraph(inRel, automaton);
            relationToNode.put(inRel, node);
            builder.addNode(node);

            final JSONSymbol key = inRel.getSymbol();
            if (keyToNodes.containsKey(key)) {
                keyToNodes.get(key).add(node);
                keyToLocations.get(key).add(node.getStartLocation());
            } else {
                final List<NodeInGraph> listNode = new LinkedList<>();
                listNode.add(node);
                keyToNodes.put(key, listNode);
                final List<Location> listLocation = new LinkedList<>();
                listLocation.add(node.getStartLocation());
                keyToLocations.put(key, listLocation);
            }

            if (inRel.getStart().equals(automaton.getInitialLocation())) {
                startingNodes.add(node);
            }
        }
        // We create the edges
        for (final InRelation source : keyValueRelation) {
            for (final InRelation target : keyValueRelation) {
                if (commaRelation.areInRelation(source.getTarget(), JSONSymbol.commaSymbol, target.getStart())) {
                    builder.putEdge(relationToNode.get(source), relationToNode.get(target));
                }
            }
        }

        this.graph = builder.build();
    }

    ImmutableGraph<NodeInGraph> getGraph() {
        return graph;
    }

    Set<NodeInGraph> nodes() {
        return this.graph.nodes();
    }

    Set<EndpointPair<NodeInGraph>> edges() {
        return this.graph.edges();
    }

    @Nullable
    NodeInGraph getNode(InRelation inRelation) {
        for (NodeInGraph node : nodes()) {
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
    public boolean isAcceptingForLocation(InRelation inRelation, Location locationBeforeCall) {
        return getNode(inRelation).isAcceptingForLocation(locationBeforeCall.getIndex());
    }

    private List<NodeInGraph> getNodesForKey(JSONSymbol key) {
        return keyToNodes.getOrDefault(key, Collections.emptyList());
    }

    /**
     * Gets all the locations for which there exists an internal transition reading
     * the given key.
     * 
     * @param key The key
     * @return
     */
    public List<Location> getLocationsReadingKey(JSONSymbol key) {
        return keyToLocations.getOrDefault(key, Collections.emptyList());
    }

    /**
     * Adds a new layer in the stack of each node.
     */
    public void addLayerInStack() {
        for (NodeInGraph node : nodes()) {
            node.addLayerInStack();
        }
    }

    /**
     * Removes the top layer from the stack of each node.
     */
    public void popLayerInStack() {
        for (NodeInGraph node : nodes()) {
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
     * @param locationsReached The locations reached
     * @param lastKeyProcessed The last key that was read
     */
    public void markNodesToReject(final List<Location> locationsReached, final JSONSymbol lastKeyProcessed) {
        final List<NodeInGraph> nodesForKey = getNodesForKey(lastKeyProcessed);
        assert nodesForKey.size() == locationsReached.size();

        final Iterator<Location> itrLocations = locationsReached.iterator();
        final Iterator<NodeInGraph> itrNodes = nodesForKey.iterator();

        while (itrLocations.hasNext()) {
            assert itrNodes.hasNext();

            final Location location = itrLocations.next();
            final NodeInGraph node = itrNodes.next();
            if (location == null || !Objects.equals(node.getTargetLocation(), location)) {
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
    public Set<Location> getLocationsWithReturnTransitionOnUnmarkedPathsWithAllKeysSeen(Set<JSONSymbol> seenKeys,
            Collection<Location> locationsBeforeCall) {
        final Set<Location> acceptingNodes = new HashSet<>();
        final Set<NodeInGraph> seenNodes = new HashSet<>();
        for (NodeInGraph initial : startingNodes) {
            depthFirstExploreForAcceptingNodes(initial, seenNodes, new LinkedList<>(), acceptingNodes, seenKeys,
                    locationsBeforeCall);
        }
        return acceptingNodes;
    }

    private void depthFirstExploreForAcceptingNodes(final NodeInGraph current, final Set<NodeInGraph> seenNodes,
            final LinkedList<JSONSymbol> seenKeysInExploration, final Set<Location> acceptingLocations,
            final Set<JSONSymbol> seenKeysInAutomaton, final Collection<Location> locationsBeforeCall) {
        if (seenNodes.contains(current)) {
            return;
        }
        seenNodes.add(current);

        if (current.isRejected()) {
            return;
        }

        final JSONSymbol key = current.getInRelation().getSymbol();
        if (!seenKeysInAutomaton.contains(key)) {
            return;
        }
        seenKeysInExploration.addFirst(key);

        boolean acceptingForOneLocation = false;
        for (final Location locationBeforeCall : locationsBeforeCall) {
            if (current.isAcceptingForLocation(locationBeforeCall)) {
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
                    }
                }

                if (allKeys) {
                    acceptingLocations.add(current.getTargetLocation());
                }
            }
        }

        final Set<NodeInGraph> successors = graph.successors(current);
        for (final NodeInGraph successor : successors) {
            depthFirstExploreForAcceptingNodes(successor, seenNodes, seenKeysInExploration, acceptingLocations,
                    seenKeysInAutomaton, locationsBeforeCall);
        }

        seenKeysInExploration.removeFirst();
    }
}
