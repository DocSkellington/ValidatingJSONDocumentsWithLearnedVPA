package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

class WitnessRelation<L> implements Iterable<InWitnessRelation<L>> {
    private final Set<InWitnessRelation<L>> relation = new LinkedHashSet<>();
    
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
        return relation.iterator();
    }

    public int size() {
        return relation.size();
    }

    public InWitnessRelation<L> getInRelation(L start, L target) {
        return relation.stream().filter(inRel -> inRel.getStart().equals(start) && inRel.getTarget().equals(target)).findAny().get();
    }

    private boolean add(InWitnessRelation<L> inRelation) {
        return relation.add(inRelation);
    }

    private boolean addAll(WitnessRelation<L> relation) {
        return this.relation.addAll(relation.relation);
    }

    public static <L> WitnessRelation<L> computeWitnessRelation(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> commaRelation, ReachabilityRelation<L> internalRelation, ReachabilityRelation<L> wellMatchedRelation) {
        final ReachabilityRelation<L> closedRelation = ReachabilityRelation.closeRelations(commaRelation, internalRelation, wellMatchedRelation);
        return computeWitnessRelation(automaton, commaRelation, internalRelation, wellMatchedRelation, closedRelation);
    }

    public static <L> WitnessRelation<L> computeWitnessRelation(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> commaRelation, ReachabilityRelation<L> internalRelation, ReachabilityRelation<L> wellMatchedRelation, ReachabilityRelation<L> closedRelation) {
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = automaton.getInputAlphabet().getReturnAlphabet();
        final WitnessRelation<L> witnessRelation = new WitnessRelation<>();

        for (L location : automaton.getLocations()) {
            if (automaton.isAcceptingLocation(location)) {
                witnessRelation.add(InWitnessRelation.of(automaton.getInitialLocation(), location, Word.epsilon(), Word.epsilon()));
            }
        }

        while (true) {
            final WitnessRelation<L> newInRelation = new WitnessRelation<>();
            for (InWitnessRelation<L> inWitnessRelation : witnessRelation) {
                for (InRelation<L> inClosedRelation : closedRelation) {
                    if (inClosedRelation.getStart().equals(inWitnessRelation.getStart())) {
                        final Word<JSONSymbol> witnessToStart = inWitnessRelation.getWitnessToStart().concat(inClosedRelation.getWitness());
                        final Word<JSONSymbol> witnessFromTarget = inWitnessRelation.getWitnessFromTarget();
                        newInRelation.add(InWitnessRelation.of(inClosedRelation.getTarget(), inWitnessRelation.getTarget(), witnessToStart, witnessFromTarget));
                    }
                    if (inClosedRelation.getTarget().equals(inWitnessRelation.getTarget())) {
                        final Word<JSONSymbol> witnessToStart = inWitnessRelation.getWitnessToStart();
                        final Word<JSONSymbol> witnessFromTarget = inClosedRelation.getWitness().concat(inWitnessRelation.getWitnessFromTarget());
                        newInRelation.add(InWitnessRelation.of(inWitnessRelation.getStart(), inClosedRelation.getStart(), witnessToStart, witnessFromTarget));
                    }
                }

                final L locationBeforeCall = inWitnessRelation.getStart();
                for (JSONSymbol callSymbol : callAlphabet) {
                    final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSymbol);
                    final L locationAfterCall = automaton.getInitialLocation();
                    final Word<JSONSymbol> witnessToStart = inWitnessRelation.getWitnessToStart().append(callSymbol);

                    for (L locationBeforeReturn : automaton.getLocations()) {
                        for (JSONSymbol returnSymbol : returnAlphabet) {
                            final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSymbol, stackSym);
                            if (locationAfterReturn == inWitnessRelation.getTarget()) {
                                final Word<JSONSymbol> witnessFromTarget = inWitnessRelation.getWitnessFromTarget().prepend(returnSymbol);
                                newInRelation.add(InWitnessRelation.of(locationAfterCall, locationBeforeReturn, witnessToStart, witnessFromTarget));
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