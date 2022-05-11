package be.ac.umons.permutationautomaton.relation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;

public class ReachabilityGraph {
    private final ImmutableValueGraph<NodeInGraph, Boolean> graph;
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
        
        final ImmutableValueGraph.Builder<NodeInGraph, Boolean> builder = ValueGraphBuilder
            .directed()
            .allowsSelfLoops(false)
            .nodeOrder(ElementOrder.insertion())
            .incidentEdgeOrder(ElementOrder.stable())
            .<NodeInGraph, Boolean>immutable()
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
                    builder.putEdgeValue(relationToNode.get(source), relationToNode.get(target), false);
                }
            }
        }

        this.graph = builder.build();
    }

    ImmutableValueGraph<NodeInGraph, Boolean> getGraph() {
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
}
