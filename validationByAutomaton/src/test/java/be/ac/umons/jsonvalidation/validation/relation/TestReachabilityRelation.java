package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.validation.Automata;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

public class TestReachabilityRelation {
    @Test
    public void testCompose() {
        JSONSymbol symbol = JSONSymbol.toSymbol("test");
        VPDAlphabet<JSONSymbol> alphabet = Automata.constructAlphabet(symbol);
        Location q0 = new Location(alphabet, 0, false);
        Location q1 = new Location(alphabet, 1, false);
        Location q2 = new Location(alphabet, 2, false);
        Location q3 = new Location(alphabet, 3, false);

        ReachabilityRelation<Location> rel1 = new ReachabilityRelation<>();
        ReachabilityRelation<Location> rel2 = new ReachabilityRelation<>();

        rel1.add(InRelation.of(q0, q1, Word.fromLetter(symbol)));
        rel1.add(InRelation.of(q1, q2, Word.fromLetter(symbol)));

        rel2.add(InRelation.of(q1, q3, Word.fromLetter(symbol)));
        rel2.add(InRelation.of(q2, q0, Word.fromLetter(symbol)));

        ReachabilityRelation<Location> result = rel1.compose(rel2, false);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.areInRelation(q0, q3));
        Assert.assertTrue(result.areInRelation(q1, q0));

