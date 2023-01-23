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

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.logging.LearnLogger;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.automata.vpda.State;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * A relation storing the fact that there is path from a location to another
 * location such that the stack contents is the same at the beginning and at the
 * end.
 * 
 * @param <L> Location type
 * @author Gaëtan Staquet
 */
public class ReachabilityRelation<L> extends ReachabilityMatrix<L, InReachabilityRelation<L>> {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(ReachabilityRelation.class);

    @Nullable
    Word<JSONSymbol> getWitness(final L start, final L target) {
        final InReachabilityRelation<L> inRelation = getCell(start, target);
        if (inRelation == null) {
            return null;
        }
        return inRelation.getWitness();
    }

    boolean add(final L start, final L target, final Word<JSONSymbol> witness) {
        return add(new InReachabilityRelation<>(start, target, witness));
    }

    private boolean add(final InReachabilityRelation<L> infoInRelation) {
        final L start = infoInRelation.getStart();
        final L target = infoInRelation.getTarget();
        if (areInRelation(start, target)) {
            // Nothing to do in this case, as we don't have any new seen locations to add
            return false;
        } else {
            set(start, target, infoInRelation);
            return true;
        }
    }

    boolean addAll(final ReachabilityRelation<L> relation) {
        boolean change = false;
        for (final InReachabilityRelation<L> inRelation : relation) {
            change = this.add(inRelation) || change;
        }
        return change;
    }

    /**
     * Computes a subset of this reachability relation such that the witnesses that
     * go from one location to the other by reading a potential value in a JSON
     * document, i.e., what follows a key symbol.
     * 
     * @param automaton        The automaton
     * @param computeWitnesses Whether to compute the witnesses
     * @return The subset of the relation
     */
    public ReachabilityRelation<L> computePotentialValueReachabilityRelation(final OneSEVPA<L, JSONSymbol> automaton,
            boolean computeWitnesses) {
        LOGGER.info("Value reach: start");
        final Alphabet<JSONSymbol> primitiveValuesAlphabet = JSONSymbol.primitiveValuesAlphabet;
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = automaton.getInputAlphabet().getReturnAlphabet();
        final Alphabet<JSONSymbol> internalAlphabet = automaton.getInputAlphabet().getInternalAlphabet();

        final ReachabilityRelation<L> valueReachabilityRelation = new ReachabilityRelation<>();

        for (final L startLocation : automaton.getLocations()) {
            for (final JSONSymbol primitiveValue : primitiveValuesAlphabet) {
                if (internalAlphabet.contains(primitiveValue)) {
                    final L successor = automaton.getInternalSuccessor(startLocation, primitiveValue);
                    if (successor != null) {
                        final Word<JSONSymbol> witness = constructWitness(primitiveValue, computeWitnesses);
                        valueReachabilityRelation.add(startLocation, successor, witness);
                    }
                }
            }

            for (final JSONSymbol callSymbol : callAlphabet) {
                final L locationAfterCall = automaton.getInitialLocation();
                final int stackSymbol = automaton.encodeStackSym(startLocation, callSymbol);

                for (final InReachabilityRelation<L> inRelation : getLocationsAndInfoInRelationWithStart(
                        locationAfterCall)) {
                    final L locationBeforeReturn = inRelation.getTarget();
                    for (final JSONSymbol returnSymbol : returnAlphabet) {
                        final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSymbol,
                                stackSymbol);
                        if (locationAfterReturn != null) {
                            final Word<JSONSymbol> witness = constructWitness(callSymbol, inRelation.getWitness(),
                                    returnSymbol, computeWitnesses);
                            valueReachabilityRelation.add(startLocation, locationAfterReturn, witness);
                        }
                    }
                }
            }
        }

