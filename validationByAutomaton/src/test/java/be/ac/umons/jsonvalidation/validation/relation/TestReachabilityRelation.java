package be.ac.umons.jsonvalidation.validation.relation;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.validation.Automata;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.Word;

public class TestReachabilityRelation {
    @Test
    public void testSmallTwoBranchesAutomatonReachabilityRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
        ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(automaton, true);

        final JSONSymbol k1Sym = JSONSymbol.toSymbol("k1");
        final JSONSymbol k2Sym = JSONSymbol.toSymbol("k2");

        Assert.assertEquals(reachabilityRelation.size(), 38);
        for (InRelation<Location> inRelation : reachabilityRelation) {
            Location start = inRelation.getStart();
            Location target = inRelation.getTarget();
            Word<JSONSymbol> witness = inRelation.getWitness();

            if (start == automaton.getLocation(0)) {
                if (target == automaton.getLocation(0)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else if (target == automaton.getLocation(1)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym));
                } else if (target == automaton.getLocation(2)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol));
                } else if (target == automaton.getLocation(3)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym));
                } else if (target == automaton.getLocation(5)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.integerSymbol,
                            JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol));
                } else if (target == automaton.getLocation(6)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol));
                } else if (target == automaton.getLocation(7)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol));
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, k2Sym));
                }
            } else if (start == automaton.getLocation(1)) {
                Assert.assertNotEquals(target, automaton.getLocation(0));
                if (target == automaton.getLocation(1)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else if (target == automaton.getLocation(2)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.integerSymbol));
                } else if (target == automaton.getLocation(3)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, k2Sym));
                } else if (target == automaton.getLocation(5)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol,
                            k2Sym, JSONSymbol.trueSymbol));
                } else if (target == automaton.getLocation(7)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.stringSymbol));
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, k2Sym));
                }
            } else if (start == automaton.getLocation(2)) {
                if (target == automaton.getLocation(2)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else if (target == automaton.getLocation(3)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym));
                } else if (target == automaton.getLocation(5)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(3)) {
                if (target == automaton.getLocation(3)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k2Sym));
                } else if (target == automaton.getLocation(5)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k2Sym, JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(4)) {
                if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else if (target == automaton.getLocation(5)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(5)) {
                if (target == automaton.getLocation(5)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(6)) {
                if (target == automaton.getLocation(6)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(7)) {
                if (target == automaton.getLocation(5)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym, JSONSymbol.stringSymbol));
                } else if (target == automaton.getLocation(7)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(8)) {
                if (target == automaton.getLocation(5)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k2Sym, JSONSymbol.stringSymbol));
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k2Sym));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(9)) {
                if (target == automaton.getLocation(5)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.stringSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else {
                Assert.fail(inRelation.toString());
            }
        }
    }

    @Test
    public void testAutomatonWithOptionalKeysReachabilityRelation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeys();
        ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(automaton, true);

        final JSONSymbol k1Sym = JSONSymbol.toSymbol("k1");
        final JSONSymbol k2Sym = JSONSymbol.toSymbol("k2");
        final JSONSymbol o1Sym = JSONSymbol.toSymbol("o1");
        final JSONSymbol o2Sym = JSONSymbol.toSymbol("o2");

        Assert.assertEquals(reachabilityRelation.size(), 64);

        for (InRelation<Location> inRelation : reachabilityRelation) {
            Location start = inRelation.getStart();
            Location target = inRelation.getTarget();
            Word<JSONSymbol> witness = inRelation.getWitness();

            if (start == automaton.getLocation(0)) {
                if (target == automaton.getLocation(0)) {
                    Assert.assertEquals(witness, Word.epsilon());
                } else if (target == automaton.getLocation(1)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym));
                } else if (target == automaton.getLocation(2)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol));
                } else if (target == automaton.getLocation(3)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, o1Sym));
                } else if (target == automaton.getLocation(5)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k2Sym));
                } else if (target == automaton.getLocation(6)) {
                    Assert.assertEquals(witness, Word.fromSymbols(k2Sym, JSONSymbol.integerSymbol));
                } else if (target == automaton.getLocation(7)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, o1Sym,
                                    JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol));
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, o1Sym,
                                    JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, o1Sym,
                                    JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol, o2Sym));
                } else if (target == automaton.getLocation(10)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(k1Sym, JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, o1Sym,
                                    JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol, o2Sym,
                                    JSONSymbol.trueSymbol));
                } else if (target == automaton.getLocation(11)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym,
                            JSONSymbol.stringSymbol, JSONSymbol.closingCurlyBraceSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(1)) {
                if (target == automaton.getLocation(1)) {
                    Word.epsilon();
                } else if (target == automaton.getLocation(2)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.stringSymbol));
                } else if (target == automaton.getLocation(3)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, o1Sym));
                } else if (target == automaton.getLocation(7)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, o1Sym,
                                    JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol));
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, o1Sym,
                                    JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.commaSymbol, o1Sym,
                                    JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol, o2Sym));
                } else if (target == automaton.getLocation(10)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.commaSymbol,
                            o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                            JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol, o2Sym, JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(2)) {
                if (target == automaton.getLocation(2)) {
                    Word.epsilon();
                } else if (target == automaton.getLocation(3)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol, o1Sym));
                } else if (target == automaton.getLocation(7)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                                    JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol));
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                                    JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol,
                                    JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                                    JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol,
                                    JSONSymbol.commaSymbol, o2Sym));
                } else if (target == automaton.getLocation(10)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                                    JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol,
                                    JSONSymbol.commaSymbol, o2Sym, JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(3)) {
                if (target == automaton.getLocation(3)) {
                    Word.epsilon();
                } else if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness, Word.fromSymbols(o1Sym));
                } else if (target == automaton.getLocation(7)) {
                    Assert.assertEquals(witness, Word.fromSymbols(o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                            JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol));
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness, Word.fromSymbols(o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                            JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol, o2Sym));
                } else if (target == automaton.getLocation(10)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol, o2Sym,
                                    JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(4)) {
                if (target == automaton.getLocation(4)) {
                    Word.epsilon();
                } else if (target == automaton.getLocation(7)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                            JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol));
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                            JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol, o2Sym));
                } else if (target == automaton.getLocation(10)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol, o2Sym,
                                    JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(5)) {
                if (target == automaton.getLocation(3)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, o1Sym));
                } else if (target == automaton.getLocation(5)) {
                    Word.epsilon();
                } else if (target == automaton.getLocation(6)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.integerSymbol));
                } else if (target == automaton.getLocation(7)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, o1Sym,
                                    JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol));
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, o1Sym,
                                    JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol, o1Sym,
                                    JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                                    JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol, o2Sym));
                } else if (target == automaton.getLocation(10)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.integerSymbol, JSONSymbol.commaSymbol,
                            o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym, JSONSymbol.integerSymbol,
                            JSONSymbol.closingCurlyBraceSymbol, JSONSymbol.commaSymbol, o2Sym, JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(6)) {
                if (target == automaton.getLocation(3)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(4)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol, o1Sym));
                } else if (target == automaton.getLocation(6)) {
                    Word.epsilon();
                } else if (target == automaton.getLocation(7)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                                    JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol));
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                                    JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol,
                                    JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                                    JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol,
                                    JSONSymbol.commaSymbol, o2Sym));
                } else if (target == automaton.getLocation(10)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, o1Sym, JSONSymbol.openingCurlyBraceSymbol, k2Sym,
                                    JSONSymbol.integerSymbol, JSONSymbol.closingCurlyBraceSymbol,
                                    JSONSymbol.commaSymbol, o2Sym, JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(7)) {
                if (target == automaton.getLocation(7)) {
                    Word.epsilon();
                } else if (target == automaton.getLocation(8)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol));
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.commaSymbol, o2Sym));
                } else if (target == automaton.getLocation(10)) {
                    Assert.assertEquals(witness,
                            Word.fromSymbols(JSONSymbol.commaSymbol, o2Sym, JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(8)) {
                if (target == automaton.getLocation(8)) {
                    Word.epsilon();
                } else if (target == automaton.getLocation(9)) {
                    Assert.assertEquals(witness, Word.fromSymbols(o2Sym));
                } else if (target == automaton.getLocation(10)) {
                    Assert.assertEquals(witness, Word.fromSymbols(o2Sym, JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(9)) {
                if (target == automaton.getLocation(9)) {
                    Word.epsilon();
                } else if (target == automaton.getLocation(10)) {
                    Assert.assertEquals(witness, Word.fromSymbols(JSONSymbol.trueSymbol));
                } else {
                    Assert.fail(inRelation.toString());
                }
            } else if (start == automaton.getLocation(10) && target == automaton.getLocation(10)) {
                Assert.assertEquals(witness, Word.epsilon());
            } else if (start == automaton.getLocation(11) && target == automaton.getLocation(11)) {
                Assert.assertEquals(witness, Word.epsilon());
            } else {
                Assert.fail(inRelation.toString());
            }
        }
    }
}
