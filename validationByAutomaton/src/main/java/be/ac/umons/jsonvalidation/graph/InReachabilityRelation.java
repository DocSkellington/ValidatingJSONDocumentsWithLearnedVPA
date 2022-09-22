package be.ac.umons.jsonvalidation.graph;

import java.util.Objects;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

/**
 * Stores two locations such that it is possible to go from the first location
 * to the second, alongside a witness of that fact.
 * 
 * @param <L> Location type
 * @author Gaëtan Staquet
 */
class InReachabilityRelation<L> {
    private final L start, target;
    private final Word<JSONSymbol> witness;

    public InReachabilityRelation(final L start, final L target, final Word<JSONSymbol> witness) {
        this.start = start;
        this.target = target;
        this.witness = witness;
    }

    public L getStart() {
        return start;
    }

    public L getTarget() {
        return target;
    }

    public Word<JSONSymbol> getWitness() {
        return witness;
    }

    @Override
    public String toString() {
        return "(" + witness + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, target, witness);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof InReachabilityRelation)) {
            return false;
        }

        final InReachabilityRelation<?> other = (InReachabilityRelation<?>) obj;
        return Objects.equals(other.witness, this.witness) && Objects.equals(other.start, this.start)
                && Objects.equals(other.target, this.target);
    }
}
