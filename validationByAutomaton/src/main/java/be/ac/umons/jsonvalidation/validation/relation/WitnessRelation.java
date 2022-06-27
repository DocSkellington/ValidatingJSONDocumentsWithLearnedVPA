package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

class WitnessRelation<L> implements Iterable<InWitnessRelation<L>> {
    private final Map<InWitnessRelation<L>, InWitnessRelation<L>> relation = new LinkedHashMap<>();
    
    @Override
    public String toString() {
        return relation.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(relation);
    }

    @Override
    public Iterator<InWitnessRelation<L>> iterator() {
        return relation.values().iterator();
    }

    public int size() {
        return relation.size();
    }

    public InWitnessRelation<L> getInRelation(L start, L target) {
        return relation.get(InWitnessRelation.of(start, target, null, null));
    }

    private boolean add(L start, L target, Word<JSONSymbol> witnessToStart, Word<JSONSymbol> witnessFromTarget) {
        return add(InWitnessRelation.of(start, target, witnessToStart, witnessFromTarget));
    }

    private boolean add(InWitnessRelation<L> inRelation) {
        if (relation.containsKey(inRelation)) {
            return false;
        }
        relation.put(inRelation, inRelation);
        return true;
    }

    private boolean addAll(WitnessRelation<L> relation) {
        boolean change = false;
        for (Map.Entry<InWitnessRelation<L>, InWitnessRelation<L>> inRel : relation.relation.entrySet()) {
            change = this.add(inRel.getValue()) || change;
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
            for (final InWitnessRelation<L> inWitnessRelation : witnessRelation) {
                for (final InRelation<L> inReachabilityRelation : reachabilityRelation) {
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
        }

        return witnessRelation;
    }

}