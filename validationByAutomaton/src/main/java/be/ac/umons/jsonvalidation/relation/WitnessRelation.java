package be.ac.umons.jsonvalidation.relation;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.logging.LearnLogger;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

class WitnessRelation<L> extends ReachabilityMatrix<L, InfoInWitnessRelation<L>> {

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

    private boolean add(L start, L target, Word<JSONSymbol> witnessToStart, Word<JSONSymbol> witnessFromTarget) {
        return add(new InfoInWitnessRelation<>(start, target, witnessToStart, witnessFromTarget));
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

    public static <L> WitnessRelation<L> computeWitnessRelation(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> reachabilityRelation) {
        LOGGER.info("Witness relation: start");
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = automaton.getInputAlphabet().getReturnAlphabet();
        final WitnessRelation<L> witnessRelation = new WitnessRelation<>();

        for (L location : automaton.getLocations()) {
            if (automaton.isAcceptingLocation(location)) {
                witnessRelation.add(automaton.getInitialLocation(), location, Word.epsilon(), Word.epsilon());
            }
        }
        LOGGER.info("Witness relation: init done");

        while (true) {
            final WitnessRelation<L> newInRelation = new WitnessRelation<>();
            for (final InfoInWitnessRelation<L> inWitnessRelation : witnessRelation) {
                for (final InfoInRelation<L> inReachabilityRelation : reachabilityRelation) {
                    if (inReachabilityRelation.getStart() == inWitnessRelation.getStart()) {
                        final Word<JSONSymbol> witnessToStart = inWitnessRelation.getWitnessToStart().concat(inReachabilityRelation.getWitness());
                        final Word<JSONSymbol> witnessFromTarget = inWitnessRelation.getWitnessFromTarget();
                        newInRelation.add(inReachabilityRelation.getTarget(), inWitnessRelation.getTarget(), witnessToStart, witnessFromTarget);
                    }
                    if (inReachabilityRelation.getTarget() == inWitnessRelation.getTarget()) {
                        final Word<JSONSymbol> witnessToStart = inWitnessRelation.getWitnessToStart();
                        final Word<JSONSymbol> witnessFromTarget = inReachabilityRelation.getWitness().concat(inWitnessRelation.getWitnessFromTarget());
                        newInRelation.add(inWitnessRelation.getStart(), inReachabilityRelation.getStart(), witnessToStart, witnessFromTarget);
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
                                newInRelation.add(locationAfterCall, locationBeforeReturn, witnessToStart, witnessFromTarget);
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

        LOGGER.info("Witness relation: end");
        return witnessRelation;
    }

}