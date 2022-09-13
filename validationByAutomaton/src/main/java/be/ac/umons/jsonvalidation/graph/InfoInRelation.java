package be.ac.umons.jsonvalidation.graph;

import java.util.Objects;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

class InfoInRelation<L> {
    private final L start, target;
    private final Word<JSONSymbol> witness;

    public InfoInRelation(final L start, final L target, final Word<JSONSymbol> witness) {
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
        return "(" +  witness + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, target, witness);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof InfoInRelation)) {
            return false;
        }

        InfoInRelation<?> other = (InfoInRelation<?>)obj;
        return Objects.equals(other.witness, this.witness) && Objects.equals(other.start, this.start) && Objects.equals(other.target, this.target);
    }
}
