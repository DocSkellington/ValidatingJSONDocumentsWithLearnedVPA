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

public class OnAcceptingPathRelation<L> extends ReachabilityMatrix<L, OnAcceptingPath<L>> {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(OnAcceptingPathRelation.class);

    @Nullable
    public Word<JSONSymbol> getWitnessToIntermediate(L start, L target) {
        final OnAcceptingPath<L> cell = getCell(start, target);
        if (cell == null) {
            return null;
        }
        else {
            return cell.getWitnessToIntermediate();
        }
    }

    public Word<JSONSymbol> getWitnessFromIntermediate(L start, L target) {
        final OnAcceptingPath<L> cell = getCell(start, target);
        if (cell == null) {
            return null;
        }
        else {
            return cell.getWitnessFromIntermediate();
        }
    }

    public L identifyBinLocation(final OneSEVPA<L, JSONSymbol> automaton) {
        for (final L location : automaton.getLocations()) {
            if (!areInRelation(automaton.getInitialLocation(), location)) {
                return location;
            }
        }
        return null;
    }

    private boolean add(L initialLocation, L target, Word<JSONSymbol> witnessToStart, Word<JSONSymbol> witnessFromTarget) {
        return add(new OnAcceptingPath<>(target, witnessToStart, witnessFromTarget), initialLocation);
    }

    private boolean add(OnAcceptingPath<L> infoInRelation, L initialLocation) {
        if (areInRelation(initialLocation, infoInRelation.getIntermediate())) {
            // If start and target are already in relation, we have nothing to do, as we already have witnesses.
            return false;
        }
        else {
            set(initialLocation, infoInRelation.getIntermediate(), infoInRelation);
            return true;
        }
    }

    private boolean addAll(OnAcceptingPathRelation<L> relation, L initialLocation) {
        boolean change = false;
        for (OnAcceptingPath<L> inRelation : relation) {
            change = this.add(inRelation, initialLocation) || change;
        }
        return change;
    }

    public static <L1, L2> OnAcceptingPathRelation<L2> computeWitnessRelation(OneSEVPA<L1, JSONSymbol> previousHypothesis, OnAcceptingPathRelation<L1> previousWitnessRelation, OneSEVPA<L2, JSONSymbol> currentHypothesis, ReachabilityRelation<L2> reachabilityRelation, boolean computeWitnesses) {
        LOGGER.info("Witness relation: start");
        final OnAcceptingPathRelation<L2> witnessRelation = initializeWitnessRelation(currentHypothesis, reachabilityRelation, computeWitnesses);
        final Map<L1, L2> locationsPreviousToCurrent = Utils.createMapLocationsOfPreviousToCurrent(previousHypothesis, currentHypothesis);

        LOGGER.info("Number of elements in Rel before adding still valid: " + witnessRelation.size());
        for (final OnAcceptingPath<L1> inPreviousRelation : previousWitnessRelation) {
            final L2 intermediateLocation = locationsPreviousToCurrent.get(inPreviousRelation.getIntermediate());

            final State<L2> toIntermediateState = currentHypothesis.getSuccessor(new State<>(currentHypothesis.getInitialLocation(), null), inPreviousRelation.getWitnessToIntermediate());
            if (toIntermediateState == null) {
                continue;
            }
            
            final State<L2> fromIntermediateState = currentHypothesis.getSuccessor(new State<L2>(intermediateLocation, toIntermediateState.getStackContents()), inPreviousRelation.getWitnessFromIntermediate());
            if (fromIntermediateState != null && currentHypothesis.isAccepting(fromIntermediateState)) {
                witnessRelation.add(currentHypothesis.getInitialLocation(), intermediateLocation, inPreviousRelation.getWitnessToIntermediate(), inPreviousRelation.getWitnessFromIntermediate());
            }
        }
        LOGGER.info("Number of elements in Rel after adding still valid: " + witnessRelation.size());

        return computeWitnessRelationLoop(currentHypothesis, reachabilityRelation, witnessRelation, computeWitnesses);
    }

