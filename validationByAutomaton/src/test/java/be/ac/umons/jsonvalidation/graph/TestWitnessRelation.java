package be.ac.umons.jsonvalidation.graph;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.jsonvalidation.Automata;
import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.Word;

public class TestWitnessRelation {
    @Test
    public void testWitnessRelationSmallTwoBranchesAutomaton() {
        final DefaultOneSEVPA<JSONSymbol> vpa = Automata.constructSmallTwoBranchesAutomaton();

        final ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation.computeReachabilityRelation(vpa);

        final WitnessRelation<Location> witnessRelation = WitnessRelation.computeWitnessRelation(vpa, reachabilityRelation);

        final JSONSymbol k1Sym = JSONSymbol.toSymbol("k1");
        final JSONSymbol k2Sym = JSONSymbol.toSymbol("k2");

        System.out.println(witnessRelation);
        Assert.assertEquals(witnessRelation.size(), vpa.size());

        for (InfoInWitnessRelation<Location> inRelation : witnessRelation) {
            Location start = vpa.getInitialLocation();
            Location target = inRelation.getTarget();
            Word<JSONSymbol> witnessToStart = inRelation.getWitnessToStart();
            Word<JSONSymbol> witnessFromTarget = inRelation.getWitnessFromTarget();

            if (start.equals(vpa.getLocation(0))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.epsilon());
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol));
                }
            }
            else {
                Assert.fail();
            }

            if (target.equals(vpa.getLocation(0))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
            }
            else if (target.equals(vpa.getLocation(1))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
            }
            else if (target.equals(vpa.getLocation(2))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
            }
            else if (target.equals(vpa.getLocation(3))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(k2Sym, JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
            }
            else if (target.equals(vpa.getLocation(4))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
            }
            else if (target.equals(vpa.getLocation(5))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.closingCurlyBraceSymbol));
            }
            else if (target.equals(vpa.getLocation(6))) {
                Assert.assertEquals(witnessFromTarget, Word.epsilon());
            }
            else if (target.equals(vpa.getLocation(7))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym, JSONSymbol.stringSymbol, JSONSymbol.closingCurlyBraceSymbol));
            }
            else if (target.equals(vpa.getLocation(8))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(k2Sym, JSONSymbol.stringSymbol, JSONSymbol.closingCurlyBraceSymbol));
            }
            else if (target.equals(vpa.getLocation(9))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.closingCurlyBraceSymbol));
            }
        }

        Assert.assertNull(witnessRelation.identifyBinLocation(vpa));
    }
}
