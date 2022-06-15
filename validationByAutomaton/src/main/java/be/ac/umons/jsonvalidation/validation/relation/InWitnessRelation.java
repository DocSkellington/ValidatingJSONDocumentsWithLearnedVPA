package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Objects;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

class InWitnessRelation<L> {
    private final L start;
    private final L target;
    private final Word<JSONSymbol> witnessToStart;
    private final Word<JSONSymbol> witnessFromTarget;

    public static <L> InWitnessRelation<L> of(final L start, final L target, Word<JSONSymbol> witnessToStart, Word<JSONSymbol> witnessFromTarget) {
        return new InWitnessRelation<>(start, target, witnessToStart, witnessFromTarget);
    }

    private InWitnessRelation(final L start, final L target, Word<JSONSymbol> witnessToStart, Word<JSONSymbol> witnessFromTarget) {
        this.start = start;
        this.target = target;
        this.witnessToStart = witnessToStart;
        this.witnessFromTarget = witnessFromTarget;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InWitnessRelation)) {
            return false;
        }

        final InWitnessRelation<?> other = (InWitnessRelation<?>) obj;
        return Objects.equals(this.start, other.start) && Objects.equals(this.target, other.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, target);
    }

    @Override
    public String toString() {
        return "(" + start + "; " + target + "; " + witnessToStart + "; " + witnessFromTarget + ")";
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
}
