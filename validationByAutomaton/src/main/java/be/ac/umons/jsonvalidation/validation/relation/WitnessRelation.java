package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

class WitnessRelation<L> implements Iterable<InfoInWitnessRelation<L>> {
    private final Map<L, Map<L, InfoInWitnessRelation<L>>> relation = new LinkedHashMap<>();
    
    @Override
    public String toString() {
        return relation.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(relation);
    }

    private class IteratorOverReachabilityMatrix implements Iterator<InfoInWitnessRelation<L>> {
        private Iterator<L> rowIterator;
        private Iterator<InfoInWitnessRelation<L>> cellIterator;

        public IteratorOverReachabilityMatrix() {
            this.rowIterator = relation.keySet().iterator();
            hasNext();
        }

        @Override
        public boolean hasNext() {
            if (cellIterator != null && cellIterator.hasNext()) {
                return true;
            }
            if (rowIterator.hasNext()) {
                L nextRow = rowIterator.next();
                cellIterator = relation.get(nextRow).values().iterator();
                return true;
            }
            return false;
        }

        @Override
        public InfoInWitnessRelation<L> next() {
            return cellIterator.next();
        }
    }

    @Override
    public Iterator<InfoInWitnessRelation<L>> iterator() {
        return new IteratorOverReachabilityMatrix();
    }

    public int size() {
        // @formatter:off
        return relation.values().stream()
            .mapToInt(map -> map.size())
            .sum();
        // @formatter:on
    }

    @Nullable
    private InfoInWitnessRelation<L> getCell(L start, L target) {
        if (!relation.containsKey(start)) {
            return null;
        }
        return relation.get(start).get(target);
    }

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
        return add(start, target, new InfoInWitnessRelation<>(start, target, witnessToStart, witnessFromTarget));
    }

    private boolean add(L start, L target, InfoInWitnessRelation<L> infoInRelation) {
        if (relation.containsKey(start)) {
            if (relation.get(start).containsKey(target)) {
                return false;
            }
            else {
                relation.get(start).put(target, infoInRelation);
                return true;
            }
        }
        else {
            relation.put(start, new LinkedHashMap<>());
            relation.get(start).put(target, infoInRelation);
            return true;
        }
    }

    private boolean add(InfoInWitnessRelation<L> infoInRelation) {
        return add(infoInRelation.getStart(), infoInRelation.getTarget(), infoInRelation);
    }

    private boolean addAll(WitnessRelation<L> relation) {
        boolean change = false;
        for (InfoInWitnessRelation<L> inRelation : relation) {
            change = this.add(inRelation) || change;
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
        }

        return witnessRelation;
    }

}