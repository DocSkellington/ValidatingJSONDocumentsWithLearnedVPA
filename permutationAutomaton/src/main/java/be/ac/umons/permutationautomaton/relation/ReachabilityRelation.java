package be.ac.umons.permutationautomaton.relation;

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
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;

class ReachabilityRelation implements Iterable<InRelation> {
    private final Set<InRelation> relation;

    ReachabilityRelation() {
        this.relation = new HashSet<>();
    }

    public boolean areInRelation(final Location q, final JSONSymbol symbol, final Location p) {
        return relation.contains(InRelation.of(q, symbol, p));
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

        final ReachabilityRelation other = (ReachabilityRelation)obj;
        // return Objects.equal(this.relation, other.relation);
        return Objects.hash(this) == Objects.hash(other);
    }

    public int size() {
        return this.relation.size();
    }

    private void add(final InRelation inRel) {
        relation.add(inRel);
    }

    void add(final Location q, final JSONSymbol symbol, final Location p) {
        add(InRelation.of(q, symbol, p));
    }

    void addAll(final ReachabilityRelation rel) {
        this.relation.addAll(rel.relation);
    }

    ReachabilityRelation post(final DefaultOneSEVPA<JSONSymbol> automaton, final JSONSymbol callSymbol, final JSONSymbol returnSymbol) {
        final ReachabilityRelation relation = new ReachabilityRelation();
        final int callSymbolIndex = automaton.getInputAlphabet().getCallSymbolIndex(callSymbol);
        final int returnSymbolIndex = automaton.getInputAlphabet().getReturnSymbolIndex(returnSymbol);

        for (final Location start : automaton.getLocations()) {
            final Location callTarget = automaton.getInitialLocation();
            final int stackSym = automaton.encodeStackSym(start, callSymbolIndex);
            if (callTarget == null) {
                continue;
            }

            // @formatter:off
            this.relation.stream()
                .filter(inRel -> Objects.equals(inRel.getStart(), callTarget))
                .map(inRel -> Pair.of(inRel.getSymbol(), inRel.getTarget().getReturnSuccessor(returnSymbolIndex, stackSym)))
                .filter(pair -> pair.getSecond() != null)
                .forEach(pair -> relation.add(start, callSymbol, pair.getSecond()));
            // @formatter:on
        }

        return relation;
    }

    ReachabilityRelation compose(ReachabilityRelation other) {
        final ReachabilityRelation relation = new ReachabilityRelation();

        for (final InRelation inRelThis : this) {
            for (final InRelation inRelOther : other) {
                if (Objects.equals(inRelThis.getTarget(), inRelOther.getStart())) {
                    relation.add(inRelThis.getStart(), inRelThis.getSymbol(), inRelOther.getTarget());
                }
            }
        }

        return relation;
    }

    public static ReachabilityRelation computeCommaRelation(DefaultOneSEVPA<JSONSymbol> automaton) {
        final JSONSymbol symbol = JSONSymbol.commaSymbol;
        assert automaton.getInputAlphabet().getInternalAlphabet().contains(symbol);
        final int symbolIndex = automaton.getInputAlphabet().getInternalAlphabet().getSymbolIndex(symbol);
        final ReachabilityRelation relation = new ReachabilityRelation();

        // @formatter:off
        automaton.getLocations().stream()
            .map(loc -> InRelation.of(loc, symbol, loc.getInternalSuccessor(symbolIndex)))
            .filter(inRel -> inRel.getTarget() != null)
            .forEach(inRel -> relation.add(inRel));
        // @formatter:on

        return relation;
    }

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
                .map(symbolIndex -> InRelation.of(loc, internalAlphabet.getSymbol(symbolIndex), loc.getInternalSuccessor(symbolIndex)))
                .filter(inRel -> inRel.getTarget() != null)
                .forEach(inRel -> relation.add(inRel));
            // @formatter:on
        }

        return relation;
    }

    public static ReachabilityRelation computeWellMatchedRelation(DefaultOneSEVPA<JSONSymbol> automaton, ReachabilityRelation commaRelation, ReachabilityRelation internalRelation) {
        final Set<ReachabilityRelation> relations = new HashSet<>();

        Set<ReachabilityRelation> R_star = new HashSet<>();
        R_star.add(getIdentityRelation(automaton));
        Set<ReachabilityRelation> R_prime = new HashSet<>();
        R_prime.add(commaRelation);
        R_prime.add(internalRelation);

        final Set<ReachabilityRelation> initial = compositionClosure(R_star, R_prime);

        R_star = new HashSet<>();
        R_star.addAll(initial);
        R_prime = new HashSet<>();

        while (true) {
            Set<ReachabilityRelation> newR = new HashSet<>();
            for (ReachabilityRelation r : R_star) {
                newR.add(r.post(automaton, JSONSymbol.openingCurlyBraceSymbol, JSONSymbol.closingCurlyBraceSymbol));
                newR.add(r.post(automaton, JSONSymbol.openingBracketSymbol, JSONSymbol.closingBracketSymbol));
            }
            newR.removeAll(relations);

            relations.addAll(newR);
            newR.removeAll(R_star);
            R_prime = newR;

            if (R_prime.isEmpty()) {
                break;
            }
            else {
                R_star = compositionClosure(R_star, R_prime);
            }
        }

        relations.removeAll(initial);

        ReachabilityRelation result = new ReachabilityRelation();
        relations.stream()
            .forEach(r -> result.addAll(r));
        
        return result;
    }

    private static ReachabilityRelation getIdentityRelation(DefaultOneSEVPA<JSONSymbol> automaton) {
        final ReachabilityRelation relation = new ReachabilityRelation();
        for (final Location loc : automaton.getLocations()) {
            relation.add(loc, JSONSymbol.toSymbol(""), loc);
        }
        return relation;
    }

    private static Set<ReachabilityRelation> compositionClosure(Set<ReachabilityRelation> R_star, Set<ReachabilityRelation> R_prime) {
        Set<ReachabilityRelation> relations = new HashSet<>(R_star);
        Queue<ReachabilityRelation> toProcess = new LinkedList<>(R_prime);

        while (!toProcess.isEmpty()) {
            ReachabilityRelation rel = toProcess.poll();
            // newR_star.add(rel);

            Set<ReachabilityRelation> newRelations = new HashSet<>();
            for (ReachabilityRelation r : relations) {
                newRelations.add(r.compose(rel));
                newRelations.add(rel.compose(r));
            }

            for (ReachabilityRelation r : newRelations) {
                if (!relations.contains(r) && !toProcess.contains(r)) {
                    toProcess.add(r);
                }
            }

            relations.addAll(newRelations);

        }

        return relations;
    }
}
