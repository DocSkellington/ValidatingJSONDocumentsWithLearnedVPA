package be.ac.umons.learningjson.validation.relation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.Alphabet;

/**
 * A relation storing triplets {@code (s, k, s')} such that it is possible to go
 * from {@code s} to {@code s'} by reading a well-matched word starting with
 * {@code k}.
 * 
 * @see InRelation for the class used to store a triplet
 * @author Gaëtan Staquet
 */
class ReachabilityRelation implements Iterable<InRelation> {
    private final Set<InRelation> relation;

    ReachabilityRelation() {
        this.relation = new HashSet<>();
    }

    /**
     * Tests whether there is triplet in the relation equals to
     * {@code (q, symbol, p)}.
     * 
     * @param q      The starting state in the triplet
     * @param symbol The symbol in the triplet
     * @param p      The target state in the triplet
     * @return True iff there is such a triplet
     */
    public boolean areInRelation(final Location q, final Location p) {
        return relation.contains(InRelation.of(q, p));
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
    public Iterator<InRelation> iterator() {
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

        final ReachabilityRelation other = (ReachabilityRelation) obj;
        return Objects.hash(this) == Objects.hash(other);
    }

    public int size() {
        return this.relation.size();
    }

    private void add(final InRelation inRel) {
        if (relation.contains(inRel)) {
            mergeSeenLocations(inRel);
        } else {
            relation.add(inRel);
        }
    }

    /**
     * Adds a new triplet in the relation.
     * 
     * @param q      The starting state in the triplet
     * @param symbol The symbol in the triplet
     * @param p      The target state in the triplet
     */
    void add(final Location q, final Location p) {
        add(InRelation.of(q, p));
    }

    /**
     * Adds all the triplets from {@code rel} inside this relation.
     * 
     * @param rel The relation to take the triplets from
     */
    private void addAll(final ReachabilityRelation rel) {
        rel.relation.forEach(r -> this.add(r));
    }

    private void mergeSeenLocations(final InRelation inRel) {
        for (InRelation alreadyInRelation : relation) {
            if (Objects.equals(inRel, alreadyInRelation)) {
                alreadyInRelation.addSeenLocations(inRel.getLocationsSeenBetweenStartAndTarget());
                return;
            }
        }
    }

    private void mergeSeenLocations(final ReachabilityRelation rel) {
        if (rel.size() < this.size()) {
            this.forEach(r -> rel.mergeSeenLocations(r));
        }
        else {
            rel.forEach(r -> this.mergeSeenLocations(r));
        }
    }

    private Set<Location> allLocationsOnAcceptingPath(DefaultOneSEVPA<JSONSymbol> automaton) {
        final Set<Location> locations = new HashSet<>();
        for (final InRelation inRelation : this) {
            if (inRelation.getStart().equals(automaton.getInitialLocation()) && inRelation.getTarget().isAccepting()) {
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
    Set<Location> identifyBinLocations(DefaultOneSEVPA<JSONSymbol> automaton) {
        Set<Location> binLocations = new HashSet<>(automaton.getLocations());
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
    ReachabilityRelation union(ReachabilityRelation other) {
        ReachabilityRelation newRelation = new ReachabilityRelation();
        newRelation.addAll(this);
        newRelation.addAll(other);
        return newRelation;
    }

    /**
     * Compute the result of the Post operation on this relation for the given call
     * and return symbols.
     * 
     * The Post operation is defined as the reachability relation obtained from this
     * relation such that the well-matched words we consider start with the call
     * symbol and end with the return symbol (and the middle comes from this
     * relation). That is, each time Post is applied, we allow one more nesting.
     * 
     * @param automaton    The VPA
     * @param callSymbol   The call symbol to consider
     * @param returnSymbol The return symbol to consider
     * @return The new relation
     */
    ReachabilityRelation post(final DefaultOneSEVPA<JSONSymbol> automaton, final JSONSymbol callSymbol,
            final JSONSymbol returnSymbol) {
        final ReachabilityRelation newRelation = new ReachabilityRelation();
        final int callSymbolIndex = automaton.getInputAlphabet().getCallSymbolIndex(callSymbol);
        final int returnSymbolIndex = automaton.getInputAlphabet().getReturnSymbolIndex(returnSymbol);

        for (final Location start : automaton.getLocations()) {
            final Location callTarget = automaton.getInitialLocation();
            final int stackSym = automaton.encodeStackSym(start, callSymbolIndex);
            if (callTarget == null) {
                continue;
            }

            for (final InRelation inRelation : relation) {
                if (Objects.equals(inRelation.getStart(), callTarget)) {
                    final Location target = inRelation.getTarget().getReturnSuccessor(returnSymbolIndex, stackSym);
                    if (target != null) {
                        final InRelation newInRelation = InRelation.of(start, target);
                        newInRelation.addSeenLocations(inRelation.getLocationsSeenBetweenStartAndTarget());
                        newRelation.add(newInRelation);
                    }
                }
            }
        }

        return newRelation;
    }

    /**
     * Compose this relation with the provided relation.
     * 
     * The composition operation creates a new relation that contains triplets
     * {@code (q, k, p)} if and only if there is {@code (q, k, q')} in this relation
     * and {@code (q', k', p)} in the other relation.
     * 
     * @param other The other relation
     * @return The composed relation
     */
    ReachabilityRelation compose(ReachabilityRelation other) {
        final ReachabilityRelation relation = new ReachabilityRelation();

        for (final InRelation inRelThis : this) {
            for (final InRelation inRelOther : other) {
                if (Objects.equals(inRelThis.getTarget(), inRelOther.getStart())) {
                    final InRelation newInRelation = InRelation.of(inRelThis.getStart(), inRelOther.getTarget());
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
    public static ReachabilityRelation computeCommaRelation(DefaultOneSEVPA<JSONSymbol> automaton) {
        final JSONSymbol symbol = JSONSymbol.commaSymbol;
        assert automaton.getInputAlphabet().getInternalAlphabet().contains(symbol);
        final int symbolIndex = automaton.getInputAlphabet().getInternalAlphabet().getSymbolIndex(symbol);
        final ReachabilityRelation relation = new ReachabilityRelation();

        // @formatter:off
        automaton.getLocations().stream()
            .map(loc -> InRelation.of(loc, loc.getInternalSuccessor(symbolIndex)))
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
    public static ReachabilityRelation computeInternalRelation(DefaultOneSEVPA<JSONSymbol> automaton) {
        final Alphabet<JSONSymbol> internalAlphabet = automaton.getInputAlphabet().getInternalAlphabet();
        // @formatter:off
        final List<Integer> internalSymbolIndices = internalAlphabet.stream()
            .filter(s -> !Objects.equals(s, JSONSymbol.commaSymbol))
            .map(s -> internalAlphabet.getSymbolIndex(s))
            .collect(Collectors.toList());
        // @formatter:on
        final ReachabilityRelation relation = new ReachabilityRelation();

        for (final Location loc : automaton.getLocations()) {
            // @formatter:off
            internalSymbolIndices.stream()
                .map(symbolIndex -> InRelation.of(loc, loc.getInternalSuccessor(symbolIndex)))
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
    public static ReachabilityRelation computeWellMatchedRelation(DefaultOneSEVPA<JSONSymbol> automaton,
            ReachabilityRelation commaRelation, ReachabilityRelation internalRelation) {
        final Set<ReachabilityRelation> relations = new HashSet<>();

        final Set<ReachabilityRelation> setIdentityRelation = new HashSet<>();
        setIdentityRelation.add(getIdentityRelation(automaton));
        final Set<ReachabilityRelation> setCommaAndInternalRelations = new HashSet<>();
        setCommaAndInternalRelations.add(commaRelation.union(internalRelation));

        final Set<ReachabilityRelation> initial = compositionClosure(setIdentityRelation, setCommaAndInternalRelations);

        Set<ReachabilityRelation> composedRelations = new HashSet<>();
        composedRelations.addAll(initial);

        while (true) {
            final Set<ReachabilityRelation> newRelations = new HashSet<>();
            for (ReachabilityRelation r : composedRelations) {
                final ReachabilityRelation curlyPost = r.post(automaton, JSONSymbol.openingCurlyBraceSymbol,
                        JSONSymbol.closingCurlyBraceSymbol);
                final ReachabilityRelation bracketPost = r.post(automaton, JSONSymbol.openingBracketSymbol,
                        JSONSymbol.closingBracketSymbol);

                addRelationToSet(newRelations, curlyPost);
                addRelationToSet(newRelations, bracketPost);
            }
            for (final ReachabilityRelation alreadyInRelations : relations) {
                for (final ReachabilityRelation inNewRelations : newRelations) {
                    alreadyInRelations.mergeSeenLocations(inNewRelations);
                }
            }
            newRelations.removeAll(relations);

            for (final ReachabilityRelation inNewRelations : newRelations) {
                addRelationToSet(relations, inNewRelations);
            }
            newRelations.removeAll(composedRelations);

            if (newRelations.isEmpty()) {
                break;
            } else {
                composedRelations = compositionClosure(composedRelations, newRelations);
            }
        }

        relations.removeAll(initial);

        final ReachabilityRelation result = new ReachabilityRelation();
        relations.forEach(r -> result.addAll(r));

        return result;
    }

    private static ReachabilityRelation getIdentityRelation(DefaultOneSEVPA<JSONSymbol> automaton) {
        final ReachabilityRelation relation = new ReachabilityRelation();
        for (final Location loc : automaton.getLocations()) {
            relation.add(loc, loc);
        }
        return relation;
    }

    private static Set<ReachabilityRelation> compositionClosure(final Set<ReachabilityRelation> composedRelations,
            final Set<ReachabilityRelation> relationsToCompose) {
        final Set<ReachabilityRelation> allComposedRelations = new HashSet<>(composedRelations);
        final Queue<ReachabilityRelation> toProcess = new LinkedList<>(relationsToCompose);

        while (!toProcess.isEmpty()) {
            final ReachabilityRelation relationToCompose = toProcess.poll();

            final Set<ReachabilityRelation> newRelations = new HashSet<>();
            for (final ReachabilityRelation relation : allComposedRelations) {
                addRelationToSet(newRelations, relation.compose(relationToCompose));
                addRelationToSet(newRelations, relationToCompose.compose(relation));
            }

            for (ReachabilityRelation relation : newRelations) {
                if (!allComposedRelations.contains(relation) && !toProcess.contains(relation)) {
                    toProcess.add(relation);
                }
            }

            for (final ReachabilityRelation inNewRelations : newRelations) {
                addRelationToSet(allComposedRelations, inNewRelations);
            }
        }

        return allComposedRelations;
    }

    private static void addRelationToSet(final Set<ReachabilityRelation> set, final ReachabilityRelation relation) {
        for (final ReachabilityRelation inSet : set) {
            if (Objects.equals(inSet, relation)) {
                inSet.mergeSeenLocations(relation);
                return;
            }
        }
        set.add(relation);
    }
}
