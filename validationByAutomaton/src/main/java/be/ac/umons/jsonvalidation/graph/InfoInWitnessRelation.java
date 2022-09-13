package be.ac.umons.jsonvalidation.graph;

import java.util.Objects;
import java.util.Set;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

class InfoInWitnessRelation<L> {
    private final L start, target;
    private final Word<JSONSymbol> witnessToStart;
    private final Word<JSONSymbol> witnessFromTarget;
    private final Set<L> seenLocationsToStart;
    private final Set<L> seenLocationsFromTarget;

    public InfoInWitnessRelation(final L start, final L target, final Word<JSONSymbol> witnessToStart, final Word<JSONSymbol> witnessFromTarget, final Set<L> seenLocationsToStart, final Set<L> seenLocationsFromTarget) {
        this.start = start;
        this.target = target;
        this.witnessToStart = witnessToStart;
        this.witnessFromTarget = witnessFromTarget;
        this.seenLocationsToStart = seenLocationsToStart;
        this.seenLocationsFromTarget = seenLocationsFromTarget;
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

    public Set<L> getSeenLocationsToStart() {
        return seenLocationsToStart;
    }

    public Set<L> getSeenLocationsFromTarget() {
        return seenLocationsFromTarget;
    }

    public boolean addSeenLocations(final Set<L> locationsToStart, final Set<L> locationsFromTarget) {
        boolean change = this.seenLocationsToStart.addAll(locationsToStart);
        return this.seenLocationsFromTarget.addAll(locationsFromTarget) || change;
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
