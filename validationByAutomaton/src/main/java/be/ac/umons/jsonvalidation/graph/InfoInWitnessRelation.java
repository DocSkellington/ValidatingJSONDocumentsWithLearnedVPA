package be.ac.umons.jsonvalidation.graph;

import java.util.Objects;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

class InfoInWitnessRelation<L> {
    private final L start, target;
    private final Word<JSONSymbol> witnessToStart;
    private final Word<JSONSymbol> witnessFromTarget;

    public InfoInWitnessRelation(final L start, final L target, final Word<JSONSymbol> witnessToStart, final Word<JSONSymbol> witnessFromTarget) {
        this.start = start;
        this.target = target;
        this.witnessToStart = witnessToStart;
        this.witnessFromTarget = witnessFromTarget;
    }

    public L getStart() {
        return start;
    }

    public L getTarget() {
        return target;
    }

    public Word<JSONSymbol> getWitnessToStart() {
        return witnessToStart;
    }

    public Word<JSONSymbol> getWitnessFromTarget() {
        return witnessFromTarget;
    }

    @Override
    public String toString() {
        return "(" + getWitnessToStart() + ", " + getWitnessFromTarget() + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWitnessToStart(), getWitnessFromTarget());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof InfoInWitnessRelation)) {
            return false;
        }

        InfoInWitnessRelation<?> other = (InfoInWitnessRelation<?>)obj;
        return Objects.equals(other.witnessToStart, this.witnessToStart) && Objects.equals(other.witnessFromTarget, this.witnessFromTarget);
    }
}