        result = rel2.compose(rel1, false);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.areInRelation(q2, q1));
    }

    @Test
    public void testSmallTwoBranchesAutomatonCommaRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
        ReachabilityRelation<Location> commaRelation = ReachabilityRelation.computeCommaRelation(automaton, false);
        Assert.assertEquals(commaRelation.size(), 2);
        Assert.assertTrue(commaRelation.areInRelation(automaton.getLocation(2), automaton.getLocation(3)));
        Assert.assertTrue(commaRelation.areInRelation(automaton.getLocation(7), automaton.getLocation(8)));
    }

    @Test
    public void testSmallTwoBranchesAutomatonInternalRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
        ReachabilityRelation<Location> internalRelation = ReachabilityRelation.computeInternalRelation(automaton, false);
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
        ReachabilityRelation<Location> commaRelation = ReachabilityRelation.computeCommaRelation(automaton, false);
        ReachabilityRelation<Location> internalRelation = ReachabilityRelation.computeInternalRelation(automaton, false);
        ReachabilityRelation<Location> wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation, false);

        Assert.assertEquals(wellMatchedRelation.size(), 1);
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(0), automaton.getLocation(6)));

        Assert.assertTrue(wellMatchedRelation.identifyBinLocations(automaton).isEmpty());
    }

    @Test
    public void testAutomatonWithOptionalKeysWellMatchedRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeys();

        ReachabilityRelation<Location> commaRelation = ReachabilityRelation.computeCommaRelation(automaton, false);
        ReachabilityRelation<Location> internalRelation = ReachabilityRelation.computeInternalRelation(automaton, false);
        ReachabilityRelation<Location> wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation, false);

        Assert.assertEquals(wellMatchedRelation.size(), 2);
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(0), automaton.getLocation(11)));
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(4), automaton.getLocation(7)));

        Assert.assertTrue(wellMatchedRelation.identifyBinLocations(automaton).isEmpty());
    }

    @Test
    public void testAutomatonWithOptionalKeysAndExplicitBinStateWellMatchedRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeysAndExplicitBinState();

        ReachabilityRelation<Location> commaRelation = ReachabilityRelation.computeCommaRelation(automaton, false);
        ReachabilityRelation<Location> internalRelation = ReachabilityRelation.computeInternalRelation(automaton, false);
        ReachabilityRelation<Location> wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation, false);

        Assert.assertEquals(wellMatchedRelation.size(), 15);
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(0), automaton.getLocation(11)));
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(4), automaton.getLocation(7)));
        for (Location source : automaton.getLocations()) {
            Assert.assertTrue(wellMatchedRelation.areInRelation(source, automaton.getLocation(12)));
        }

        Set<Location> binLocations = wellMatchedRelation.identifyBinLocations(automaton);
        Assert.assertEquals(1, binLocations.size());
        Assert.assertTrue(binLocations.contains(automaton.getLocation(12)));
    }

    @Test
    public void testAutomatonWithTwoKeysOnSameTransitionInternalRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithTwoKeysOnSameTransition();

        ReachabilityRelation<Location> internalRelation = ReachabilityRelation.computeInternalRelation(automaton, false);
        Assert.assertEquals(internalRelation.size(), 3);
        Assert.assertTrue(internalRelation.areInRelation(automaton.getLocation(0), automaton.getLocation(1)));
        Assert.assertTrue(internalRelation.areInRelation(automaton.getLocation(1), automaton.getLocation(2)));
        Assert.assertTrue(internalRelation.areInRelation(automaton.getLocation(3), automaton.getLocation(4)));
    }

    @Test
    public void testAutomatonWithTwoKeysOnSameTransitionWellMatchedRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithTwoKeysOnSameTransition();

        ReachabilityRelation<Location> commaRelation = ReachabilityRelation.computeCommaRelation(automaton, false);
        ReachabilityRelation<Location> internalRelation = ReachabilityRelation.computeInternalRelation(automaton, false);
        ReachabilityRelation<Location> wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation, false);

        Assert.assertEquals(wellMatchedRelation.size(), 2);
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(0), automaton.getLocation(6)));
        Assert.assertTrue(wellMatchedRelation.areInRelation(automaton.getLocation(4), automaton.getLocation(5)));

        Assert.assertTrue(wellMatchedRelation.identifyBinLocations(automaton).isEmpty());
    }

    @Test
    public void testSmallTwoBranchesAutomatonCloseRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();

        ReachabilityRelation<Location> commaRelation = ReachabilityRelation.computeCommaRelation(automaton, true);
        ReachabilityRelation<Location> internalRelation = ReachabilityRelation.computeInternalRelation(automaton, true);
        ReachabilityRelation<Location> wellMatchedRelation = ReachabilityRelation.computeWellMatchedRelation(automaton,
                commaRelation, internalRelation, true);

        ReachabilityRelation<Location> closedRelation = ReachabilityRelation.closeRelations(commaRelation, internalRelation, wellMatchedRelation, true);

        final JSONSymbol k1Sym = JSONSymbol.toSymbol("k1");
        final JSONSymbol k2Sym = JSONSymbol.toSymbol("k2");

        Assert.assertEquals(closedRelation.size(), 28);
        for (InRelation<Location> inRelation : closedRelation) {
            Location start = inRelation.getStart();
            Location target = inRelation.getTarget();
            Word<JSONSymbol> witness = inRelation.getWitness();

            if (start.equals(automaton.getLocation(0))) {
                if (target.equals(automaton.getLocation(1))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym));
                }
                else if (target.equals(automaton.getLocation(2))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol));
                }
                else if (target.equals(automaton.getLocation(3))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol));
                }
                else if (target.equals(automaton.getLocation(4))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym));
                }
                else if (target.equals(automaton.getLocation(5))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym, JSONSymbol.falseSymbol));
                }
                else if (target.equals(automaton.getLocation(6))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym, JSONSymbol.falseSymbol, JSONSymbol.closingCurlyBraceSymbol));
                }
                else if (target.equals(automaton.getLocation(7))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol));
                }
                else if (target.equals(automaton.getLocation(8))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol));
                }
                else if (target.equals(automaton.getLocation(9))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, k2Sym));
                }
            }
            else if (start.equals(automaton.getLocation(1))) {
                if (target.equals(automaton.getLocation(2))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.integerSymbol));
                }
                else if (target.equals(automaton.getLocation(3))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol));
                }
                else if (target.equals(automaton.getLocation(4))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym));
                }
                else if (target.equals(automaton.getLocation(5))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym, JSONSymbol.falseSymbol));
                }
                else if (target.equals(automaton.getLocation(7))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.stringSymbol));
                }
                else if (target.equals(automaton.getLocation(8))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.commaSymbol));
                }
                else if (target.equals(automaton.getLocation(9))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, k2Sym));
                }
            }
            else if (start.equals(automaton.getLocation(2))) {
                if (target.equals(automaton.getLocation(3))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol));
                }
                else if (target.equals(automaton.getLocation(4))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym));
                }
                else if (target.equals(automaton.getLocation(5))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym, JSONSymbol.falseSymbol));
                }
            }
            else if (start.equals(automaton.getLocation(3))) {
                if (target.equals(automaton.getLocation(4))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k2Sym));
                }
                else if (target.equals(automaton.getLocation(5))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k2Sym, JSONSymbol.falseSymbol));
                }
            }
            else if (start.equals(automaton.getLocation(4))) {
                if (target.equals(automaton.getLocation(5))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.falseSymbol));
                }
            }
            else if (start.equals(automaton.getLocation(7))) {
                if (target.equals(automaton.getLocation(5))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym, JSONSymbol.stringSymbol));
                }
                else if (target.equals(automaton.getLocation(8))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol));
                }
                else if (target.equals(automaton.getLocation(9))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym));
                }
            }
            else if (start.equals(automaton.getLocation(8))) {
                if (target.equals(automaton.getLocation(5))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k2Sym, JSONSymbol.stringSymbol));
                }
                else if (target.equals(automaton.getLocation(9))) {
                    Assert.assertEquals(witness, Word.fromSymbols(k2Sym));
                }
            }
            else if (start.equals(automaton.getLocation(9))) {
                if (target.equals(automaton.getLocation(5))) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.stringSymbol));
                }
            }
        }
    }
}
