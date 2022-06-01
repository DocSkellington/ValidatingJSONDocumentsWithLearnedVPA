package be.ac.umons.learningjson.validation.relation;

import java.util.HashSet;
import java.util.Set;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableGraph;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.learningjson.validation.Automata;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;

public class TestReachabilityGraph {
    @Test
    public void testStraightforwardAutomatonGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructStraightforwardAutomaton();
        ReachabilityGraph graph = new ReachabilityGraph(automaton);
        Assert.assertTrue(graph.isValid());
        Set<NodeInGraph> nodes = graph.nodes();
        Set<EndpointPair<NodeInGraph>> edges = graph.edges();

        Location q0 = automaton.getLocation(0);
        Location q2 = automaton.getLocation(2);
        Location q3 = automaton.getLocation(3);
        Location q5 = automaton.getLocation(5);
        InRelation q0Toq2Relation = InRelation.of(q0, q2);
        NodeInGraph q0Toq2 = new NodeInGraph(q0Toq2Relation, JSONSymbol.toSymbol("k1"), automaton, new HashSet<>());
        InRelation q3Toq5Relation = InRelation.of(q3, q5);
        NodeInGraph q3Toq5 = new NodeInGraph(q3Toq5Relation, JSONSymbol.toSymbol("k2"), automaton, new HashSet<>());

        Assert.assertEquals(nodes.size(), 2);
        Assert.assertTrue(nodes.contains(q0Toq2));
        Assert.assertTrue(nodes.contains(q3Toq5));

        Assert.assertEquals(edges.size(), 1);
        Assert.assertTrue(graph.getGraph().hasEdgeConnecting(q0Toq2, q3Toq5));
        
