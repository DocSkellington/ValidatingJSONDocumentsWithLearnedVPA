package be.ac.umons.jsonvalidation.relation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.logging.LearnLogger;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class ReachabilityRelation<L> extends ReachabilityMatrix<L, InfoInRelation<L>> {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(ReachabilityRelation.class);
    
    @Nullable
    public Word<JSONSymbol> getWitness(final L start, final L target) {
        final InfoInRelation<L> inRelation = getCell(start, target);
        if (inRelation == null) {
            return null;
        }
        return inRelation.getWitness();
    }

    /**
     * Based on this relation, identifies all the bin locations in the automaton.
     * 
     * Using this relation, we can find all locations that are on a path from the
     * initial location to an accepting locations. If one location of the automaton
     * is not among these locations, it is a bin location.
     * 
     * @param automaton The VPA
     * @return A set with the bin locations
     */
    public Set<L> identifyBinLocations(OneSEVPA<L, JSONSymbol> automaton) {
        final Set<L> binLocations = new LinkedHashSet<>(automaton.getLocations());
        binLocations.removeAll(allLocationsOnAcceptingPath(automaton));
        return binLocations;
    }

    private Set<L> allLocationsOnAcceptingPath(OneSEVPA<L, JSONSymbol> automaton) {
        final Set<L> locations = new LinkedHashSet<>();
        // @formatter:off
        getLocationsAndInfoInRelationWith(automaton.getInitialLocation()).stream()
            .filter(info -> automaton.isAcceptingLocation(info.getTarget()))
            .forEach(info -> locations.addAll(info.getLocationsBetweenStartAndTarget()));
        // @formatter:on
        return locations;
    }

    boolean add(final L start, final L target, final Word<JSONSymbol> witness) {
        if (areInRelation(start, target)) {
            // Nothing to do in this case, as we don't have any new seen locations to add
            return false;
        }
        else {
            final Set<L> locationsBetweenStartAndTarget = new LinkedHashSet<>();
            locationsBetweenStartAndTarget.add(start);
            locationsBetweenStartAndTarget.add(target);
            return add(new InfoInRelation<L>(start, target, witness, locationsBetweenStartAndTarget));
        }
    }

    boolean add(final L start, final L target, final Word<JSONSymbol> witness, final Set<L> locationsBetweenStartAndTarget) {
        return add(new InfoInRelation<>(start, target, witness, locationsBetweenStartAndTarget));
    }

    private boolean add(final InfoInRelation<L> infoInRelation) {
        final L start = infoInRelation.getStart();
        final L target = infoInRelation.getTarget();
        if (areInRelation(start, target)) {
            // If start and target are already in relation, we simply add the intermediate locations
            // We keep the previous witness as, by construction, the previous witness is smaller than the new
            return getCell(start, target).addSeenLocations(infoInRelation.getLocationsBetweenStartAndTarget());
        }
        else {
            set(start, target, infoInRelation);
            return true;
        }
    }

    boolean addAll(final ReachabilityRelation<L> relation) {
        boolean change = false;
        for (final InfoInRelation<L> inRelation : relation) {
            change = this.add(inRelation) || change;
        }
        return change;
    }

    public static <L> ReachabilityRelation<L> computeReachabilityRelation(OneSEVPA<L, JSONSymbol> automaton, boolean computeWitnesses) {
        LOGGER.info("Reach: start");
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

        return computeReachabilityRelationLoop(automaton, computeWitnesses, reachabilityRelation);
    }

    public static <L1, L2> ReachabilityRelation<L2> computeReachabilityRelation(OneSEVPA<L1, JSONSymbol> previousHypothesis, ReachabilityRelation<L1> previousReachabilityRelation, OneSEVPA<L2, JSONSymbol> currentHypothesis, boolean computeWitnesses) {
        LOGGER.info("Reach: start");
        final Alphabet<JSONSymbol> internalAlphabet = currentHypothesis.getInputAlphabet().getInternalAlphabet();

        // Using the previous hypothesis and reachability relation (from the last learning round), we want to avoid having to re-compute pairs (q, q') such that going from q to q' goes by paths that were not modified.
        // If we can find such (q, q'), we know that it is still in the reachability relation for the current hypothesis.

        // First, we want to know which location in the current hypothesis corresponds to a location in the previous hypothesis
        final Map<L1, L2> previousToCurrentLocations = new LinkedHashMap<>();
        for (final L1 locationInPrevious : previousHypothesis.getLocations()) {
            for (final L2 locationInCurrent : currentHypothesis.getLocations()) {
                if (previousHypothesis.getLocationId(locationInPrevious) == currentHypothesis.getLocationId(locationInCurrent)) {
                    previousToCurrentLocations.put(locationInPrevious, locationInCurrent);
                }
            }
        }

        // Second, we seek the locations for which no existing transitions were modified.
        // If a brand new return transition was added in the hypothesis, we do not consider it as a modification, as it strictly opens new paths in the VPA.
        // The set contains locations in the previous hypothesis
        final Set<L1> unmodifiedLocations = findUnmodifiedLocations(previousHypothesis, currentHypothesis, previousToCurrentLocations);
        System.out.println("Number of unmodified locations " + unmodifiedLocations + "; " + unmodifiedLocations.size() + " out of " + previousHypothesis.size());

        // Third, we initialize the reachability relation with the identity relation and the internal transitions
        final ReachabilityRelation<L2> reachabilityRelation = getIdentityRelation(currentHypothesis, computeWitnesses);
        for (final L2 location : currentHypothesis.getLocations()) {
            for (final JSONSymbol internal : internalAlphabet) {
                final L2 successor = currentHypothesis.getInternalSuccessor(location, internal);
                if (successor != null) {
                    reachabilityRelation.add(location, successor, constructWitness(internal, computeWitnesses));
                }
            }
        }
        System.out.println("Number of elements in reach before adding unmodified: " + reachabilityRelation.size());
        // Finally, we add the pairs from the previous relation that are still valid
        for (final InfoInRelation<L1> inRelation : previousReachabilityRelation) {
            final Set<L1> locationsOnPaths = inRelation.getLocationsBetweenStartAndTarget();
            // @formatter:off
            final Optional<L1> modifiedLocationOnPath = locationsOnPaths.stream()
                .filter(l -> !unmodifiedLocations.contains(l))
                .findAny();
            // @formatter:on
            if (modifiedLocationOnPath.isEmpty()) {
                final L2 inRelationStartInCurrent = previousToCurrentLocations.get(inRelation.getStart());
                final L2 inRelationTargetInCurrent = previousToCurrentLocations.get(inRelation.getTarget());
                final Word<JSONSymbol> witness = inRelation.getWitness();
                reachabilityRelation.add(inRelationStartInCurrent, inRelationTargetInCurrent, witness);
            }
        }
        System.out.println("Number of elements in reach after adding unmodified: " + reachabilityRelation.size());

        return computeReachabilityRelationLoop(currentHypothesis, computeWitnesses, reachabilityRelation);
    }

    public static <L> ReachabilityRelation<L> computeValueReachabilityRelation(final OneSEVPA<L, JSONSymbol> automaton, final ReachabilityRelation<L> reachabilityRelation, final boolean computeWitnesses) {
        LOGGER.info("Value reach: start");
        final Alphabet<JSONSymbol> primitiveValuesAlphabet = JSONSymbol.primitiveValuesAlphabet;
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = automaton.getInputAlphabet().getReturnAlphabet();

        final ReachabilityRelation<L> valueReachabilityRelation = new ReachabilityRelation<>();
        
        for (final L startLocation : automaton.getLocations()) {
            for (final JSONSymbol primitiveValue : primitiveValuesAlphabet) {
                final L successor = automaton.getInternalSuccessor(startLocation, primitiveValue);
                if (successor != null) {
                    final Word<JSONSymbol> witness = constructWitness(primitiveValue, computeWitnesses);
                    valueReachabilityRelation.add(startLocation, successor, witness);
                }
            }

            for (final JSONSymbol callSymbol : callAlphabet) {
                final L locationAfterCall = automaton.getInitialLocation();
                final int stackSymbol = automaton.encodeStackSym(startLocation, callSymbol);

                for (final InfoInRelation<L> inRelation : reachabilityRelation.getLocationsAndInfoInRelationWith(locationAfterCall)) {
                    final L locationBeforeReturn = inRelation.getTarget();
                    for (final JSONSymbol returnSymbol : returnAlphabet) {
                        final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSymbol, stackSymbol);
                        if (locationAfterReturn != null) {
                            final Word<JSONSymbol> witness = constructWitness(callSymbol, inRelation.getWitness(), returnSymbol, computeWitnesses);
                            valueReachabilityRelation.add(startLocation, locationAfterReturn, witness);
                        }
                    }
                }
            }
        }

        LOGGER.info("Value reach: end");
        return valueReachabilityRelation;
    }

    private static Word<JSONSymbol> constructWitness(boolean computeWitnesses) {
        if (computeWitnesses) {
            return Word.epsilon();
        }
        else {
            return null;
        }
    }

    private static Word<JSONSymbol> constructWitness(JSONSymbol symbol, boolean computeWitnesses) {
        if (computeWitnesses) {
            return Word.fromLetter(symbol);
        } else {
            return null;
        }
    }

    private static Word<JSONSymbol> constructWitness(JSONSymbol callSymbol, Word<JSONSymbol> witness, JSONSymbol returnSymbol, boolean computeWitnesses) {
        if (computeWitnesses) {
            final WordBuilder<JSONSymbol> builder = new WordBuilder<>(witness.length() + 2);
            builder.add(callSymbol);
            builder.append(witness);
            builder.add(returnSymbol);
            return builder.toWord();
        }
        else {
            return null;
        }
    }

    private static <L> ReachabilityRelation<L> getIdentityRelation(OneSEVPA<L, JSONSymbol> automaton, boolean computeWitnesses) {
        final ReachabilityRelation<L> relation = new ReachabilityRelation<>();
        for (final L loc : automaton.getLocations()) {
            relation.add(loc, loc, constructWitness(computeWitnesses));
        }
        return relation;
    }

    private static <L> ReachabilityRelation<L> computeReachabilityRelationLoop(OneSEVPA<L, JSONSymbol> automaton, boolean computeWitnesses, ReachabilityRelation<L> reachabilityRelation) {
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = automaton.getInputAlphabet().getReturnAlphabet();
        final List<L> locations = automaton.getLocations();

        LOGGER.info("Reach: init warshall");
        Warshall.warshall(reachabilityRelation, locations, computeWitnesses);
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

                    for (final InfoInRelation<L> inRelation : reachabilityRelation.getLocationsAndInfoInRelationWith(locationAfterCall)) {
                        final L locationBeforeReturn = inRelation.getTarget();
                        for (final JSONSymbol returnSym : returnAlphabet) {
                            final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSym, stackSym);
                            if (locationAfterReturn != null) {
                                final Word<JSONSymbol> witnessAfterToBefore = inRelation.getWitness();
                                final Word<JSONSymbol> witnessStartToTarget = constructWitness(callSym, witnessAfterToBefore, returnSym, computeWitnesses);

                                final Set<L> seenStates = new LinkedHashSet<>();
                                seenStates.addAll(inRelation.getLocationsBetweenStartAndTarget());
                                seenStates.add(locationBeforeCall);
                                seenStates.add(locationAfterReturn);

                                newLocationsInRelation.add(locationBeforeCall, locationAfterReturn, witnessStartToTarget, seenStates);
                            }
                        }
                    }
                }
            }
            change = reachabilityRelation.addAll(newLocationsInRelation);
            LOGGER.info("Reach: warshall loop");
            change = Warshall.warshall(reachabilityRelation, locations, computeWitnesses) || change;
            LOGGER.info("Reach: loop done");
        }

        return reachabilityRelation;
    }

    private static <L1, L2> boolean isLocationUnmodified(final L1 locationInPrevious, final OneSEVPA<L1, JSONSymbol> previousHypothesis, final OneSEVPA<L2, JSONSymbol> currentHypothesis, final Map<L1, L2> previousToCurrentLocations) {
        final Alphabet<JSONSymbol> internalAlphabet = currentHypothesis.getInputAlphabet().getInternalAlphabet();
        final Alphabet<JSONSymbol> callAlphabet = currentHypothesis.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = currentHypothesis.getInputAlphabet().getReturnAlphabet();

        final L2 locationInCurrent = previousToCurrentLocations.get(locationInPrevious);

        // We check whether there is an internal transition that already existed in the previous hypothesis and that leads to a different location in the current hypothesis
        for (final JSONSymbol internalSym : internalAlphabet) {
            final L1 targetInPrevious = previousHypothesis.getInternalSuccessor(locationInPrevious, internalSym);
            final L2 targetInCurrent = currentHypothesis.getInternalSuccessor(locationInCurrent, internalSym);
            if (targetInCurrent != previousToCurrentLocations.get(targetInPrevious)) {
                return false;
            }
        }

        // We do the same for return transitions
        for (final JSONSymbol callSymbol : callAlphabet) {
            for (final JSONSymbol returnSymbol : returnAlphabet) {
                for (final L1 locationBeforeCallInPrevious : previousHypothesis.getLocations()) {
                    final L2 locationBeforeCallInCurrent = previousToCurrentLocations.get(locationBeforeCallInPrevious);

                    final int stackSymInPrevious = previousHypothesis.encodeStackSym(locationBeforeCallInPrevious, callSymbol);
                    final int stackSymInCurrent = currentHypothesis.encodeStackSym(locationBeforeCallInCurrent, callSymbol);

                    final L1 targetInPrevious = previousHypothesis.getReturnSuccessor(locationInPrevious, returnSymbol, stackSymInPrevious);
                    final L2 targetInCurrent = currentHypothesis.getReturnSuccessor(locationInCurrent, returnSymbol, stackSymInCurrent);

                    if (targetInCurrent != previousToCurrentLocations.get(targetInPrevious)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

    private static <L1, L2> Set<L1> findUnmodifiedLocations(OneSEVPA<L1, JSONSymbol> previousHypothesis, OneSEVPA<L2, JSONSymbol> currentHypothesis, Map<L1, L2> previousToCurrentLocations) {
        final Set<L1> unmodifiedLocations = new LinkedHashSet<>();
        for (final L1 locationInPrevious : previousHypothesis.getLocations()) {
            if (isLocationUnmodified(locationInPrevious, previousHypothesis, currentHypothesis, previousToCurrentLocations)) {
                unmodifiedLocations.add(locationInPrevious);
            }
        }

        return unmodifiedLocations;
    }
}