        LOGGER.info("Value reach: end");
        return valueReachabilityRelation;
    }

    /**
     * Computes the relation for the automaton
     * 
     * @param <L>              Location type
     * @param automaton        The automaton
     * @param computeWitnesses Whether to compute the witnesses
     * @return The relation
     */
    public static <L> ReachabilityRelation<L> computeReachabilityRelation(final OneSEVPA<L, JSONSymbol> automaton,
            final boolean computeWitnesses) {
        LOGGER.info("Reach: start");

        return computeReachabilityRelationLoop(automaton, initializeReachabilityRelation(automaton, computeWitnesses),
                computeWitnesses);
    }

    /**
     * Computes the relation for the automaton, using information computed in a
     * previous relation.
     * 
     * That is, this function reduces the number of iterations of the algorithm by
     * recycling information from a previous relation.
     * 
     * @param <L1>                         Location type of the previous relation
     * @param <L2>                         Location type of the new relation
     * @param previousHypothesis           The previous automaton
     * @param previousReachabilityRelation The previous relation
     * @param currentHypothesis            The current automaton
     * @param computeWitnesses             Whether to compute the witnesses
     * @return The relation
     */
    public static <L1, L2> ReachabilityRelation<L2> computeReachabilityRelation(
            final OneSEVPA<L1, JSONSymbol> previousHypothesis,
            final ReachabilityRelation<L1> previousReachabilityRelation,
            final OneSEVPA<L2, JSONSymbol> currentHypothesis, final boolean computeWitnesses) {
        LOGGER.info("Reach: start");
        LOGGER.info("Size of hypothesis " + currentHypothesis.size());
        final ReachabilityRelation<L2> reachabilityRelation = initializeReachabilityRelation(currentHypothesis,
                computeWitnesses);

        LOGGER.info("Number of elements in reach before adding still valid: " + reachabilityRelation.size());
        final Map<L1, L2> locationsPreviousToCurrent = Utils.createMapLocationsOfPreviousToCurrent(previousHypothesis,
                currentHypothesis);

        for (final InReachabilityRelation<L1> inPreviousRelation : previousReachabilityRelation) {
            final L2 startLocation = locationsPreviousToCurrent.get(inPreviousRelation.getStart());

            final State<L2> startState = new State<L2>(startLocation, null);
            final State<L2> targetState = currentHypothesis.getSuccessor(startState, inPreviousRelation.getWitness());
            reachabilityRelation.add(startLocation, targetState.getLocation(), inPreviousRelation.getWitness());
        }
        LOGGER.info("Number of elements in reach after adding still valid: " + reachabilityRelation.size());

        return computeReachabilityRelation(currentHypothesis, computeWitnesses);
    }

    private static Word<JSONSymbol> constructWitness(final boolean computeWitnesses) {
        if (computeWitnesses) {
            return Word.epsilon();
        } else {
            return null;
        }
    }

    private static Word<JSONSymbol> constructWitness(final JSONSymbol symbol, final boolean computeWitnesses) {
        if (computeWitnesses) {
            return Word.fromLetter(symbol);
        } else {
            return null;
        }
    }

    private static Word<JSONSymbol> constructWitness(final JSONSymbol callSymbol, final Word<JSONSymbol> witness,
            final JSONSymbol returnSymbol, final boolean computeWitnesses) {
        if (computeWitnesses) {
            final WordBuilder<JSONSymbol> builder = new WordBuilder<>(witness.length() + 2);
            builder.add(callSymbol);
            builder.append(witness);
            builder.add(returnSymbol);
            return builder.toWord();
        } else {
            return null;
        }
    }

    private static <L> ReachabilityRelation<L> getIdentityRelation(final OneSEVPA<L, JSONSymbol> automaton,
            final boolean computeWitnesses) {
        final ReachabilityRelation<L> relation = new ReachabilityRelation<>();
        for (final L loc : automaton.getLocations()) {
            relation.add(loc, loc, constructWitness(computeWitnesses));
        }
        return relation;
    }

    private static <L> ReachabilityRelation<L> initializeReachabilityRelation(final OneSEVPA<L, JSONSymbol> automaton,
            final boolean computeWitnesses) {
        final Alphabet<JSONSymbol> internalAlphabet = automaton.getInputAlphabet().getInternalAlphabet();
        final List<L> locations = automaton.getLocations();

        final ReachabilityRelation<L> reachabilityRelation = getIdentityRelation(automaton, computeWitnesses);
        for (final L start : locations) {
            for (final JSONSymbol internalSym : internalAlphabet) {
                final L target = automaton.getInternalSuccessor(start, internalSym);
                if (target != null) {
                    reachabilityRelation.add(start, target, constructWitness(internalSym, computeWitnesses));
                }
            }
        }

        return reachabilityRelation;
    }

    private static <L> ReachabilityRelation<L> computeReachabilityRelationLoop(final OneSEVPA<L, JSONSymbol> automaton,
            final ReachabilityRelation<L> reachabilityRelation, final boolean computeWitnesses) {
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final List<L> locations = automaton.getLocations();

        LOGGER.info("Reach: init warshall");
        Utils.warshall(reachabilityRelation, locations, computeWitnesses);
        LOGGER.info("Reach: init done");

        boolean change = true;
        while (change) {
            LOGGER.info("Reach: increasing depth");
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return new ReachabilityRelation<>();
            }

            final ReachabilityRelation<L> newLocationsInRelation = new ReachabilityRelation<>();

            for (final L locationBeforeCall : locations) {
                for (final JSONSymbol callSym : callAlphabet) {
                    final L locationAfterCall = automaton.getInitialLocation();
                    final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSym);

                    final JSONSymbol returnSym = callSym.callToReturn();

                    for (final InReachabilityRelation<L> inRelation : reachabilityRelation
                            .getLocationsAndInfoInRelationWithStart(locationAfterCall)) {
                        final L locationBeforeReturn = inRelation.getTarget();
                        final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSym,
                                stackSym);
                        if (locationAfterReturn != null) {
                            final Word<JSONSymbol> witnessAfterToBefore = inRelation.getWitness();
                            final Word<JSONSymbol> witnessStartToTarget;

                            if (reachabilityRelation.areInRelation(locationBeforeCall, locationAfterReturn)) {
                                witnessStartToTarget = reachabilityRelation.getWitness(locationBeforeCall,
                                        locationAfterReturn);
                            } else if (newLocationsInRelation.areInRelation(locationBeforeCall, locationAfterReturn)) {
                                witnessStartToTarget = newLocationsInRelation.getWitness(locationBeforeCall,
                                        locationAfterReturn);
                            } else {
                                witnessStartToTarget = constructWitness(callSym, witnessAfterToBefore, returnSym,
                                        computeWitnesses);
                            }

                            newLocationsInRelation.add(locationBeforeCall, locationAfterReturn, witnessStartToTarget);
                        }
                    }
                }
            }
            change = reachabilityRelation.addAll(newLocationsInRelation);
            LOGGER.info("Reach: warshall loop");
            change = Utils.warshall(reachabilityRelation, locations, computeWitnesses) || change;
            LOGGER.info("Reach: loop done");
        }

        LOGGER.info("Size " + reachabilityRelation.size());
        return reachabilityRelation;
    }
}
