package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * A relation storing triplets {@code (s, k, s')} such that it is possible to go
 * from {@code s} to {@code s'} by reading a well-matched word starting with
 * {@code k}.
 * 
 * @see InRelation for the class used to store a triplet
 * @author Gaëtan Staquet
 */
public class ReachabilityRelation<L> implements Iterable<InRelation<L>> {
    private final Map<InRelation<L>, Word<JSONSymbol>> relation = new LinkedHashMap<>();

    /**
     * Tests whether there is a couple in the relation equals to
     * {@code (q, p)}.
     * 
     * @param start      The starting state in the triplet
     * @param target      The target state in the triplet
     * @return True iff there is such a triplet
     */
    public boolean areInRelation(final L start, final L target) {
        return areInRelation(InRelation.of(start, target));
    }

    private boolean areInRelation(final InRelation<L> inRelation) {
        return relation.containsKey(inRelation);
    }

    public Word<JSONSymbol> getWitness(final L start, final L target) {
        return getWitness(InRelation.of(start, target));
    }

    Word<JSONSymbol> getWitness(final InRelation<L> inRelation) {
        return relation.get(inRelation);
    }
    
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
        return this.relation.keySet().iterator();
    }

    private Set<Map.Entry<InRelation<L>, Word<JSONSymbol>>> entrySet() {
        return this.relation.entrySet();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ReachabilityRelation)) {
            return false;
        }

        final ReachabilityRelation<?> other = (ReachabilityRelation<?>) obj;
        return Objects.hash(this) == Objects.hash(other);
    }

    public int size() {
        return this.relation.size();
    }

    private boolean add(final InRelation<L> inRel, final Word<JSONSymbol> witness) {
        for (final InRelation<L> alreadyInRelation : this) {
            if (Objects.equals(alreadyInRelation, inRel)) {
                return alreadyInRelation.addSeenLocations(inRel);
            }
        }
        return relation.put(inRel, witness) == null;
    }

    private boolean add(final L startLocation, final L targetLocation, final Word<JSONSymbol> witness) {
        return add(InRelation.of(startLocation, targetLocation), witness);
    }

    /**
     * Adds all the triplets from {@code rel} inside this relation.
     * 
     * @param rel The relation to take the triplets from
     */
    private boolean addAll(final ReachabilityRelation<L> rel) {
        boolean change = false;
        for (Map.Entry<InRelation<L>, Word<JSONSymbol>> inRel : rel.entrySet()) {
            change = this.add(inRel.getKey(), inRel.getValue()) || change;
        }
        return change;
    }

    private Set<L> allLocationsOnAcceptingPath(OneSEVPA<L, JSONSymbol> automaton) {
        final Set<L> locations = new LinkedHashSet<>();
        for (final InRelation<L> inRelation : this) {
            if (inRelation.getStart().equals(automaton.getInitialLocation()) && automaton.isAcceptingLocation(inRelation.getTarget())) {
                locations.addAll(inRelation.getLocationsSeenBetweenStartAndTarget());
            }
        }
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

    private Word<JSONSymbol> constructWitness(InRelation<L> left, InRelation<L> right, boolean computeWitnesses) {
        if (computeWitnesses) {
            return getWitness(left).concat(getWitness(right));
        } else {
            return null;
        }
    }

    private Word<JSONSymbol> constructWitness(JSONSymbol callSymbol, InRelation<L> inRelation, JSONSymbol returnSymbol, boolean computeWitnesses) {
        if (computeWitnesses) {
            final WordBuilder<JSONSymbol> builder = new WordBuilder<>();
            builder.add(callSymbol);
            builder.append(getWitness(inRelation));
            builder.add(returnSymbol);
            return builder.toWord();
        }
        else {
            return null;
        }
    }

    public Iterable<InRelation<L>> getPairsWithStartLocation(final L start) {
        return new Iterable<InRelation<L>>() {
            @Override
            public Iterator<InRelation<L>> iterator() {
                return new Iterator<InRelation<L>>() {

                    final Iterator<InRelation<L>> inRelation = relation.keySet().iterator();
                    InRelation<L> next = findNext();

                    private InRelation<L> findNext() {
                        while (inRelation.hasNext()) {
                            InRelation<L> inRel = inRelation.next();
                            if (inRel.getStart() == start) {
                                return inRel;
                            }
                        }
                        return null;
                    }

                    @Override
                    public boolean hasNext() {
                        return next != null;
                    }

                    @Override
                    public InRelation<L> next() {
                        final InRelation<L> toReturn = next;
                        next = findNext();
                        return toReturn;
                    }
                    
                };
            }
            
        };
    }

    public static <L> ReachabilityRelation<L> computeReachabilityRelation(OneSEVPA<L, JSONSymbol> automaton, boolean computeWitnesses) {
        final Alphabet<JSONSymbol> internalAlphabet = automaton.getInputAlphabet().getInternalAlphabet();
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = automaton.getInputAlphabet().getReturnAlphabet();

        final ReachabilityRelation<L> reachabilityRelation = getIdentityRelation(automaton, computeWitnesses);
        for (L location : automaton.getLocations()) {
            for (JSONSymbol internal : internalAlphabet) {
                L successor = automaton.getInternalSuccessor(location, internal);
                if (successor != null) {
                    reachabilityRelation.add(location, successor, constructWitness(internal, computeWitnesses));
                }
            }
        }

        boolean change = true;
        while (change) {
            change = false;
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return new ReachabilityRelation<>();
            }

            final ReachabilityRelation<L> newLocationsInRelation = new ReachabilityRelation<>();
            for (final InRelation<L> left : reachabilityRelation) {
                final L startLocation = left.getStart();
                final L intermediateLocation = left.getTarget();
                for (final InRelation<L> right : reachabilityRelation) {
                    // We have (q, p) and (p, q') in the reachability relation. So, (q, q') must also be in the relation
                    if (intermediateLocation == right.getStart()) {
                        final L targetLocation = right.getTarget();

                        final Word<JSONSymbol> witness = reachabilityRelation.constructWitness(left, right, computeWitnesses);
                        final InRelation<L> startToTarget = InRelation.of(startLocation, targetLocation);
                        startToTarget.addSeenLocations(left);
                        startToTarget.addSeenLocations(right);

                        newLocationsInRelation.add(startToTarget, witness);
                    }
                }
            }

            for (final L locationBeforeCall : automaton.getLocations()) {
                for (final JSONSymbol callSymbol : callAlphabet) {
                    final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSymbol);
                    final L locationAfterCall = automaton.getInitialLocation();
                    for (final InRelation<L> inRelation : reachabilityRelation) {
                        // We have (q, a, p, gamma) in the call transition function, and (p, p') in the reachability relation. So, if there is (p', a gamma, q') in the return transition function, we add (q, q') in the reachability relation
                        if (inRelation.getStart() == locationAfterCall) {
                            final L locationBeforeReturn = inRelation.getTarget();
                            for (final JSONSymbol returnSymbol : returnAlphabet) {
                                final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSymbol, stackSym);
                                if (locationAfterReturn != null) {
                                    final Word<JSONSymbol> witness = reachabilityRelation.constructWitness(callSymbol, inRelation, returnSymbol, computeWitnesses);
                                    final InRelation<L> callReturn = InRelation.of(locationBeforeCall, locationAfterReturn);
                                    callReturn.addSeenLocations(inRelation);

                                    newLocationsInRelation.add(callReturn, witness);
                                }
                            }
                        }
                    }
                }
            }

            change = reachabilityRelation.addAll(newLocationsInRelation);
        }

        return reachabilityRelation;
    }

    public static <L> ReachabilityRelation<L> computeValueReachabilityRelation(final OneSEVPA<L, JSONSymbol> automaton, final ReachabilityRelation<L> reachabilityRelation, final boolean computeWitnesses) {
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

                for (final InRelation<L> inRelation : reachabilityRelation.getPairsWithStartLocation(locationAfterCall)) {
                    final L locationBeforeReturn = inRelation.getTarget();
                    for (final JSONSymbol returnSymbol : returnAlphabet) {
                        final L locationAfterReturn = automaton.getReturnSuccessor(locationBeforeReturn, returnSymbol, stackSymbol);
                        if (locationAfterReturn != null) {
                            final Word<JSONSymbol> witness = reachabilityRelation.constructWitness(callSymbol, inRelation, returnSymbol, computeWitnesses);
                            valueReachabilityRelation.add(startLocation, locationAfterReturn, witness);
                        }
                    }
                }
            }
        }

        return valueReachabilityRelation;
    }

    private static <L> ReachabilityRelation<L> getIdentityRelation(OneSEVPA<L, JSONSymbol> automaton, boolean computeWitnesses) {
        final ReachabilityRelation<L> relation = new ReachabilityRelation<>();
        for (final L loc : automaton.getLocations()) {
            relation.add(loc, loc, constructWitness(computeWitnesses));
        }
        return relation;
    }
}
