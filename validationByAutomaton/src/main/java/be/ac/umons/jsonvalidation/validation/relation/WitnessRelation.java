package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

class WitnessRelation<L> implements Iterable<InRelation<L>> {
    private final Map<InRelation<L>, Pair<Word<JSONSymbol>, Word<JSONSymbol>>> relation = new LinkedHashMap<>();
    
    @Override
    public String toString() {
        return relation.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(relation);
    }

    @Override
    public Iterator<InRelation<L>> iterator() {
        return relation.keySet().iterator();
    }

    public int size() {
        return relation.size();
    }

    public Word<JSONSymbol> getWitnessToStart(L start, L target) {
        return getWitnessToStart(InRelation.of(start, target));
    }

    private Word<JSONSymbol> getWitnessToStart(InRelation<L> inRelation) {
        return relation.get(inRelation).getFirst();
    }

    public Word<JSONSymbol> getWitnessFromTarget(L start, L target) {
        return getWitnessFromTarget(InRelation.of(start, target));
    }

    private Word<JSONSymbol> getWitnessFromTarget(InRelation<L> inRelation) {
        return relation.get(inRelation).getSecond();
    }

    private boolean add(L start, L target, Word<JSONSymbol> witnessToStart, Word<JSONSymbol> witnessFromTarget) {
        return add(InRelation.of(start, target), witnessToStart, witnessFromTarget);
    }

    private boolean add(InRelation<L> inRelation, Word<JSONSymbol> witnessToStart, Word<JSONSymbol> witnessFromTarget) {
        if (relation.containsKey(inRelation)) {
            return false;
        }
        relation.put(inRelation, Pair.of(witnessToStart, witnessFromTarget));
        return true;
    }

    private boolean addAll(WitnessRelation<L> relation) {
        boolean change = false;
        for (Map.Entry<InRelation<L>, Pair<Word<JSONSymbol>, Word<JSONSymbol>>> inRel : relation.relation.entrySet()) {
            change = this.add(inRel.getKey(), inRel.getValue().getFirst(), inRel.getValue().getSecond()) || change;
        }
        return change;
    }

    public static <L> WitnessRelation<L> computeWitnessRelation(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> reachabilityRelation) {
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = automaton.getInputAlphabet().getReturnAlphabet();
        final WitnessRelation<L> witnessRelation = new WitnessRelation<>();

        for (L location : automaton.getLocations()) {
            if (automaton.isAcceptingLocation(location)) {
                witnessRelation.add(automaton.getInitialLocation(), location, Word.epsilon(), Word.epsilon());
            }
        }

        while (true) {
            final WitnessRelation<L> newInRelation = new WitnessRelation<>();
            for (final InRelation<L> inWitnessRelation : witnessRelation) {
                for (final InRelation<L> inReachabilityRelation : reachabilityRelation) {
                    if (inReachabilityRelation.getStart() == inWitnessRelation.getStart()) {
                        final Word<JSONSymbol> witnessToStart = witnessRelation.getWitnessToStart(inWitnessRelation).concat(reachabilityRelation.getWitness(inReachabilityRelation));
                        final Word<JSONSymbol> witnessFromTarget = witnessRelation.getWitnessFromTarget(inWitnessRelation);
                        newInRelation.add(inReachabilityRelation.getTarget(), inWitnessRelation.getTarget(), witnessToStart, witnessFromTarget);
                    }
                    if (inReachabilityRelation.getTarget() == inWitnessRelation.getTarget()) {
                        final Word<JSONSymbol> witnessToStart = witnessRelation.getWitnessToStart(inWitnessRelation);
                        final Word<JSONSymbol> witnessFromTarget = reachabilityRelation.getWitness(inReachabilityRelation).concat(witnessRelation.getWitnessFromTarget(inWitnessRelation));
                        newInRelation.add(inWitnessRelation.getStart(), inReachabilityRelation.getStart(), witnessToStart, witnessFromTarget);
                    }
                }

                final L locationBeforeCall = inWitnessRelation.getStart();
                for (final JSONSymbol callSymbol : callAlphabet) {
                    final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSymbol);
                    final L locationAfterCall = automaton.getInitialLocation();
                    final Word<JSONSymbol> witnessToStart = witnessRelation.getWitnessToStart(inWitnessRelation).append(callSymbol);

                    for (final L locationBeforeReturn : automaton.getLocations()) {
                        for (final JSONSymbol returnSymbol : returnAlphabet) {
                            final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSymbol, stackSym);
                            if (locationAfterReturn == inWitnessRelation.getTarget()) {
                                final Word<JSONSymbol> witnessFromTarget = witnessRelation.getWitnessFromTarget(inWitnessRelation).prepend(returnSymbol);
                                newInRelation.add(locationAfterCall, locationBeforeReturn, witnessToStart, witnessFromTarget);
                            }
                        }
                    }
                }
            }

            if (!witnessRelation.addAll(newInRelation)) {
                break;
            }
        }

        return witnessRelation;
    }

}