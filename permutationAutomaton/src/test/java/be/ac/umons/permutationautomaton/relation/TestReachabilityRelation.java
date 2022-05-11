package be.ac.umons.permutationautomaton.relation;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class TestReachabilityRelation {
    private static List<JSONSymbol> fromStringsToSymbols(String... symbols) {
        List<JSONSymbol> jsonSymbols = new ArrayList<>(symbols.length);
        for (String symbol : symbols) {
            jsonSymbols.add(JSONSymbol.toSymbol(symbol));
        }
        return jsonSymbols;
    }

    private static VPDAlphabet<JSONSymbol> constructAlphabet(String... symbols) {
        Alphabet<JSONSymbol> internalAlphabet = Alphabets.fromCollection(fromStringsToSymbols(symbols));
        Alphabet<JSONSymbol> callAlphabet = Alphabets.fromCollection(fromStringsToSymbols("{", "["));
        Alphabet<JSONSymbol> returnAlphabet = Alphabets.fromCollection(fromStringsToSymbols("}", "]"));
        return new DefaultVPDAlphabet<>(internalAlphabet, callAlphabet, returnAlphabet);
    }

    private static DefaultOneSEVPA<JSONSymbol> constructStraightforwardAutomaton() {
        VPDAlphabet<JSONSymbol> alphabet = constructAlphabet("k1", "k2", "k3", ",", "int", "str", "bool");
        DefaultOneSEVPA<JSONSymbol> automaton = new DefaultOneSEVPA<>(alphabet);

        Location q0 = automaton.addInitialLocation(false);
        Location q1 = automaton.addLocation(false);
        Location q2 = automaton.addLocation(false);
        Location q3 = automaton.addLocation(false);
        Location q4 = automaton.addLocation(false);
        Location q5 = automaton.addLocation(false);
        Location q6 = automaton.addLocation(true);

        automaton.setInternalSuccessor(q0, JSONSymbol.toSymbol("k1"), q1);

        automaton.setInternalSuccessor(q1, JSONSymbol.toSymbol("int"), q2);

        automaton.setInternalSuccessor(q2, JSONSymbol.toSymbol(","), q3);

        automaton.setInternalSuccessor(q3, JSONSymbol.toSymbol("k2"), q4);

        automaton.setInternalSuccessor(q4, JSONSymbol.toSymbol("bool"), q5);

        automaton.setReturnSuccessor(q5, JSONSymbol.toSymbol("}"),
                automaton.encodeStackSym(q0, JSONSymbol.toSymbol("{")), q6);

        return automaton;
    }

    private static DefaultOneSEVPA<JSONSymbol> constructSmallTwoBranchesAutomaton() {
        VPDAlphabet<JSONSymbol> alphabet = constructAlphabet("k1", "k2", "k3", ",", "int", "str", "bool");
        DefaultOneSEVPA<JSONSymbol> automaton = new DefaultOneSEVPA<>(alphabet);

        Location q0 = automaton.addInitialLocation(false);
        Location q1 = automaton.addLocation(false);
        Location q2 = automaton.addLocation(false);
        Location q3 = automaton.addLocation(false);
        Location q4 = automaton.addLocation(false);
        Location q5 = automaton.addLocation(false);
        Location q6 = automaton.addLocation(true);
        Location q7 = automaton.addLocation(false);
        Location q8 = automaton.addLocation(false);
        Location q9 = automaton.addLocation(false);

        automaton.setInternalSuccessor(q0, JSONSymbol.toSymbol("k1"), q1);

        automaton.setInternalSuccessor(q1, JSONSymbol.toSymbol("int"), q2);
        automaton.setInternalSuccessor(q1, JSONSymbol.toSymbol("str"), q7);

        automaton.setInternalSuccessor(q2, JSONSymbol.toSymbol(","), q3);

        automaton.setInternalSuccessor(q3, JSONSymbol.toSymbol("k2"), q4);

        automaton.setInternalSuccessor(q4, JSONSymbol.toSymbol("bool"), q5);

        automaton.setReturnSuccessor(q5, JSONSymbol.toSymbol("}"),
                automaton.encodeStackSym(q0, JSONSymbol.toSymbol("{")), q6);

        automaton.setInternalSuccessor(q7, JSONSymbol.toSymbol(","), q8);

        automaton.setInternalSuccessor(q8, JSONSymbol.toSymbol("k2"), q9);

        automaton.setInternalSuccessor(q9, JSONSymbol.toSymbol("str"), q5);

        return automaton;
    }

    private static DefaultOneSEVPA<JSONSymbol> constructAutomatonWithOptionalKeys() {
        VPDAlphabet<JSONSymbol> alphabet = constructAlphabet("k1", "k2", "o1", "o2", ",", "int", "str", "bool");
        DefaultOneSEVPA<JSONSymbol> automaton = new DefaultOneSEVPA<>(alphabet);

        Location q0 = automaton.addInitialLocation(false);
        Location q1 = automaton.addLocation(false);
        Location q2 = automaton.addLocation(false);
        Location q3 = automaton.addLocation(false);
        Location q4 = automaton.addLocation(false);
        Location q5 = automaton.addLocation(false);
        Location q6 = automaton.addLocation(false);
        Location q7 = automaton.addLocation(false);
        Location q8 = automaton.addLocation(false);
        Location q9 = automaton.addLocation(false);
        Location q10 = automaton.addLocation(false);
        Location q11 = automaton.addLocation(true);

        int q0StackSymbol = automaton.encodeStackSym(q0, JSONSymbol.toSymbol("{"));
        automaton.setInternalSuccessor(q0, JSONSymbol.toSymbol("k1"), q1);
        automaton.setInternalSuccessor(q0, JSONSymbol.toSymbol("k2"), q5);

        automaton.setInternalSuccessor(q1, JSONSymbol.toSymbol("str"), q2);

        automaton.setReturnSuccessor(q2, JSONSymbol.toSymbol("}"), q0StackSymbol, q11);
        automaton.setInternalSuccessor(q2, JSONSymbol.toSymbol(","), q3);

        automaton.setInternalSuccessor(q3, JSONSymbol.toSymbol("o1"), q4);

        int q4StackSymbol = automaton.encodeStackSym(q4, JSONSymbol.toSymbol("{"));

        automaton.setInternalSuccessor(q5, JSONSymbol.toSymbol("int"), q6);
        
        automaton.setReturnSuccessor(q6, JSONSymbol.toSymbol("}"), q4StackSymbol, q7);
        automaton.setInternalSuccessor(q6, JSONSymbol.toSymbol(","), q3);

        automaton.setReturnSuccessor(q7, JSONSymbol.toSymbol("}"), q0StackSymbol, q11);
        automaton.setReturnSuccessor(q7, JSONSymbol.toSymbol("}"), q4StackSymbol, q7);
        automaton.setInternalSuccessor(q7, JSONSymbol.toSymbol(","), q8);

        automaton.setInternalSuccessor(q8, JSONSymbol.toSymbol("o2"), q9);

        automaton.setInternalSuccessor(q9, JSONSymbol.toSymbol("bool"), q10);

        automaton.setReturnSuccessor(q10, JSONSymbol.toSymbol("}"), q0StackSymbol, q11);
        automaton.setReturnSuccessor(q10, JSONSymbol.toSymbol("}"), q4StackSymbol, q7);

        return automaton;
    }

    @Test
    public void testCompose() {
        VPDAlphabet<JSONSymbol> alphabet = constructAlphabet("test");
        Location q0 = new Location(alphabet, 0, false);
        Location q1 = new Location(alphabet, 1, false);
        Location q2 = new Location(alphabet, 2, false);
        Location q3 = new Location(alphabet, 3, false);

        ReachabilityRelation rel1 = new ReachabilityRelation();
        ReachabilityRelation rel2 = new ReachabilityRelation();

        rel1.add(q0, JSONSymbol.toSymbol("a"), q1);
        rel1.add(q1, JSONSymbol.toSymbol("b"), q2);

        rel2.add(q1, JSONSymbol.toSymbol("c"), q3);
        rel2.add(q2, JSONSymbol.toSymbol("d"), q0);

        ReachabilityRelation result = rel1.compose(rel2);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.areInRelation(q0, q3));
        Assert.assertTrue(result.areInRelation(q0, JSONSymbol.toSymbol("a"), q3));
        Assert.assertTrue(result.areInRelation(q1, q0));
        Assert.assertTrue(result.areInRelation(q1, JSONSymbol.toSymbol("b"), q0));

        result = rel2.compose(rel1);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.areInRelation(q2, q1));
        Assert.assertTrue(result.areInRelation(q2, JSONSymbol.toSymbol("d"), q1));
    }

    @Test
    public void testStraightforwardAutomatonPost() {
        DefaultOneSEVPA<JSONSymbol> automaton = constructStraightforwardAutomaton();
        JSONSymbol openingCurly = JSONSymbol.toSymbol("{");
        JSONSymbol closingCurly = JSONSymbol.toSymbol("}");

        ReachabilityRelation relation = new ReachabilityRelation();
        for (int i = 0 ; i <= 6 ; i++) {
            relation.add(automaton.getLocation(i), JSONSymbol.toSymbol("a"), automaton.getLocation(i));
            for (int j = i + 1 ; j <= 5 ; j++) {
                relation.add(automaton.getLocation(i), JSONSymbol.toSymbol("a"), automaton.getLocation(j));
            }
        }

        ReachabilityRelation result = relation.post(automaton, openingCurly, closingCurly);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.areInRelation(automaton.getLocation(0), automaton.getLocation(6)));
        Assert.assertTrue(result.areInRelation(automaton.getLocation(0), openingCurly, automaton.getLocation(6)));
    }

    @Test
    public void testAutomatonWithOptionalKeysPost() {
        DefaultOneSEVPA<JSONSymbol> automaton = constructAutomatonWithOptionalKeys();
        JSONSymbol openingCurly = JSONSymbol.toSymbol("{");
        JSONSymbol closingCurly = JSONSymbol.toSymbol("}");

        ReachabilityRelation relation = new ReachabilityRelation();
        // We add all pairs (p, p)
        for (int i = 0 ; i < automaton.size() ; i++) {
            relation.add(automaton.getLocation(i), JSONSymbol.toSymbol(""), automaton.getLocation(i));
        }

        for (int j = 1 ; j <= 4 ; j++) {
            relation.add(automaton.getLocation(0), JSONSymbol.toSymbol(""), automaton.getLocation(j));
        }
        relation.add(automaton.getLocation(0), JSONSymbol.toSymbol(""), automaton.getLocation(5));
        relation.add(automaton.getLocation(0), JSONSymbol.toSymbol(""), automaton.getLocation(6));

        for (int j = 2 ; j <= 4 ; j++) {
            relation.add(automaton.getLocation(1), JSONSymbol.toSymbol(""), automaton.getLocation(j));
        }

        relation.add(automaton.getLocation(2), JSONSymbol.toSymbol(""), automaton.getLocation(3));
        relation.add(automaton.getLocation(2), JSONSymbol.toSymbol(""), automaton.getLocation(4));

        relation.add(automaton.getLocation(3), JSONSymbol.toSymbol(""), automaton.getLocation(4));

        relation.add(automaton.getLocation(5), JSONSymbol.toSymbol(""), automaton.getLocation(6));

        relation.add(automaton.getLocation(7), JSONSymbol.toSymbol(""), automaton.getLocation(8));
        relation.add(automaton.getLocation(7), JSONSymbol.toSymbol(""), automaton.getLocation(9));
        relation.add(automaton.getLocation(7), JSONSymbol.toSymbol(""), automaton.getLocation(10));

        relation.add(automaton.getLocation(8), JSONSymbol.toSymbol(""), automaton.getLocation(9));
        relation.add(automaton.getLocation(8), JSONSymbol.toSymbol(""), automaton.getLocation(10));

        relation.add(automaton.getLocation(9), JSONSymbol.toSymbol(""), automaton.getLocation(10));

        ReachabilityRelation result = relation.post(automaton, openingCurly, closingCurly);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.areInRelation(automaton.getLocation(0), automaton.getLocation(11)));
        Assert.assertTrue(result.areInRelation(automaton.getLocation(4), automaton.getLocation(7)));
    }

    @Test
    public void testSmallTwoBranchesAutomatonCommaRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = constructSmallTwoBranchesAutomaton();
        ReachabilityRelation commaRelation = ReachabilityRelation.computeCommaRelation(automaton);
        Assert.assertEquals(commaRelation.size(), 2);
        Assert.assertTrue(commaRelation.areInRelation(automaton.getLocation(2), automaton.getLocation(3)));
        Assert.assertTrue(commaRelation.areInRelation(automaton.getLocation(7), automaton.getLocation(8)));
    }

    @Test
    public void testSmallTwoBranchesAutomatonInternalRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = constructSmallTwoBranchesAutomaton();
        ReachabilityRelation internalRelation = ReachabilityRelation.computeInternalRelation(automaton);
        Assert.assertEquals(internalRelation.size(), 7);
        Assert.assertTrue(internalRelation.areInRelation(automaton.getLocation(0), automaton.getLocation(1)));
        Assert.assertTrue(internalRelation.areInRelation(automaton.getLocation(1), automaton.getLocation(2)));
        Assert.assertTrue(internalRelation.areInRelation(automaton.getLocation(1), automaton.getLocation(7)));
        Assert.assertTrue(internalRelation.areInRelation(automaton.getLocation(3), automaton.getLocation(4)));
        Assert.assertTrue(internalRelation.areInRelation(automaton.getLocation(4), automaton.getLocation(5)));
        Assert.assertTrue(internalRelation.areInRelation(automaton.getLocation(8), automaton.getLocation(9)));
        Assert.assertTrue(internalRelation.areInRelation(automaton.getLocation(9), automaton.getLocation(5)));
    }

    @Test
    public void testSmallTwoBranchesAutomatonWellMatchedRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = constructSmallTwoBranchesAutomaton();
        ReachabilityRelation commaRelation = ReachabilityRelation.computeCommaRelation(automaton);
        ReachabilityRelation internalRelation = ReachabilityRelation.computeInternalRelation(automaton);
        ReachabilityRelation wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation);

        Assert.assertEquals(wellMatchedRelation.size(), 1);
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(0), automaton.getLocation(6)));
    }

    @Test
    public void testAutomatonWithOptionalKeysWellMatchedRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = constructAutomatonWithOptionalKeys();

        ReachabilityRelation commaRelation = ReachabilityRelation.computeCommaRelation(automaton);
        ReachabilityRelation internalRelation = ReachabilityRelation.computeInternalRelation(automaton);
        ReachabilityRelation wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation);

        Assert.assertEquals(wellMatchedRelation.size(), 2);
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(0), automaton.getLocation(11)));
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(4), automaton.getLocation(7)));
    }
}