    public static <L> OnAcceptingPathRelation<L> computeWitnessRelation(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> reachabilityRelation, boolean computeWitnesses) {
        LOGGER.info("Witness relation: start");
        final OnAcceptingPathRelation<L> witnessRelation = initializeWitnessRelation(automaton, reachabilityRelation, computeWitnesses);
        return computeWitnessRelationLoop(automaton, reachabilityRelation, witnessRelation, computeWitnesses);
    }

    private static <L> OnAcceptingPathRelation<L> initializeWitnessRelation(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> reachabilityRelation, boolean computeWitnesses) {
        final OnAcceptingPathRelation<L> witnessRelation = new OnAcceptingPathRelation<>();
        final L initialLocation = automaton.getInitialLocation();
        for (final L location : automaton.getLocations()) {
            if (automaton.isAcceptingLocation(location)) {
                final Word<JSONSymbol> witness;
                if (computeWitnesses) {
                    witness = Word.epsilon();
                }
                else {
                    witness = null;
                }
                witnessRelation.add(initialLocation, location, witness, witness);
            }
        }
        LOGGER.info("Witness relation: init done");
        return witnessRelation;
    }

    private static <L> OnAcceptingPathRelation<L> computeWitnessRelationLoop(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> reachabilityRelation, OnAcceptingPathRelation<L> witnessRelation, boolean computeWitnesses) {
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final L initialLocation = automaton.getInitialLocation();

        while (true) {
            final OnAcceptingPathRelation<L> newInRelation = new OnAcceptingPathRelation<>();
            for (final OnAcceptingPath<L> inWitnessRelation : witnessRelation) {
                for (final InReachabilityRelation<L> inReachabilityRelation : reachabilityRelation) {
                    if (inReachabilityRelation.getTarget() == inWitnessRelation.getIntermediate()) {
                        final Word<JSONSymbol> witnessToIntermediate, witnessFromIntermediate;
                        
                        if (computeWitnesses) {
                            witnessToIntermediate = inWitnessRelation.getWitnessToIntermediate();
                            witnessFromIntermediate = inReachabilityRelation.getWitness().concat(inWitnessRelation.getWitnessFromIntermediate());
                        }
                        else {
                            witnessToIntermediate = witnessFromIntermediate = null;
                        }

                        newInRelation.add(initialLocation, inReachabilityRelation.getStart(), witnessToIntermediate, witnessFromIntermediate);
                    }
                }

                for (final InReachabilityRelation<L> inRelationWithInitial : reachabilityRelation.getLocationsAndInfoInRelationWithStart(initialLocation)) {
                    final L locationBeforeCall = inRelationWithInitial.getTarget();
                    for (final JSONSymbol callSymbol : callAlphabet) {
                        final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSymbol);

                        final JSONSymbol returnSymbol = callSymbol.callToReturn();

                        final Word<JSONSymbol> witnessToIntermediate;
                        if (computeWitnesses) {
                            witnessToIntermediate = inWitnessRelation.getWitnessToIntermediate().concat(inRelationWithInitial.getWitness()).append(callSymbol);
                        } 
                        else {
                            witnessToIntermediate = null;
                        }

                        // TODO: create a map beforehand to retrieve the correct locationBeforeReturn?
                        for (final L locationBeforeReturn : automaton.getLocations()) {
                            final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSymbol, stackSym);
                            if (Objects.equals(locationAfterReturn, inWitnessRelation.getIntermediate())) {
                                final Word<JSONSymbol> witnessFromIntermediate;
                                if (computeWitnesses) {
                                    witnessFromIntermediate = inWitnessRelation.getWitnessFromIntermediate().prepend(returnSymbol);
                                }
                                else {
                                    witnessFromIntermediate = null;
                                }

                                newInRelation.add(initialLocation, locationBeforeReturn, witnessToIntermediate, witnessFromIntermediate);
                            }
                        }
                    }
                }
            }

            if (!witnessRelation.addAll(newInRelation, initialLocation)) {
                break;
            }
            LOGGER.info("Witness relation: end loop");
        }

        LOGGER.info("Size: " + witnessRelation.size());
        return witnessRelation;
    }
}