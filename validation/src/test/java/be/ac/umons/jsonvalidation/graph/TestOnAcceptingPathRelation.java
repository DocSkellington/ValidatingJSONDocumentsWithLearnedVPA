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

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.jsonvalidation.Automata;
import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.Word;

/**
 * @author Gaëtan Staquet
 */
public class TestOnAcceptingPathRelation {
    @Test
    public void testOnAcceptingRelationRelationSmallTwoBranchesAutomaton() {
        final DefaultOneSEVPA<JSONSymbol> vpa = Automata.constructSmallTwoBranchesAutomaton();

        final ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(vpa, true);

        final OnAcceptingPathRelation<Location> onAcceptingRelation = OnAcceptingPathRelation.computeRelation(vpa,
                reachabilityRelation, true);

        final JSONSymbol k1Sym = JSONSymbol.toSymbol("k1");
        final JSONSymbol k2Sym = JSONSymbol.toSymbol("k2");

        Assert.assertEquals(onAcceptingRelation.size(), vpa.size());

        for (OnAcceptingPath<Location> inRelation : onAcceptingRelation) {
            Location start = vpa.getInitialLocation();
            Location target = inRelation.getIntermediate();
            Word<JSONSymbol> witnessToStart = inRelation.getWitnessToIntermediate();
            Word<JSONSymbol> witnessFromTarget = inRelation.getWitnessFromIntermediate();

            if (start.equals(vpa.getLocation(0))) {
                if (target.equals(vpa.getLocation(0)) || target.equals(vpa.getLocation(6))) {
                    Assert.assertEquals(witnessToStart, Word.epsilon());
                } else {
                    Assert.assertEquals(witnessToStart, Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol));
                }
            } else {
                Assert.fail();
            }

            if (target.equals(vpa.getLocation(0))) {
                Assert.assertEquals(witnessFromTarget,
                        Word.fromSymbols(JSONSymbol.openingCurlyBraceSymbol, k1Sym, JSONSymbol.integerSymbol,
                                JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol,
                                JSONSymbol.closingCurlyBraceSymbol));
            } else if (target.equals(vpa.getLocation(1))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.integerSymbol,
                        JSONSymbol.commaSymbol, k2Sym, JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
            } else if (target.equals(vpa.getLocation(2))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym,
                        JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
            } else if (target.equals(vpa.getLocation(3))) {
                Assert.assertEquals(witnessFromTarget,
                        Word.fromSymbols(k2Sym, JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
            } else if (target.equals(vpa.getLocation(4))) {
                Assert.assertEquals(witnessFromTarget,
                        Word.fromSymbols(JSONSymbol.trueSymbol, JSONSymbol.closingCurlyBraceSymbol));
            } else if (target.equals(vpa.getLocation(5))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.closingCurlyBraceSymbol));
            } else if (target.equals(vpa.getLocation(6))) {
                Assert.assertEquals(witnessFromTarget, Word.epsilon());
            } else if (target.equals(vpa.getLocation(7))) {
                Assert.assertEquals(witnessFromTarget, Word.fromSymbols(JSONSymbol.commaSymbol, k2Sym,
                        JSONSymbol.stringSymbol, JSONSymbol.closingCurlyBraceSymbol));
            } else if (target.equals(vpa.getLocation(8))) {
                Assert.assertEquals(witnessFromTarget,
                        Word.fromSymbols(k2Sym, JSONSymbol.stringSymbol, JSONSymbol.closingCurlyBraceSymbol));
            } else if (target.equals(vpa.getLocation(9))) {
                Assert.assertEquals(witnessFromTarget,
                        Word.fromSymbols(JSONSymbol.stringSymbol, JSONSymbol.closingCurlyBraceSymbol));
            }
        }

        Assert.assertNull(onAcceptingRelation.identifyBinLocation(vpa));
    }

    @Test
    public void testDetectBinLocation() {
        final DefaultOneSEVPA<JSONSymbol> vpa = Automata.constructAutomatonWithOptionalKeysAndExplicitBinState();
        final ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(vpa, true);
        final OnAcceptingPathRelation<Location> onAcceptingRelation = OnAcceptingPathRelation.computeRelation(vpa,
                reachabilityRelation, true);

        Assert.assertEquals(onAcceptingRelation.size(), 12);
        Assert.assertEquals(onAcceptingRelation.identifyBinLocation(vpa), vpa.getLocation(12));
    }
}
