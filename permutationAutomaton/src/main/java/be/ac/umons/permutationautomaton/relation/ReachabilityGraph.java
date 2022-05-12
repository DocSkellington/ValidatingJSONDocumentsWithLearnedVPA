package be.ac.umons.permutationautomaton.relation;

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

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;

public class ReachabilityGraph {
    private final ImmutableGraph<NodeInGraph> graph;
    private final Map<JSONSymbol, List<NodeInGraph>> keyToNodes;
    private final List<NodeInGraph> startingNodes;

    public ReachabilityGraph(DefaultOneSEVPA<JSONSymbol> automaton) {
        this.keyToNodes = new HashMap<>();
        this.startingNodes = new LinkedList<>();

        final ReachabilityRelation commaRelation = ReachabilityRelation.computeCommaRelation(automaton);
        final ReachabilityRelation internalRelation = ReachabilityRelation.computeInternalRelation(automaton);
        final ReachabilityRelation wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton, commaRelation, internalRelation);

        final ReachabilityRelation unionRelation = new ReachabilityRelation();
        unionRelation.addAll(internalRelation);
        unionRelation.addAll(wellMatchedRelation);

        final ReachabilityRelation keyValueRelation = unionRelation.compose(unionRelation);
        
        final ImmutableGraph.Builder<NodeInGraph> builder = GraphBuilder
            .directed()
            .allowsSelfLoops(false)
            .nodeOrder(ElementOrder.insertion())
            .incidentEdgeOrder(ElementOrder.stable())
            .<NodeInGraph>immutable()
        ;

        final Map<InRelation, NodeInGraph> relationToNode = new HashMap<>();
        for (final InRelation inRel : keyValueRelation) {
            final NodeInGraph node = new NodeInGraph(inRel, automaton);
            relationToNode.put(inRel, node);
            builder.addNode(node);

            final JSONSymbol key = inRel.getSymbol();
            if (keyToNodes.containsKey(key)) {
                keyToNodes.get(key).add(node);
            }
            else {
                List<NodeInGraph> list = new LinkedList<>();
                list.add(node);
                keyToNodes.put(key, list);
            }

            if (inRel.getStart().equals(automaton.getInitialLocation())) {
                startingNodes.add(node);
            }
        }
        for (final InRelation source : keyValueRelation) {
            for (final InRelation target : keyValueRelation) {
                if (commaRelation.areInRelation(source.getTarget(), target.getStart())) {
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

    public boolean isAcceptingForLocation(InRelation inRelation, Location location) {
        return getNode(inRelation).isAcceptingForLocation(location.getIndex());
    }

    public List<NodeInGraph> getNodesForKey(JSONSymbol key) {
        return keyToNodes.getOrDefault(key, Collections.emptyList());
    }

    public void addLayerInStack() {
        for (NodeInGraph node : nodes()) {
            node.addLayerInStack();
        }
    }

    public void popLayerInStack() {
        for (NodeInGraph node : nodes()) {
            node.popLayerInStack();
        }
    }

    public Set<NodeInGraph> getNodesAcceptingForLocationsAndNotInRejectedPath(Set<JSONSymbol> seenKeys, Collection<Location> locationsBeforeCall) {
        final Set<NodeInGraph> acceptingNodes = new HashSet<>();
        final Set<NodeInGraph> seenNodes = new HashSet<>();
        for (NodeInGraph initial : startingNodes) {
            depthFirstExploreForAcceptingNodes(initial, seenNodes, new LinkedList<>(), acceptingNodes, seenKeys, locationsBeforeCall);
        }
        return acceptingNodes;
    }

    private void depthFirstExploreForAcceptingNodes(final NodeInGraph current, final Set<NodeInGraph> seenNodes, final LinkedList<JSONSymbol> seenKeysInExploration, final Set<NodeInGraph> acceptingNodes, final Set<JSONSymbol> seenKeysInAutomaton, final Collection<Location> locationsBeforeCall) {
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
        for (Location locationBeforeCall : locationsBeforeCall) {
            if (current.isAcceptingForLocation(locationBeforeCall)) {
                acceptingForOneLocation = true;
                break;
            }
        }

        if (acceptingForOneLocation) {
            if (seenKeysInAutomaton.size() == seenKeysInExploration.size()) {
                boolean allKeys = true;
                for (JSONSymbol seenKey : seenKeysInExploration) {
                    if (!seenKeysInAutomaton.contains(seenKey)) {
                        allKeys = false;
                    }
                }

                if (allKeys) {
                    acceptingNodes.add(current);
                }
            }
        }

        final Set<NodeInGraph> successors = graph.successors(current);
        for (final NodeInGraph successor : successors) {
            depthFirstExploreForAcceptingNodes(successor, seenNodes, seenKeysInExploration, acceptingNodes, seenKeysInAutomaton, locationsBeforeCall);
        }

        seenKeysInExploration.removeFirst();
    }
}
