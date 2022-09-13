package be.ac.umons.jsonvalidation.graph;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.logging.LearnLogger;
import net.automatalib.automata.vpda.OneSEVPA;
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

    private boolean add(L start, L target, Word<JSONSymbol> witnessToStart, Word<JSONSymbol> witnessFromTarget, Set<L> seenLocationsToStart, Set<L> seenLocationsFromTarget) {
        return add(new InfoInWitnessRelation<>(start, target, witnessToStart, witnessFromTarget, seenLocationsToStart, seenLocationsFromTarget));
    }

    private boolean add(InfoInWitnessRelation<L> infoInRelation) {
        if (areInRelation(infoInRelation.getStart(), infoInRelation.getTarget())) {
            // If start and target are already in relation, we have nothing to do, as we already have witnesses.
            return false;
        }
        else {
            set(infoInRelation.getStart(), infoInRelation.getTarget(), infoInRelation);
            return true;
        }
    }

    private boolean addAll(WitnessRelation<L> relation) {
        boolean change = false;
        for (InfoInWitnessRelation<L> inRelation : relation) {
            change = this.add(inRelation) || change;
        }
        return change;
    }

    public static <L1, L2> WitnessRelation<L2> computeWitnessRelation(OneSEVPA<L1, JSONSymbol> previousHypothesis, WitnessRelation<L1> previousWitnessRelation, OneSEVPA<L2, JSONSymbol> currentHypothesis, ReachabilityRelation<L2> reachabilityRelation) {
        LOGGER.info("Witness relation: start");
        final WitnessRelation<L2> witnessRelation = new WitnessRelation<>();

        final Map<L1, L2> previousToCurrentLocations = UnmodifiedLocations.createMapLocationsOfPreviousToCurrent(previousHypothesis, currentHypothesis);
        final Set<L1> unmodifiedLocations = UnmodifiedLocations.findUnmodifiedLocations(previousHypothesis, currentHypothesis, previousToCurrentLocations);

        for (final InfoInWitnessRelation<L1> inWitnessRelation : previousWitnessRelation) {
            // @formatter:off
            final Optional<L1> modifiedLocationsToStart = inWitnessRelation.getSeenLocationsToStart().stream()
                .filter(l -> !unmodifiedLocations.contains(l))
                .findAny();
            // @formatter:on

            if (modifiedLocationsToStart.isEmpty()) {
                continue;
            }

            // @formatter:off
            final Optional<L1> modifiedLocationsFromTarget = inWitnessRelation.getSeenLocationsFromTarget().stream()
                .filter(l -> !unmodifiedLocations.contains(l))
                .findAny();
            // @formatter:on

            if (modifiedLocationsFromTarget.isEmpty()) {
                final L2 startInCurrent = previousToCurrentLocations.get(inWitnessRelation.getStart());
                final L2 targetInCurrent = previousToCurrentLocations.get(inWitnessRelation.getTarget());
                final Word<JSONSymbol> witnessToStart = inWitnessRelation.getWitnessToStart();
                final Word<JSONSymbol> witnessFromTarget = inWitnessRelation.getWitnessFromTarget();
                final Set<L2> seenLocationsToStart = inWitnessRelation.getSeenLocationsToStart().stream().map(l -> previousToCurrentLocations.get(l)).collect(Collectors.toSet());
                final Set<L2> seenLocationsFromTarget = inWitnessRelation.getSeenLocationsFromTarget().stream().map(l -> previousToCurrentLocations.get(l)).collect(Collectors.toSet());

                witnessRelation.add(startInCurrent, targetInCurrent, witnessToStart, witnessFromTarget, seenLocationsToStart, seenLocationsFromTarget);
            }
        }

        computeWitnessRelationLoop(currentHypothesis, reachabilityRelation, witnessRelation);

        LOGGER.info("Witness relation: end");
        return witnessRelation;
    }

    public static <L> WitnessRelation<L> computeWitnessRelation(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> reachabilityRelation) {
        LOGGER.info("Witness relation: start");
        final WitnessRelation<L> witnessRelation = new WitnessRelation<>();

        computeWitnessRelationLoop(automaton, reachabilityRelation, witnessRelation);

        LOGGER.info("Witness relation: end");
        return witnessRelation;
    }

    private static <L> WitnessRelation<L> computeWitnessRelationLoop(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> reachabilityRelation, WitnessRelation<L> witnessRelation) {
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = automaton.getInputAlphabet().getReturnAlphabet();

        for (final L location : automaton.getLocations()) {
            if (automaton.isAcceptingLocation(location)) {
                final Set<L> seenLocationsToStart = new LinkedHashSet<>();
                seenLocationsToStart.add(automaton.getInitialLocation());
                final Set<L> seenLocationsFromTarget = new LinkedHashSet<>();
                seenLocationsFromTarget.add(location);
                witnessRelation.add(automaton.getInitialLocation(), location, Word.epsilon(), Word.epsilon(), seenLocationsToStart, seenLocationsFromTarget);
            }
        }
        LOGGER.info("Witness relation: init done");

        while (true) {
            final WitnessRelation<L> newInRelation = new WitnessRelation<>();
            for (final InfoInWitnessRelation<L> inWitnessRelation : witnessRelation) {
                for (final InfoInRelation<L> inReachabilityRelation : reachabilityRelation) {
                    if (inReachabilityRelation.getStart() == inWitnessRelation.getStart() && !witnessRelation.areInRelation(inReachabilityRelation.getTarget(), inWitnessRelation.getTarget())) {
                        final Word<JSONSymbol> witnessToStart = inWitnessRelation.getWitnessToStart().concat(inReachabilityRelation.getWitness());
                        final Word<JSONSymbol> witnessFromTarget = inWitnessRelation.getWitnessFromTarget();

                        final Set<L> seenLocationsToStart = new LinkedHashSet<>();
                        final Set<L> seenLocationsFromTarget = new LinkedHashSet<>();
                        if (!witnessRelation.areInRelation(inReachabilityRelation.getTarget(), inWitnessRelation.getTarget())) {
                            seenLocationsToStart.addAll(inWitnessRelation.getSeenLocationsToStart());
                            seenLocationsToStart.addAll(inReachabilityRelation.getAllLocationsBetweenStartAndTarget());

                            seenLocationsFromTarget.addAll(inWitnessRelation.getSeenLocationsFromTarget());
                        }

                        newInRelation.add(inReachabilityRelation.getTarget(), inWitnessRelation.getTarget(), witnessToStart, witnessFromTarget, seenLocationsToStart, seenLocationsFromTarget);
                    }
                    if (inReachabilityRelation.getTarget() == inWitnessRelation.getTarget() && !witnessRelation.areInRelation(inWitnessRelation.getStart(), inReachabilityRelation.getStart())) {
                        final Word<JSONSymbol> witnessToStart = inWitnessRelation.getWitnessToStart();
                        final Word<JSONSymbol> witnessFromTarget = inReachabilityRelation.getWitness().concat(inWitnessRelation.getWitnessFromTarget());

                        final Set<L> seenLocationsToStart = new LinkedHashSet<>();
                        final Set<L> seenLocationsFromTarget = new LinkedHashSet<>();
                        if (!witnessRelation.areInRelation(inWitnessRelation.getStart(), inReachabilityRelation.getStart())) {
                            seenLocationsToStart.addAll(inWitnessRelation.getSeenLocationsToStart());

                            seenLocationsFromTarget.addAll(inWitnessRelation.getSeenLocationsFromTarget());
                            seenLocationsFromTarget.addAll(inReachabilityRelation.getAllLocationsBetweenStartAndTarget());
                        }

                        newInRelation.add(inWitnessRelation.getStart(), inReachabilityRelation.getStart(), witnessToStart, witnessFromTarget, seenLocationsToStart, seenLocationsFromTarget);
                    }
                }

                final L locationBeforeCall = inWitnessRelation.getStart();
                for (final JSONSymbol callSymbol : callAlphabet) {
                    final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSymbol);
                    final L locationAfterCall = automaton.getInitialLocation();
                    final Word<JSONSymbol> witnessToStart = inWitnessRelation.getWitnessToStart().append(callSymbol);

                    for (final L locationBeforeReturn : automaton.getLocations()) {
                        for (final JSONSymbol returnSymbol : returnAlphabet) {
                            final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSymbol, stackSym);
                            if (locationAfterReturn == inWitnessRelation.getTarget()) {
                                final Word<JSONSymbol> witnessFromTarget = inWitnessRelation.getWitnessFromTarget().prepend(returnSymbol);

                                final Set<L> seenLocationsToStart = new LinkedHashSet<>();
                                final Set<L> seenLocationsFromTarget = new LinkedHashSet<>();

                                if (!witnessRelation.areInRelation(locationAfterCall, locationBeforeReturn)) {
                                    seenLocationsToStart.addAll(inWitnessRelation.getSeenLocationsToStart());
                                    seenLocationsToStart.add(locationAfterCall);

                                    seenLocationsFromTarget.addAll(inWitnessRelation.getSeenLocationsFromTarget());
                                    seenLocationsFromTarget.add(locationBeforeReturn);
                                }

                                newInRelation.add(locationAfterCall, locationBeforeReturn, witnessToStart, witnessFromTarget, seenLocationsToStart, seenLocationsFromTarget);
                            }
                        }
                    }
                }
            }

            if (!witnessRelation.addAll(newInRelation)) {
                break;
            }
            LOGGER.info("Witness relation: end loop");
        }

        LOGGER.info("Size: " + witnessRelation.size());
        return witnessRelation;
    }

}