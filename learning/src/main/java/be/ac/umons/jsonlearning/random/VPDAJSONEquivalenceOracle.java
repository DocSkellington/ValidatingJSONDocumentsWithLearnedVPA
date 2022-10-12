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

package be.ac.umons.jsonlearning.random;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONObject;

import be.ac.umons.jsonlearning.IVPDAJSONEquivalenceOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.graph.OnAcceptingPathRelation;
import be.ac.umons.jsonvalidation.graph.ReachabilityRelation;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;

/**
 * Specialization of {@link AbstractJSONEquivalenceOracle} for VPDAs.
 * 
 * <p>
 * When performing an equivalence check, the following tests are performed, in
 * this order:
 * <ol>
 * <li>Is there a loop over the initial location reading an internal symbol? See
 * {@link IVPDAJSONEquivalenceOracle#counterexampleByLoopingOverInitial(OneSEVPA, Random)}</li>
 * <li>Is there a valid document that is rejected by the hypothesis?</li>
 * <li>Is there an invalid document that is accepted by the hypothesis?</li>
 * <li>Is there a gibberish word that is accepted by the hypothesis?</li>
 * <li>Is the key graph valid? Note that this implementation reuses previously
 * computed relations.</li>
 * </ol>
 * </p>
 * 
 * @author Gaëtan Staquet
 */
public class VPDAJSONEquivalenceOracle extends AbstractJSONEquivalenceOracle<OneSEVPA<?, JSONSymbol>>
        implements IVPDAJSONEquivalenceOracle {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(VPDAJSONEquivalenceOracle.class);

    private DefaultOneSEVPA<JSONSymbol> previousHypothesis = null;
    private ReachabilityRelation<Location> previousReachabilityRelation = null;
    private OnAcceptingPathRelation<Location> previousOnAcceptingPathRelation = null;

    public VPDAJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet, Collection<JSONObject> documentsToTest) {
        super(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys,
                alphabet, documentsToTest);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(OneSEVPA<?, JSONSymbol> hypo,
            Collection<? extends JSONSymbol> inputs) {
        DefaultQuery<JSONSymbol, Boolean> query = counterexampleByLoopingOverInitial(hypo, getRandom());
        if (query != null) {
            return query;
        }

        query = super.findCounterExample(hypo);
        if (query != null) {
            return query;
        }

        DefaultOneSEVPA<JSONSymbol> hypothesis = convertHypothesis(hypo);
        return findCounterExampleFromKeyGraph(hypothesis);
    }

    private DefaultQuery<JSONSymbol, Boolean> findCounterExampleFromKeyGraph(
            DefaultOneSEVPA<JSONSymbol> currentHypothesis) {
        LOGGER.info("Creating graph");
        final CounterexampleWithRelations<Location> queryAndRelations;
        if (previousHypothesis == null) {
            queryAndRelations = counterexampleAndRelationFromKeyGraph(currentHypothesis);
        } else {
            queryAndRelations = counterexampleAndRelationFromKeyGraph(previousHypothesis, previousReachabilityRelation,
                    previousOnAcceptingPathRelation, currentHypothesis);
        }

        if (queryAndRelations == null) {
            return null;
        }

        this.previousReachabilityRelation = queryAndRelations.reachabilityRelation;
        this.previousOnAcceptingPathRelation = queryAndRelations.onAcceptingPathRelation;
        this.previousHypothesis = currentHypothesis;
        return queryAndRelations.counterexample;
    }

    private <L> DefaultOneSEVPA<JSONSymbol> convertHypothesis(OneSEVPA<L, JSONSymbol> original) {
        LOGGER.info("Converting hypothesis");
        final Alphabet<JSONSymbol> internAlphabet = original.getInputAlphabet().getInternalAlphabet();
        final Alphabet<JSONSymbol> callAlphabet = original.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = original.getInputAlphabet().getReturnAlphabet();

        final DefaultOneSEVPA<JSONSymbol> converted = new DefaultOneSEVPA<>(original.getInputAlphabet(),
                original.size());

        final Map<L, Location> originalToConvertedLocations = new LinkedHashMap<>();
        for (final L location : original.getLocations()) {
            final boolean accepting = original.isAcceptingLocation(location);
            final Location convertedLocation;
            if (location == original.getInitialLocation()) {
                convertedLocation = converted.addInitialLocation(accepting);
            } else {
                convertedLocation = converted.addLocation(accepting);
            }
            originalToConvertedLocations.put(location, convertedLocation);
        }

        for (final L originalLocation : original.getLocations()) {
            final Location convertedLocation = originalToConvertedLocations.get(originalLocation);
            for (final JSONSymbol internalSym : internAlphabet) {
                final L originalTarget = original.getInternalSuccessor(originalLocation, internalSym);
                final Location convertedTarget = originalToConvertedLocations.get(originalTarget);
                converted.setInternalSuccessor(convertedLocation, internalSym, convertedTarget);
            }

            for (final JSONSymbol callSym : callAlphabet) {
                for (final JSONSymbol returnSym : returnAlphabet) {
                    for (final L originalBeforeCall : original.getLocations()) {
                        final Location convertedBeforeCall = originalToConvertedLocations.get(originalBeforeCall);

                        final int originalStackSym = original.encodeStackSym(originalBeforeCall, callSym);
                        final int convertedStackSym = converted.encodeStackSym(convertedBeforeCall, callSym);

                        final L originalTarget = original.getReturnSuccessor(originalLocation, returnSym,
                                originalStackSym);
                        final Location convertedTarget = originalToConvertedLocations.get(originalTarget);

                        converted.setReturnSuccessor(convertedLocation, returnSym, convertedStackSym, convertedTarget);
                    }
                }
            }
        }

        return converted;
    }

}