        for (Location location : automaton.getLocations()) {
            Assert.assertFalse(graph.isAcceptingForLocation(q0Toq2Relation, location));
        }
        for (Location location : automaton.getLocations()) {
            if (location.getIndex() == 0) {
                Assert.assertTrue(graph.isAcceptingForLocation(q3Toq5Relation, location));
            }
            else {
                Assert.assertFalse(graph.isAcceptingForLocation(q3Toq5Relation, location));
            }
        }
    }

    @Test
    public void testSmallTwoBranchesAutomatonGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
        ReachabilityGraph graph = new ReachabilityGraph(automaton);
        Assert.assertTrue(graph.isValid());
        Set<NodeInGraph> nodes = graph.nodes();
        Set<EndpointPair<NodeInGraph>> edges = graph.edges();
        ImmutableGraph<NodeInGraph> actualGraph = graph.getGraph();

        Location q0 = automaton.getLocation(0);
        Location q2 = automaton.getLocation(2);
        Location q3 = automaton.getLocation(3);
        Location q5 = automaton.getLocation(5);
        Location q7 = automaton.getLocation(7);
        Location q8 = automaton.getLocation(8);
        InRelation q0Toq2Relation = InRelation.of(q0, q2);
        InRelation q0Toq7Relation = InRelation.of(q0, q7);
        InRelation q3Toq5Relation = InRelation.of(q3, q5);
        InRelation q8Toq5Relation = InRelation.of(q8, q5);
        NodeInGraph q0Toq2 = new NodeInGraph(q0Toq2Relation, JSONSymbol.toSymbol("k1"), automaton, new HashSet<>());
        NodeInGraph q0Toq7 = new NodeInGraph(q0Toq7Relation, JSONSymbol.toSymbol("k1"), automaton, new HashSet<>());
        NodeInGraph q3Toq5 = new NodeInGraph(q3Toq5Relation, JSONSymbol.toSymbol("k2"), automaton, new HashSet<>());
        NodeInGraph q8Toq5 = new NodeInGraph(q8Toq5Relation, JSONSymbol.toSymbol("k2"), automaton, new HashSet<>());

        Assert.assertEquals(nodes.size(), 4);
        Assert.assertTrue(nodes.contains(q0Toq2));
        Assert.assertTrue(nodes.contains(q0Toq7));
        Assert.assertTrue(nodes.contains(q3Toq5));
        Assert.assertTrue(nodes.contains(q8Toq5));

        Assert.assertEquals(edges.size(), 2);
        Assert.assertTrue(actualGraph.hasEdgeConnecting(q0Toq2, q3Toq5));
        Assert.assertTrue(actualGraph.hasEdgeConnecting(q0Toq7, q8Toq5));

        for (Location location : automaton.getLocations()) {
            Assert.assertFalse(graph.isAcceptingForLocation(q0Toq2Relation, location));
            Assert.assertFalse(graph.isAcceptingForLocation(q0Toq7Relation, location));
        }
        for (Location location : automaton.getLocations()) {
            if (location.getIndex() == 0) {
                Assert.assertTrue(graph.isAcceptingForLocation(q3Toq5Relation, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q8Toq5Relation, location));
            }
            else {
                Assert.assertFalse(graph.isAcceptingForLocation(q3Toq5Relation, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q8Toq5Relation, location));
            }
        }
    }

    private void checkNodesInAutomatonWithOptionalKeysGraph(DefaultOneSEVPA<JSONSymbol> automaton, ReachabilityGraph graph) {
        Assert.assertTrue(graph.isValid());

        Set<NodeInGraph> nodes = graph.nodes();
        Set<EndpointPair<NodeInGraph>> edges = graph.edges();
        ImmutableGraph<NodeInGraph> actualGraph = graph.getGraph();

        Location q0 = automaton.getLocation(0);
        Location q2 = automaton.getLocation(2);
        Location q3 = automaton.getLocation(3);
        Location q6 = automaton.getLocation(6);
        Location q7 = automaton.getLocation(7);
        Location q8 = automaton.getLocation(8);
        Location q10 = automaton.getLocation(10);
        InRelation q0Toq2Relation = InRelation.of(q0, q2);
        InRelation q0Toq6Relation = InRelation.of(q0, q6);
        InRelation q3Toq7Relation = InRelation.of(q3, q7);
        InRelation q8Toq10Relation = InRelation.of(q8, q10);
        NodeInGraph q0Toq2 = new NodeInGraph(q0Toq2Relation, JSONSymbol.toSymbol("k1"), automaton, new HashSet<>());
        NodeInGraph q0Toq6 = new NodeInGraph(q0Toq6Relation, JSONSymbol.toSymbol("k2"), automaton, new HashSet<>());
        NodeInGraph q3Toq7 = new NodeInGraph(q3Toq7Relation, JSONSymbol.toSymbol("o1"), automaton, new HashSet<>());
        NodeInGraph q8Toq10 = new NodeInGraph(q8Toq10Relation, JSONSymbol.toSymbol("o2"), automaton, new HashSet<>());

        Assert.assertEquals(nodes.size(), 4);
        Assert.assertTrue(nodes.contains(q0Toq2));
        Assert.assertTrue(nodes.contains(q0Toq6));
        Assert.assertTrue(nodes.contains(q3Toq7));
        Assert.assertTrue(nodes.contains(q8Toq10));

        Assert.assertEquals(edges.size(), 3);
        Assert.assertTrue(actualGraph.hasEdgeConnecting(q0Toq2, q3Toq7));
        Assert.assertTrue(actualGraph.hasEdgeConnecting(q0Toq6, q3Toq7));
        Assert.assertTrue(actualGraph.hasEdgeConnecting(q3Toq7, q8Toq10));
        
        for (Location location : automaton.getLocations()) {
            if (location.getIndex() == 0) {
                Assert.assertTrue(graph.isAcceptingForLocation(q0Toq2Relation, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q0Toq6Relation, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q3Toq7Relation, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q8Toq10Relation, location));
            }
            else if (location.getIndex() == 4) {
                Assert.assertFalse(graph.isAcceptingForLocation(q0Toq2Relation, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q0Toq6Relation, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q3Toq7Relation, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q8Toq10Relation, location));
            }
            else {
                Assert.assertFalse(graph.isAcceptingForLocation(q0Toq2Relation, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q0Toq6Relation, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q3Toq7Relation, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q8Toq10Relation, location));
            }
        }
    }

    @Test
    public void testAutomatonWithOptionalKeysGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeys();
        ReachabilityGraph graph = new ReachabilityGraph(automaton);
        checkNodesInAutomatonWithOptionalKeysGraph(automaton, graph);
    }

    @Test
    public void testAutomatonWithOptionalKeysAndExplicitBinStateGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeysAndExplicitBinState();
        ReachabilityGraph graph = new ReachabilityGraph(automaton);
        checkNodesInAutomatonWithOptionalKeysGraph(automaton, graph);
    }

    @Test
    public void testAutomatonWithDuplicateKeys() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithDuplicateKeys();
        ReachabilityGraph graph = new ReachabilityGraph(automaton);
        Assert.assertFalse(graph.isValid());
    }
}
