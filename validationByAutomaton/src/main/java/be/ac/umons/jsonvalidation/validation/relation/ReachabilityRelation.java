package be.ac.umons.jsonvalidation.validation.relation;

import java.util.LinkedHashSet;
import java.util.List;
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

    private Set<L> allLocationsOnAcceptingPath(OneSEVPA<L, JSONSymbol> automaton) {
        final Set<L> locations = new LinkedHashSet<>();
        // @formatter:off
        getLocationsAndInfoInRelationWith(automaton.getInitialLocation()).stream()
            .filter(info -> automaton.isAcceptingLocation(info.getTarget()))
            .forEach(info -> locations.addAll(info.getLocationsBetweenStartAndTarget()));
        // @formatter:on
        return locations;
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

    public boolean add(final L start, final L target, final Word<JSONSymbol> witness) {
        final Set<L> locationsBetweenStartAndTarget = new LinkedHashSet<>();
        locationsBetweenStartAndTarget.add(start);
        locationsBetweenStartAndTarget.add(target);
        return add(start, target, witness, locationsBetweenStartAndTarget);
    }

    public boolean add(final L start, final L target, final Word<JSONSymbol> witness, final Set<L> locationsBetweenStartAndTarget) {
        return add(new InfoInRelation<>(start, target, witness, locationsBetweenStartAndTarget));
    }

    private boolean add(final InfoInRelation<L> infoInRelation) {
        final L start = infoInRelation.getStart();
        final L target = infoInRelation.getTarget();
        if (containsKey(start)) {
            if (areInRelation(start, target)) {
                return getCell(start, target).addSeenLocations(infoInRelation.getLocationsBetweenStartAndTarget());
            }
        }
        set(start, target, infoInRelation);
        return true;
    }

    public boolean addAll(final ReachabilityRelation<L> relation) {
        boolean change = false;
        for (final InfoInRelation<L> inRelation : relation) {
            change = this.add(inRelation) || change;
        }
        return change;
    }

    static Word<JSONSymbol> constructWitness(Word<JSONSymbol> witnessFromStartToMid, Word<JSONSymbol> witnessFromMidToTarget, boolean computeWitnesses) {
        if (computeWitnesses) {
            return witnessFromStartToMid.concat(witnessFromMidToTarget);
        }
        else {
            return null;
        }
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

    public static <L> ReachabilityRelation<L> computeReachabilityRelation(OneSEVPA<L, JSONSymbol> automaton, boolean computeWitnesses) {
        LOGGER.info("Reach: start");
        final Alphabet<JSONSymbol> internalAlphabet = automaton.getInputAlphabet().getInternalAlphabet();
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = automaton.getInputAlphabet().getReturnAlphabet();
        final List<L> locations = automaton.getLocations();

        final ReachabilityRelation<L> relation = getIdentityRelation(automaton, computeWitnesses);
        for (final L start : locations) {
            for (final JSONSymbol internalSym : internalAlphabet) {
                final L target = automaton.getInternalSuccessor(start, internalSym);
                if (target != null) {
                    relation.add(start, target, constructWitness(internalSym, computeWitnesses));
                }
            }
        }
        LOGGER.info("Reach: init warshall");
        Warshall.warshall(relation, locations, computeWitnesses);
        LOGGER.info("Reach: init done");

        boolean change = true;
        while (change) {
            LOGGER.info("Reach: increasing depth");
            final ReachabilityRelation<L> newLocationsInRelation = new ReachabilityRelation<>();

            for (final L locationBeforeCall : locations) {
                for (final JSONSymbol callSym : callAlphabet) {
                    final L locationAfterCall = automaton.getInitialLocation();
                    final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSym);

                    for (final InfoInRelation<L> inRelation : relation.getLocationsAndInfoInRelationWith(locationAfterCall)) {
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
            change = relation.addAll(newLocationsInRelation);
            LOGGER.info("Reach: warshall loop");
            change = Warshall.warshall(relation, locations, computeWitnesses) || change;
            LOGGER.info("Reach: loop done");
        }

        return relation;
    }

    private static <L> ReachabilityRelation<L> getIdentityRelation(OneSEVPA<L, JSONSymbol> automaton, boolean computeWitnesses) {
        final ReachabilityRelation<L> relation = new ReachabilityRelation<>();
        for (final L loc : automaton.getLocations()) {
            relation.add(loc, loc, constructWitness(computeWitnesses));
        }
        return relation;
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
}
