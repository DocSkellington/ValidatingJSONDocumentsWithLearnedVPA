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

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.logging.LearnLogger;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.automata.vpda.State;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Stores the locations that are on an accepting location.
 * 
 * A location is on an accepting location is it is on path from the initial
 * configuration to an accepting configuration.
 * 
 * In the paper, this relation is called Z_A.
 * 
 * @param <L> Location type
 * @author Gaëtan Staquet
 */
public class OnAcceptingPathRelation<L> extends ReachabilityMatrix<L, OnAcceptingPath<L>> {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(OnAcceptingPathRelation.class);

    private final L initialLocation;

    private OnAcceptingPathRelation(final L initialLocation) {
        this.initialLocation = initialLocation;
    }

    private OnAcceptingPath<L> getCell(final L target) {
        return getCell(initialLocation, target);
    }

    /**
     * Gets the witness from the initial location to the provided intermediate
     * location, if it belongs to the relation.
     * 
     * @param intermediate The intermediate location
     * @return The witness, if it exists
     */
    @Nullable
    public Word<JSONSymbol> getWitnessToIntermediate(final L intermediate) {
        final OnAcceptingPath<L> cell = getCell(intermediate);
        if (cell == null) {
            return null;
        } else {
            return cell.getWitnessToIntermediate();
        }
    }

    /**
     * Gets the witness from the provided intermediate location to an accepting
     * location, if it belongs to the relation.
     * 
     * @param intermediate The intermediate location
     * @return The witness, if it exists
     */
    @Nullable
    public Word<JSONSymbol> getWitnessFromIntermediate(final L intermediate) {
        final OnAcceptingPath<L> cell = getCell(intermediate);
        if (cell == null) {
            return null;
        } else {
            return cell.getWitnessFromIntermediate();
        }
    }

    /**
     * Identifies the bin location in the automaton
     * 
     * @param automaton The automaton
     * @return The bin location
     */
    public L identifyBinLocation(final OneSEVPA<L, JSONSymbol> automaton) {
        for (final L location : automaton.getLocations()) {
            if (!areInRelation(automaton.getInitialLocation(), location)) {
                return location;
            }
        }
        return null;
    }

    private boolean add(final L initialLocation, final L target, final Word<JSONSymbol> witnessToStart,
            final Word<JSONSymbol> witnessFromTarget) {
        return add(new OnAcceptingPath<>(target, witnessToStart, witnessFromTarget), initialLocation);
    }

    private boolean add(final OnAcceptingPath<L> infoInRelation, final L initialLocation) {
        if (areInRelation(initialLocation, infoInRelation.getIntermediate())) {
            // If start and target are already in relation, we have nothing to do, as we
            // already have witnesses.
            return false;
        } else {
            set(initialLocation, infoInRelation.getIntermediate(), infoInRelation);
            return true;
        }
    }

    private boolean addAll(final OnAcceptingPathRelation<L> relation, final L initialLocation) {
        boolean change = false;
        for (OnAcceptingPath<L> inRelation : relation) {
            change = this.add(inRelation, initialLocation) || change;
        }
        return change;
    }

    /**
     * Computes the relation, using the previously computed relation.
     * 
     * That is, this function reduces the number of iterations of the algorithm by
     * recycling information from a previous relation.
     * 
     * @param <L1>                 Location type of the previous relation
     * @param <L2>                 Location type of the current relation
     * @param previousHypothesis   The previous hypothesis
     * @param previousRelation     The previous relation
     * @param currentHypothesis    The current hypothesis
     * @param reachabilityRelation The reachability relation
     * @param computeWitnesses     Whether to compute the witnesses
     * @return The relation
     */
    public static <L1, L2> OnAcceptingPathRelation<L2> computeRelation(
            final OneSEVPA<L1, JSONSymbol> previousHypothesis, final OnAcceptingPathRelation<L1> previousRelation,
            final OneSEVPA<L2, JSONSymbol> currentHypothesis, final ReachabilityRelation<L2> reachabilityRelation,
            final boolean computeWitnesses) {
        LOGGER.info("Z_A relation: start");
        final OnAcceptingPathRelation<L2> onAcceptingRelation = initializeRelation(currentHypothesis,
                reachabilityRelation,
                computeWitnesses);
        final Map<L1, L2> locationsPreviousToCurrent = Utils.createMapLocationsOfPreviousToCurrent(previousHypothesis,
                currentHypothesis);

        LOGGER.info("Number of elements in Rel before adding still valid: " + onAcceptingRelation.size());
        for (final OnAcceptingPath<L1> inPreviousRelation : previousRelation) {
            final L2 intermediateLocation = locationsPreviousToCurrent.get(inPreviousRelation.getIntermediate());

            final State<L2> toIntermediateState = currentHypothesis.getSuccessor(
                    new State<>(currentHypothesis.getInitialLocation(), null),
                    inPreviousRelation.getWitnessToIntermediate());
            if (toIntermediateState == null) {
                continue;
            }

            final State<L2> fromIntermediateState = currentHypothesis.getSuccessor(
                    new State<L2>(intermediateLocation, toIntermediateState.getStackContents()),
                    inPreviousRelation.getWitnessFromIntermediate());
            if (fromIntermediateState != null && currentHypothesis.isAccepting(fromIntermediateState)) {
                onAcceptingRelation.add(currentHypothesis.getInitialLocation(), intermediateLocation,
                        inPreviousRelation.getWitnessToIntermediate(), inPreviousRelation.getWitnessFromIntermediate());
            }
        }
        LOGGER.info("Number of elements in Rel after adding still valid: " + onAcceptingRelation.size());

        return computeRelationLoop(currentHypothesis, reachabilityRelation, onAcceptingRelation, computeWitnesses);
    }

