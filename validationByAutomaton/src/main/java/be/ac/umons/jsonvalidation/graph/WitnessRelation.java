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

public class WitnessRelation<L> extends ReachabilityMatrix<L, InfoInWitnessRelation<L>> {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(WitnessRelation.class);

    @Nullable
    public Word<JSONSymbol> getWitnessToStart(L start, L target) {
        final InfoInWitnessRelation<L> cell = getCell(start, target);
        if (cell == null) {
            return null;
        }
        else {
            return cell.getWitnessToStart();
        }
    }

    public Word<JSONSymbol> getWitnessFromTarget(L start, L target) {
        final InfoInWitnessRelation<L> cell = getCell(start, target);
        if (cell == null) {
            return null;
        }
        else {
            return cell.getWitnessFromTarget();
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
        return add(new InfoInWitnessRelation<>(target, witnessToStart, witnessFromTarget), initialLocation);
    }

    private boolean add(InfoInWitnessRelation<L> infoInRelation, L initialLocation) {
        if (areInRelation(initialLocation, infoInRelation.getTarget())) {
            // If start and target are already in relation, we have nothing to do, as we already have witnesses.
            return false;
        }
        else {
            set(initialLocation, infoInRelation.getTarget(), infoInRelation);
            return true;
        }
    }

    private boolean addAll(WitnessRelation<L> relation, L initialLocation) {
        boolean change = false;
        for (InfoInWitnessRelation<L> inRelation : relation) {
            change = this.add(inRelation, initialLocation) || change;
        }
        return change;
    }

    public static <L1, L2> WitnessRelation<L2> computeWitnessRelation(OneSEVPA<L1, JSONSymbol> previousHypothesis, WitnessRelation<L1> previousWitnessRelation, OneSEVPA<L2, JSONSymbol> currentHypothesis, ReachabilityRelation<L2> reachabilityRelation, boolean computeWitnesses) {
        LOGGER.info("Witness relation: start");
        final WitnessRelation<L2> witnessRelation = initializeWitnessRelation(currentHypothesis, reachabilityRelation, computeWitnesses);
        final Map<L1, L2> locationsPreviousToCurrent = UnmodifiedLocations.createMapLocationsOfPreviousToCurrent(previousHypothesis, currentHypothesis);

        System.out.println("Number of elements in Rel before adding still valid: " + reachabilityRelation.size());
        for (final InfoInWitnessRelation<L1> inPreviousRelation : previousWitnessRelation) {
            final L2 targetLocation = locationsPreviousToCurrent.get(inPreviousRelation.getTarget());

            final State<L2> toStartState = currentHypothesis.getSuccessor(new State<>(currentHypothesis.getInitialLocation(), null), inPreviousRelation.getWitnessToStart());
            if (toStartState == null) {
                continue;
            }
            
            final State<L2> fromTargetState = currentHypothesis.getSuccessor(new State<L2>(targetLocation, toStartState.getStackContents()), inPreviousRelation.getWitnessFromTarget());
            if (fromTargetState != null && currentHypothesis.isAccepting(fromTargetState)) {
                witnessRelation.add(currentHypothesis.getInitialLocation(), targetLocation, inPreviousRelation.getWitnessToStart(), inPreviousRelation.getWitnessFromTarget());
            }
        }
        System.out.println("Number of elements in Rel after adding still valid: " + reachabilityRelation.size());

        return computeWitnessRelationLoop(currentHypothesis, reachabilityRelation, witnessRelation, computeWitnesses);
    }

    public static <L> WitnessRelation<L> computeWitnessRelation(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> reachabilityRelation, boolean computeWitnesses) {
        LOGGER.info("Witness relation: start");
        final WitnessRelation<L> witnessRelation = initializeWitnessRelation(automaton, reachabilityRelation, computeWitnesses);
        return computeWitnessRelationLoop(automaton, reachabilityRelation, witnessRelation, computeWitnesses);
    }

    private static <L> WitnessRelation<L> initializeWitnessRelation(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> reachabilityRelation, boolean computeWitnesses) {
        final WitnessRelation<L> witnessRelation = new WitnessRelation<>();
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

    private static <L> WitnessRelation<L> computeWitnessRelationLoop(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> reachabilityRelation, WitnessRelation<L> witnessRelation, boolean computeWitnesses) {
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final L initialLocation = automaton.getInitialLocation();

        while (true) {
            final WitnessRelation<L> newInRelation = new WitnessRelation<>();
            for (final InfoInWitnessRelation<L> inWitnessRelation : witnessRelation) {
                for (final InfoInRelation<L> inReachabilityRelation : reachabilityRelation) {
                    if (inReachabilityRelation.getTarget() == inWitnessRelation.getTarget()) {
                        final Word<JSONSymbol> witnessToStart, witnessFromTarget;
                        
                        if (computeWitnesses) {
                            witnessToStart = inWitnessRelation.getWitnessToStart();
                            witnessFromTarget = inReachabilityRelation.getWitness().concat(inWitnessRelation.getWitnessFromTarget());
                        }
                        else {
                            witnessToStart = witnessFromTarget = null;
                        }

                        newInRelation.add(initialLocation, inReachabilityRelation.getStart(), witnessToStart, witnessFromTarget);
                    }
                }

                for (final InfoInRelation<L> inRelationWithInitial : reachabilityRelation.getLocationsAndInfoInRelationWithStart(initialLocation)) {
                    final L locationBeforeCall = inRelationWithInitial.getTarget();
                    for (final JSONSymbol callSymbol : callAlphabet) {
                        final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSymbol);

                        final JSONSymbol returnSymbol = callSymbol.callToReturn();

                        final Word<JSONSymbol> witnessToStart;
                        if (computeWitnesses) {
                            witnessToStart = inWitnessRelation.getWitnessToStart().concat(inRelationWithInitial.getWitness()).append(callSymbol);
                        } 
                        else {
                            witnessToStart = null;
                        }

                        // TODO: create a map beforehand to retrieve the correct locationBeforeReturn?
                        for (final L locationBeforeReturn : automaton.getLocations()) {
                            final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSymbol, stackSym);
                            if (Objects.equals(locationAfterReturn, inWitnessRelation.getTarget())) {
                                final Word<JSONSymbol> witnessFromTarget;
                                if (computeWitnesses) {
                                    witnessFromTarget = inWitnessRelation.getWitnessFromTarget().prepend(returnSymbol);
                                }
                                else {
                                    witnessFromTarget = null;
                                }

                                newInRelation.add(initialLocation, locationBeforeReturn, witnessToStart, witnessFromTarget);
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