package be.ac.umons.permutationautomaton.relation;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.permutationautomaton.Automata;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.VPDAlphabet;

public class TestReachabilityRelation {
    @Test
    public void testCompose() {
        VPDAlphabet<JSONSymbol> alphabet = Automata.constructAlphabet("test");
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
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructStraightforwardAutomaton();
        JSONSymbol openingCurly = JSONSymbol.openingCurlyBraceSymbol;
        JSONSymbol closingCurly = JSONSymbol.closingCurlyBraceSymbol;

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
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeys();
        JSONSymbol openingCurly = JSONSymbol.openingCurlyBraceSymbol;
        JSONSymbol closingCurly = JSONSymbol.closingCurlyBraceSymbol;

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
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
        ReachabilityRelation commaRelation = ReachabilityRelation.computeCommaRelation(automaton);
        Assert.assertEquals(commaRelation.size(), 2);
        Assert.assertTrue(commaRelation.areInRelation(automaton.getLocation(2), automaton.getLocation(3)));
        Assert.assertTrue(commaRelation.areInRelation(automaton.getLocation(7), automaton.getLocation(8)));
    }

    @Test
    public void testSmallTwoBranchesAutomatonInternalRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
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
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
        ReachabilityRelation commaRelation = ReachabilityRelation.computeCommaRelation(automaton);
        ReachabilityRelation internalRelation = ReachabilityRelation.computeInternalRelation(automaton);
        ReachabilityRelation wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation);

        Assert.assertEquals(wellMatchedRelation.size(), 1);
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(0), automaton.getLocation(6)));
    }

    @Test
    public void testAutomatonWithOptionalKeysWellMatchedRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeys();

        ReachabilityRelation commaRelation = ReachabilityRelation.computeCommaRelation(automaton);
        ReachabilityRelation internalRelation = ReachabilityRelation.computeInternalRelation(automaton);
        ReachabilityRelation wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation);

        Assert.assertEquals(wellMatchedRelation.size(), 2);
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(0), automaton.getLocation(11)));
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(4), automaton.getLocation(7)));
    }
}