    /**
     * Computes the relation from the provided automaton, and its reachability
     * relation.
     * 
     * @param <L>                  Location type
     * @param automaton            The automaton
     * @param reachabilityRelation The reachability relation
     * @param computeWitnesses     Whether to compute the witnesses
     * @return The relation
     */
    public static <L> OnAcceptingPathRelation<L> computeRelation(final OneSEVPA<L, JSONSymbol> automaton,
            final ReachabilityRelation<L> reachabilityRelation, final boolean computeWitnesses) {
        LOGGER.info("Z_A relation: start");
        final OnAcceptingPathRelation<L> onAcceptingRelation = initializeRelation(automaton, reachabilityRelation,
                computeWitnesses);
        return computeRelationLoop(automaton, reachabilityRelation, onAcceptingRelation, computeWitnesses);
    }

    private static <L> OnAcceptingPathRelation<L> initializeRelation(final OneSEVPA<L, JSONSymbol> automaton,
            final ReachabilityRelation<L> reachabilityRelation, final boolean computeWitnesses) {
        final OnAcceptingPathRelation<L> onAcceptingRelation = new OnAcceptingPathRelation<>(
                automaton.getInitialLocation());
        final L initialLocation = automaton.getInitialLocation();
        for (final L location : automaton.getLocations()) {
            if (automaton.isAcceptingLocation(location)) {
                final Word<JSONSymbol> witness;
                if (computeWitnesses) {
                    witness = Word.epsilon();
                } else {
                    witness = null;
                }
                onAcceptingRelation.add(initialLocation, location, witness, witness);
            }
        }
        LOGGER.info("Z_A relation: init done");
        return onAcceptingRelation;
    }

    private static <L> OnAcceptingPathRelation<L> computeRelationLoop(final OneSEVPA<L, JSONSymbol> automaton,
            final ReachabilityRelation<L> reachabilityRelation, final OnAcceptingPathRelation<L> onAcceptingRelation,
            final boolean computeWitnesses) {
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final L initialLocation = automaton.getInitialLocation();

        while (true) {
            final OnAcceptingPathRelation<L> newInRelation = new OnAcceptingPathRelation<>(
                    automaton.getInitialLocation());
            for (final OnAcceptingPath<L> onAcceptingPath : onAcceptingRelation) {
                for (final InReachabilityRelation<L> inReachabilityRelation : reachabilityRelation) {
                    if (inReachabilityRelation.getTarget() == onAcceptingPath.getIntermediate()) {
                        final Word<JSONSymbol> witnessToIntermediate, witnessFromIntermediate;

                        if (computeWitnesses) {
                            witnessToIntermediate = onAcceptingPath.getWitnessToIntermediate();
                            witnessFromIntermediate = inReachabilityRelation.getWitness()
                                    .concat(onAcceptingPath.getWitnessFromIntermediate());
                        } else {
                            witnessToIntermediate = witnessFromIntermediate = null;
                        }

                        newInRelation.add(initialLocation, inReachabilityRelation.getStart(), witnessToIntermediate,
                                witnessFromIntermediate);
                    }
                }

                for (final InReachabilityRelation<L> inRelationWithInitial : reachabilityRelation
                        .getLocationsAndInfoInRelationWithStart(initialLocation)) {
                    final L locationBeforeCall = inRelationWithInitial.getTarget();
                    for (final JSONSymbol callSymbol : callAlphabet) {
                        final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSymbol);

                        final JSONSymbol returnSymbol = callSymbol.callToReturn();

                        final Word<JSONSymbol> witnessToIntermediate;
                        if (computeWitnesses) {
                            witnessToIntermediate = onAcceptingPath.getWitnessToIntermediate()
                                    .concat(inRelationWithInitial.getWitness()).append(callSymbol);
                        } else {
                            witnessToIntermediate = null;
                        }

                        for (final L locationBeforeReturn : automaton.getLocations()) {
                            final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn,
                                    returnSymbol, stackSym);
                            if (Objects.equals(locationAfterReturn, onAcceptingPath.getIntermediate())) {
                                final Word<JSONSymbol> witnessFromIntermediate;
                                if (computeWitnesses) {
                                    witnessFromIntermediate = onAcceptingPath.getWitnessFromIntermediate()
                                            .prepend(returnSymbol);
                                } else {
                                    witnessFromIntermediate = null;
                                }

                                newInRelation.add(initialLocation, locationBeforeReturn, witnessToIntermediate,
                                        witnessFromIntermediate);
                            }
                        }
                    }
                }
            }

            if (!onAcceptingRelation.addAll(newInRelation, initialLocation)) {
                break;
            }
            LOGGER.info("Z_A relation: end loop");
        }

        LOGGER.info("Size: " + onAcceptingRelation.size());
        return onAcceptingRelation;
    }
}