package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * A relation storing triplets {@code (s, k, s')} such that it is possible to go
 * from {@code s} to {@code s'} by reading a well-matched word starting with
 * {@code k}.
 * 
 * @see InRelation for the class used to store a triplet
 * @author Gaëtan Staquet
 */
public class ReachabilityRelation<L> implements Iterable<InRelation<L>> {
    private final Set<InRelation<L>> relation = new LinkedHashSet<>();

    /**
     * Tests whether there is triplet in the relation equals to
     * {@code (q, symbol, p)}.
     * 
     * @param q      The starting state in the triplet
     * @param symbol The symbol in the triplet
     * @param p      The target state in the triplet
     * @return True iff there is such a triplet
     */
    public boolean areInRelation(final L q, final L p) {
        return relation.contains(InRelation.of(q, p, Word.epsilon()));
    }

    public boolean areInRelation(final InRelation<L> inRelation) {
        return relation.contains(inRelation);
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
        return this.relation.iterator();
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

    boolean add(final InRelation<L> inRel) {
        if (relation.contains(inRel)) {
            return mergeSeenLocations(inRel);
        } else {
            return relation.add(inRel);
        }
    }

    /**
     * Adds all the triplets from {@code rel} inside this relation.
     * 
     * @param rel The relation to take the triplets from
     */
    private boolean addAll(final ReachabilityRelation<L> rel) {
        boolean change = false;
        for (InRelation<L> inRel : rel) {
            change = this.add(inRel) || change;
        }
        return change;
    }

    private boolean mergeSeenLocations(final InRelation<L> inRel) {
        for (InRelation<L> alreadyInRelation : relation) {
            if (Objects.equals(inRel, alreadyInRelation)) {
                return alreadyInRelation.addSeenLocations(inRel.getLocationsSeenBetweenStartAndTarget());
            }
        }
        return false;
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
        Set<L> binLocations = new LinkedHashSet<>(automaton.getLocations());
        binLocations.removeAll(allLocationsOnAcceptingPath(automaton));
        return binLocations;
    }

    /**
     * Creates a new relation that is union of this relation and the provided
     * relation.
     * 
     * @param other The other relation
     * @return The union of this and other
     */
    ReachabilityRelation<L> union(ReachabilityRelation<L> other) {
        ReachabilityRelation<L> newRelation = new ReachabilityRelation<>();
        newRelation.addAll(this);
        newRelation.addAll(other);
        return newRelation;
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

    private static <L> Word<JSONSymbol> constructWitness(InRelation<L> left, InRelation<L> right, boolean computeWitnesses) {
        if (computeWitnesses) {
            return left.getWitness().concat(right.getWitness());
        } else {
            return null;
        }
    }

    private static <L> Word<JSONSymbol> constructWitness(JSONSymbol callSymbol, InRelation<L> inRelation, JSONSymbol returnSymbol, boolean computeWitnesses) {
        if (computeWitnesses) {
            return Word.fromLetter(callSymbol).concat(inRelation.getWitness()).append(returnSymbol);
        }
        else {
            return null;
        }
    }

    /**
     * Compose this relation with the provided relation.
     * 
     * The composition operation creates a new relation that contains pairs
     * {@code (q, p)} if and only if there is {@code (q, q')} in this relation
     * and {@code (q', p)} in the other relation.
     * 
     * @param other The other relation
     * @return The composed relation
     */
    ReachabilityRelation<L> compose(ReachabilityRelation<L> other, boolean computeWitnesses) {
        final ReachabilityRelation<L> relation = new ReachabilityRelation<>();

        for (final InRelation<L> inRelThis : this) {
            for (final InRelation<L> inRelOther : other) {
                if (Objects.equals(inRelThis.getTarget(), inRelOther.getStart())) {
                    final Word<JSONSymbol> witness = constructWitness(inRelThis, inRelOther, computeWitnesses);
                    final InRelation<L> newInRelation = InRelation.of(inRelThis.getStart(), inRelOther.getTarget(), witness);
                    newInRelation.addSeenLocations(inRelThis.getLocationsSeenBetweenStartAndTarget());
                    newInRelation.addSeenLocations(inRelOther.getLocationsSeenBetweenStartAndTarget());
                    relation.add(newInRelation);
                }
            }
        }

        return relation;
    }

    /**
     * Compute the reachability relation where the well-matched words are restricted
     * to the single comma symbol.
     * 
     * @param automaton The VPA
     * @return The comma relation
     */
    public static <L> ReachabilityRelation<L> computeCommaRelation(OneSEVPA<L, JSONSymbol> automaton, boolean computeWitnesses) {
        final JSONSymbol symbol = JSONSymbol.commaSymbol;
        assert automaton.getInputAlphabet().getInternalAlphabet().contains(symbol);
        final ReachabilityRelation<L> relation = new ReachabilityRelation<L>();

        // @formatter:off
        automaton.getLocations().stream()
            .map(loc -> InRelation.of(loc, automaton.getInternalSuccessor(loc, symbol), constructWitness(symbol, computeWitnesses)))
            .filter(inRel -> inRel.getTarget() != null)
            .forEach(inRel -> relation.add(inRel));
        // @formatter:on

        return relation;
    }

    /**
     * Compute the reachability relation where the well-matched words are restricted
     * to a single internal symbol (except the comma).
     * 
     * @param automaton The VPA
     * @return The internal relation
     */
    public static <L> ReachabilityRelation<L> computeInternalRelation(OneSEVPA<L, JSONSymbol> automaton, boolean computeWitnesses) {
        final Alphabet<JSONSymbol> internalAlphabet = automaton.getInputAlphabet().getInternalAlphabet();
        // @formatter:off
        final List<JSONSymbol> internalSymbolWithoutComma = internalAlphabet.stream()
            .filter(s -> !Objects.equals(s, JSONSymbol.commaSymbol))
            .collect(Collectors.toList());
        // @formatter:on
        final ReachabilityRelation<L> relation = new ReachabilityRelation<>();

        for (final L loc : automaton.getLocations()) {
            // @formatter:off
            internalSymbolWithoutComma.stream()
                .map(symbol -> InRelation.of(loc, automaton.getInternalSuccessor(loc, symbol), constructWitness(symbol, computeWitnesses)))
                .filter(inRel -> inRel.getTarget() != null)
                .forEach(inRel -> relation.add(inRel));
            // @formatter:on
        }

        return relation;
    }

    /**
     * Compute the reachability relation where the well-matched words must contain
     * at least one call and one return symbols.
     * 
     * @param automaton        The VPA
     * @param commaRelation    The comma relation
     * @param internalRelation The internal relation
     * @return The well-matched relation
     */
    public static <L> ReachabilityRelation<L> computeWellMatchedRelation(OneSEVPA<L, JSONSymbol> automaton, ReachabilityRelation<L> commaRelation, ReachabilityRelation<L> internalRelation, boolean computeWitnesses) {
        final Alphabet<JSONSymbol> callAlphabet = automaton.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = automaton.getInputAlphabet().getReturnAlphabet();
        final ReachabilityRelation<L> wellMatched = new ReachabilityRelation<>();
        final ReachabilityRelation<L> reachabilityRelation = commaRelation.union(internalRelation).union(getIdentityRelation(automaton, computeWitnesses));

        boolean change = true;
        while (change) {
            change = false;
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return new ReachabilityRelation<>();
            }

            final ReachabilityRelation<L> newLocationsInRelation = new ReachabilityRelation<>();
            for (InRelation<L> left : reachabilityRelation) {
                for (InRelation<L> right : reachabilityRelation) {
                    if (left.getTarget().equals(right.getStart())) {
                        final Word<JSONSymbol> witness = constructWitness(left, right, computeWitnesses);
                        final InRelation<L> closed = InRelation.of(left.getStart(), right.getTarget(), witness);
                        closed.addSeenLocations(left.getLocationsSeenBetweenStartAndTarget());
                        closed.addSeenLocations(right.getLocationsSeenBetweenStartAndTarget());

                        boolean alreadyInRelation = false;
                        for (InRelation<L> inRelation : reachabilityRelation) {
                            if (inRelation.equals(closed)) {
                                if (!change) {
                                    Set<L> newLocations = new LinkedHashSet<>(closed.getLocationsSeenBetweenStartAndTarget());
                                    newLocations.removeAll(inRelation.getLocationsSeenBetweenStartAndTarget());
                                    change = !newLocations.isEmpty();
                                }

                                closed.addSeenLocations(inRelation.getLocationsSeenBetweenStartAndTarget());
                                alreadyInRelation = true;
                                break;
                            }
                        }
                        newLocationsInRelation.add(closed);

                        if (!alreadyInRelation) {
                            change = true;
                        }

                        if (wellMatched.areInRelation(closed)) {
                            change = wellMatched.mergeSeenLocations(closed) || change;
                        }
                    }
                }
            }

            for (L startingLocation : automaton.getLocations()) {
                for (JSONSymbol callSymbol : callAlphabet) {
                    int stackSym = automaton.encodeStackSym(startingLocation, callSymbol);
                    for (InRelation<L> inRelation : reachabilityRelation) {
                        if (inRelation.getStart().equals(automaton.getInitialLocation())) {
                            for (JSONSymbol returnSymbol : returnAlphabet) {
                                L targetLocation = automaton.getReturnSuccessor(inRelation.getTarget(), returnSymbol, stackSym);
                                if (targetLocation != null) {
                                    final Word<JSONSymbol> witness = constructWitness(callSymbol, inRelation, returnSymbol, computeWitnesses);
                                    final InRelation<L> callReturn = InRelation.of(startingLocation, targetLocation, witness);
                                    callReturn.addSeenLocations(inRelation.getLocationsSeenBetweenStartAndTarget());
                                    final Set<L> startingAndTargetLocations = new LinkedHashSet<>();
                                    startingAndTargetLocations.add(startingLocation);
                                    startingAndTargetLocations.add(targetLocation);
                                    callReturn.addSeenLocations(startingAndTargetLocations);

                                    boolean alreadyInRelation = false;
                                    for (InRelation<L> inRel : reachabilityRelation) {
                                        if (inRel.equals(callReturn)) {
                                            if (!change) {
                                                Set<L> newLocations = new LinkedHashSet<>(callReturn.getLocationsSeenBetweenStartAndTarget());
                                                newLocations.removeAll(inRel.getLocationsSeenBetweenStartAndTarget());
                                                change = !newLocations.isEmpty();
                                            }

                                            callReturn.addSeenLocations(inRel.getLocationsSeenBetweenStartAndTarget());
                                            alreadyInRelation = true;
                                            break;
                                        }
                                    }
                                    newLocationsInRelation.add(callReturn);
                                    change = wellMatched.add(callReturn) || change;

                                    if (!alreadyInRelation) {
                                        change = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            reachabilityRelation.addAll(newLocationsInRelation);
        }

        return wellMatched;
    }

    private static <L> ReachabilityRelation<L> getIdentityRelation(OneSEVPA<L, JSONSymbol> automaton, boolean computeWitnesses) {
        final ReachabilityRelation<L> relation = new ReachabilityRelation<>();
        for (final L loc : automaton.getLocations()) {
            relation.add(loc, loc);
        }
        return relation;
    }
}
