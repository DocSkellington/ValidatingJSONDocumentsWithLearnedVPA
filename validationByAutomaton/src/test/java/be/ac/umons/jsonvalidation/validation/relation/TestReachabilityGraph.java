package be.ac.umons.jsonvalidation.validation.relation;

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableGraph;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.validation.Automata;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.Word;

public class TestReachabilityGraph {
    @Test
    public void testStraightforwardAutomatonGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructStraightforwardAutomaton();
        KeyGraph<Location> graph = KeyGraph.graphFor(automaton, true);
        Assert.assertTrue(graph.isValid());
        Set<NodeInGraph<Location>> nodes = graph.nodes();
        Set<EndpointPair<NodeInGraph<Location>>> edges = graph.edges();

        JSONSymbol k1Sym = JSONSymbol.toSymbol("k1");
        JSONSymbol k2Sym = JSONSymbol.toSymbol("k2");

        Location q0 = automaton.getLocation(0);
        Location q2 = automaton.getLocation(2);
        Location q3 = automaton.getLocation(3);
        Location q5 = automaton.getLocation(5);

        InRelation<Location> q0Toq2Relation = InRelation.of(q0, q2, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol));
        NodeInGraph<Location> q0Toq2 = new NodeInGraph<>(q0Toq2Relation, k1Sym, automaton, new HashSet<>());
        InRelation<Location> q3Toq5Relation = InRelation.of(q3, q5, Word.fromSymbols(k2Sym, JSONSymbol.trueSymbol));
        NodeInGraph<Location> q3Toq5 = new NodeInGraph<>(q3Toq5Relation, k2Sym, automaton, new HashSet<>());

        Assert.assertEquals(nodes.size(), 2);
        Assert.assertTrue(nodes.contains(q0Toq2));
        Assert.assertTrue(nodes.contains(q3Toq5));

        Assert.assertEquals(edges.size(), 1);
        Assert.assertTrue(graph.getGraph().hasEdgeConnecting(q0Toq2, q3Toq5));
        
        for (Location location : automaton.getLocations()) {
            Assert.assertFalse(graph.isAcceptingForLocation(q0, k1Sym, q2, location));
        }
        for (Location location : automaton.getLocations()) {
            if (location.getIndex() == 0) {
                Assert.assertTrue(graph.isAcceptingForLocation(q3, k2Sym, q5, location));
            }
            else {
                Assert.assertFalse(graph.isAcceptingForLocation(q3, k2Sym, q5, location));
            }
        }
    }

    @Test
    public void testSmallTwoBranchesAutomatonGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
        KeyGraph<Location> graph = KeyGraph.graphFor(automaton, true);
        Assert.assertTrue(graph.isValid());
        Set<NodeInGraph<Location>> nodes = graph.nodes();
        Set<EndpointPair<NodeInGraph<Location>>> edges = graph.edges();
        ImmutableGraph<NodeInGraph<Location>> actualGraph = graph.getGraph();

        JSONSymbol k1Sym = JSONSymbol.toSymbol("k1");
        JSONSymbol k2Sym = JSONSymbol.toSymbol("k2");

        Location q0 = automaton.getLocation(0);
        Location q2 = automaton.getLocation(2);
        Location q3 = automaton.getLocation(3);
        Location q5 = automaton.getLocation(5);
        Location q7 = automaton.getLocation(7);
        Location q8 = automaton.getLocation(8);

        InRelation<Location> q0Toq2Relation = InRelation.of(q0, q2, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol));
        InRelation<Location> q0Toq7Relation = InRelation.of(q0, q7, Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol));
        InRelation<Location> q3Toq5Relation = InRelation.of(q3, q5, Word.fromSymbols(k2Sym, JSONSymbol.trueSymbol));
        InRelation<Location> q8Toq5Relation = InRelation.of(q8, q5, Word.fromSymbols(k2Sym, JSONSymbol.stringSymbol));
        NodeInGraph<Location> q0Toq2 = new NodeInGraph<>(q0Toq2Relation, k1Sym, automaton, new HashSet<>());
        NodeInGraph<Location> q0Toq7 = new NodeInGraph<>(q0Toq7Relation, k1Sym, automaton, new HashSet<>());
        NodeInGraph<Location> q3Toq5 = new NodeInGraph<>(q3Toq5Relation, k2Sym, automaton, new HashSet<>());
        NodeInGraph<Location> q8Toq5 = new NodeInGraph<>(q8Toq5Relation, k2Sym, automaton, new HashSet<>());

        Assert.assertEquals(nodes.size(), 4);
        Assert.assertTrue(nodes.contains(q0Toq2));
        Assert.assertTrue(nodes.contains(q0Toq7));
        Assert.assertTrue(nodes.contains(q3Toq5));
        Assert.assertTrue(nodes.contains(q8Toq5));

        Assert.assertEquals(edges.size(), 2);
        Assert.assertTrue(actualGraph.hasEdgeConnecting(q0Toq2, q3Toq5));
        Assert.assertTrue(actualGraph.hasEdgeConnecting(q0Toq7, q8Toq5));

        for (Location location : automaton.getLocations()) {
            Assert.assertFalse(graph.isAcceptingForLocation(q0, k1Sym, q2, location));
            Assert.assertFalse(graph.isAcceptingForLocation(q0, k1Sym, q7, location));
        }
        for (Location location : automaton.getLocations()) {
            if (location.getIndex() == 0) {
                Assert.assertTrue(graph.isAcceptingForLocation(q3, k2Sym, q5, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q8, k2Sym, q5, location));
            }
            else {
                Assert.assertFalse(graph.isAcceptingForLocation(q3, k2Sym, q5, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q8, k2Sym, q5, location));
            }
        }
    }

    private void checkNodesInAutomatonWithOptionalKeysGraph(DefaultOneSEVPA<JSONSymbol> automaton, KeyGraph<Location> graph) {
        Assert.assertTrue(graph.isValid());

        Set<NodeInGraph<Location>> nodes = graph.nodes();
        Set<EndpointPair<NodeInGraph<Location>>> edges = graph.edges();
        ImmutableGraph<NodeInGraph<Location>> actualGraph = graph.getGraph();

        JSONSymbol k1Sym = JSONSymbol.toSymbol("k1");
        JSONSymbol k2Sym = JSONSymbol.toSymbol("k2");
        JSONSymbol o1Sym = JSONSymbol.toSymbol("o1");
        JSONSymbol o2Sym = JSONSymbol.toSymbol("o2");

        Location q0 = automaton.getLocation(0);
        Location q2 = automaton.getLocation(2);
        Location q3 = automaton.getLocation(3);
        Location q6 = automaton.getLocation(6);
        Location q7 = automaton.getLocation(7);
        Location q8 = automaton.getLocation(8);
        Location q10 = automaton.getLocation(10);

        InRelation<Location> q0Toq2Relation = InRelation.of(q0, q2, Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol));
        InRelation<Location> q0Toq6Relation = InRelation.of(q0, q6, Word.fromSymbols(k2Sym, JSONSymbol.integerSymbol));
        InRelation<Location> q3Toq7Relation = InRelation.of(q3, q7, Word.fromSymbols(o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol));
        InRelation<Location> q8Toq10Relation = InRelation.of(q8, q10, Word.fromSymbols(o2Sym, JSONSymbol.trueSymbol));

        NodeInGraph<Location> q0Toq2 = new NodeInGraph<>(q0Toq2Relation, k1Sym, automaton, new HashSet<>());
        NodeInGraph<Location> q0Toq6 = new NodeInGraph<>(q0Toq6Relation, k2Sym, automaton, new HashSet<>());
        NodeInGraph<Location> q3Toq7 = new NodeInGraph<>(q3Toq7Relation, o1Sym, automaton, new HashSet<>());
        NodeInGraph<Location> q8Toq10 = new NodeInGraph<>(q8Toq10Relation, o2Sym, automaton, new HashSet<>());

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
                Assert.assertTrue(graph.isAcceptingForLocation(q0, k1Sym, q2, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q0, k2Sym, q6, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q3, o1Sym, q7, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q8, o2Sym, q10, location));
            }
            else if (location.getIndex() == 4) {
                Assert.assertFalse(graph.isAcceptingForLocation(q0, k1Sym, q2, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q0, k2Sym, q6, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q3, o1Sym, q7, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q8, o2Sym, q10, location));
            }
            else {
                Assert.assertFalse(graph.isAcceptingForLocation(q0, k1Sym, q2, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q0, k2Sym, q6, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q3, o1Sym, q7, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q8, o2Sym, q10, location));
            }
        }
    }

    @Test
    public void testAutomatonWithOptionalKeysGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeys();
        KeyGraph<Location> graph = KeyGraph.graphFor(automaton, true);
        checkNodesInAutomatonWithOptionalKeysGraph(automaton, graph);
    }

    @Test
    public void testAutomatonWithOptionalKeysAndExplicitBinStateGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeysAndExplicitBinState();
        KeyGraph<Location> graph = KeyGraph.graphFor(automaton, true);
        checkNodesInAutomatonWithOptionalKeysGraph(automaton, graph);
    }

    @Test
    public void testAutomatonWithDuplicateKeys() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithDuplicateKeys();
        KeyGraph<Location> graph = KeyGraph.graphFor(automaton, true);
        Assert.assertFalse(graph.isValid());
    }
}
