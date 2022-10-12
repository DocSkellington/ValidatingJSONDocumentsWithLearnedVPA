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

import java.io.IOException;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableGraph;

import be.ac.umons.jsonvalidation.Automata;
import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;

/**
 * @author Gaëtan Staquet
 */
public class TestKeyGraph {
    @Test
    public void testStraightforwardAutomatonGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructStraightforwardAutomaton();
        ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(automaton, true);
        OnAcceptingPathRelation<Location> onAcceptingRelation = OnAcceptingPathRelation.computeRelation(automaton,
                reachabilityRelation, true);
        KeyGraph<Location> graph = new KeyGraph<>(automaton, reachabilityRelation, onAcceptingRelation, true);
        Assert.assertTrue(graph.isValid());
        Set<NodeInGraph<Location>> nodes = graph.nodes();
        Set<EndpointPair<NodeInGraph<Location>>> edges = graph.edges();

        JSONSymbol k1Sym = JSONSymbol.toSymbol("k1");
        JSONSymbol k2Sym = JSONSymbol.toSymbol("k2");

        Location q0 = automaton.getLocation(0);
        Location q2 = automaton.getLocation(2);
        Location q3 = automaton.getLocation(3);
        Location q5 = automaton.getLocation(5);

        NodeInGraph<Location> q0Toq2 = new NodeInGraph<>(q0, q2, k1Sym, automaton, null);
        NodeInGraph<Location> q3Toq5 = new NodeInGraph<>(q3, q5, k2Sym, automaton, null);

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
            } else {
                Assert.assertFalse(graph.isAcceptingForLocation(q3, k2Sym, q5, location));
            }
        }
    }

    @Test
    public void testSmallTwoBranchesAutomatonGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
        ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(automaton, true);
        OnAcceptingPathRelation<Location> onAcceptingRelation = OnAcceptingPathRelation.computeRelation(automaton,
                reachabilityRelation, true);
        KeyGraph<Location> graph = new KeyGraph<>(automaton, reachabilityRelation, onAcceptingRelation, true);
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

        NodeInGraph<Location> q0Toq2 = new NodeInGraph<>(q0, q2, k1Sym, automaton, null);
        NodeInGraph<Location> q0Toq7 = new NodeInGraph<>(q0, q7, k1Sym, automaton, null);
        NodeInGraph<Location> q3Toq5 = new NodeInGraph<>(q3, q5, k2Sym, automaton, null);
        NodeInGraph<Location> q8Toq5 = new NodeInGraph<>(q8, q5, k2Sym, automaton, null);

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
            } else {
                Assert.assertFalse(graph.isAcceptingForLocation(q3, k2Sym, q5, location));
                Assert.assertFalse(graph.isAcceptingForLocation(q8, k2Sym, q5, location));
            }
        }
    }

    private void checkNodesInAutomatonWithOptionalKeysGraph(DefaultOneSEVPA<JSONSymbol> automaton,
            KeyGraph<Location> graph) {
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

        NodeInGraph<Location> q0Toq2 = new NodeInGraph<>(q0, q2, k1Sym, automaton, null);
        NodeInGraph<Location> q0Toq6 = new NodeInGraph<>(q0, q6, k2Sym, automaton, null);
        NodeInGraph<Location> q3Toq7 = new NodeInGraph<>(q3, q7, o1Sym, automaton, null);
        NodeInGraph<Location> q8Toq10 = new NodeInGraph<>(q8, q10, o2Sym, automaton, null);

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
            } else if (location.getIndex() == 4) {
                Assert.assertFalse(graph.isAcceptingForLocation(q0, k1Sym, q2, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q0, k2Sym, q6, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q3, o1Sym, q7, location));
                Assert.assertTrue(graph.isAcceptingForLocation(q8, o2Sym, q10, location));
            } else {
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
        ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(automaton, true);
        OnAcceptingPathRelation<Location> onAcceptingRelation = OnAcceptingPathRelation.computeRelation(automaton,
                reachabilityRelation, true);
        KeyGraph<Location> graph = new KeyGraph<>(automaton, reachabilityRelation, onAcceptingRelation, true);
        checkNodesInAutomatonWithOptionalKeysGraph(automaton, graph);
    }

    @Test
    public void testAutomatonWithOptionalKeysAndExplicitBinStateGraph() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeysAndExplicitBinState();
        ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(automaton, true);
        OnAcceptingPathRelation<Location> onAcceptingRelation = OnAcceptingPathRelation.computeRelation(automaton,
                reachabilityRelation, true);
        KeyGraph<Location> graph = new KeyGraph<>(automaton, reachabilityRelation, onAcceptingRelation, true);
        checkNodesInAutomatonWithOptionalKeysGraph(automaton, graph);
    }

    @Test
    public void testAutomatonWithDuplicateKeys() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithDuplicateKeys();
        ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(automaton, true);
        OnAcceptingPathRelation<Location> onAcceptingRelation = OnAcceptingPathRelation.computeRelation(automaton,
                reachabilityRelation, true);
        KeyGraph<Location> graph = new KeyGraph<>(automaton, reachabilityRelation, onAcceptingRelation, true);
        Assert.assertFalse(graph.isValid());
        Assert.assertNotNull(graph.getWitnessInvalid());
    }

    @Test
    public void testAutomatonWithCycle() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithCycleReadingAKey();
        ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(automaton, true);
        OnAcceptingPathRelation<Location> onAcceptingRelation = OnAcceptingPathRelation.computeRelation(automaton,
                reachabilityRelation, true);
        KeyGraph<Location> graph = new KeyGraph<>(automaton, reachabilityRelation, onAcceptingRelation, true);
        Assert.assertFalse(graph.isValid());
        Assert.assertNotNull(graph.getWitnessInvalid());
    }

    @Test
    public void testWitnessCycleInGraph() throws IOException {
        final InputModelDeserializer<JSONSymbol, DefaultOneSEVPA<JSONSymbol>> parser = DOTParsers
                .oneSEVPA(JSONSymbol::toSymbol);
        final DefaultOneSEVPA<JSONSymbol> vpa = parser.readModel(getClass().getResource("/automaton.dot")).model;
        ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation.computeReachabilityRelation(vpa,
                true);
        OnAcceptingPathRelation<Location> onAcceptingRelation = OnAcceptingPathRelation.computeRelation(vpa,
                reachabilityRelation, true);
        KeyGraph<Location> graph = new KeyGraph<>(vpa, reachabilityRelation, onAcceptingRelation, true);
        Assert.assertNotNull(graph.getWitnessInvalid());
        Assert.assertTrue(vpa.accepts(graph.getWitnessInvalid()));
    }
}
