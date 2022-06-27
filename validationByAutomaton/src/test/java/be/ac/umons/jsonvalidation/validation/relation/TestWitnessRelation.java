package be.ac.umons.jsonvalidation.validation.relation;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.validation.Automata;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.Word;

public class TestWitnessRelation {
    @Test
    public void testWitnessRelationSmallTwoBranchesAutomaton() {
        final DefaultOneSEVPA<JSONSymbol> vpa = Automata.constructSmallTwoBranchesAutomaton();

        final ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation.computeReachabilityRelation(vpa, true);

        final WitnessRelation<Location> witnessRelation = WitnessRelation.computeWitnessRelation(vpa, reachabilityRelation);

        final JSONSymbol k1Sym = JSONSymbol.toSymbol("k1");
        final JSONSymbol k2Sym = JSONSymbol.toSymbol("k2");

        Assert.assertEquals(witnessRelation.size(), vpa.size() * vpa.size());

        for (InWitnessRelation<Location> inRelation : witnessRelation) {
            Location start = inRelation.getStart();
            Location target = inRelation.getTarget();
            Word<JSONSymbol> witnessToStart = inRelation.getWitnessToStart();
            Word<JSONSymbol> witnessFromTarget = inRelation.getWitnessFromTarget();

            if (start.equals(vpa.getLocation(0))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    System.out.println(inRelation);
                    Assert.assertEquals(witnessToStart, Word.epsilon());
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol));
                }
            }
            else if (start.equals(vpa.getLocation(1))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(k1Sym));
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym));
                }
            }
            else if (start.equals(vpa.getLocation(2))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol));
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.integerSymbol));
                }
            }
            else if (start.equals(vpa.getLocation(3))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol));
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol));
                }
            }
            else if (start.equals(vpa.getLocation(4))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym));
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym));
                }
            }
            else if (start.equals(vpa.getLocation(5))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol));
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol));
                }
            }
            else if (start.equals(vpa.getLocation(6))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
                }
            }
            else if (start.equals(vpa.getLocation(7))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol));
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.stringSymbol));
                }
            }
            else if (start.equals(vpa.getLocation(8))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol));
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol));
                }
            }
            else if (start.equals(vpa.getLocation(9))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, k2Sym));
                }
                else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, k2Sym));
                }
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
    }
}
