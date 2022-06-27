package be.ac.umons.jsonvalidation.validation.relation;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

/**
 * A triplet {@code (s, k, s')} such that the VPA can go from {@code s} to
 * {@code s'} by reading a well-matched word starting with {@code k}.
 * 
 * The set of all locations seen when going from s to s' is stored alongside the
 * triplet.
 * 
 * @author Gaëtan Staquet
 */
class InRelation<L> {
    private final L start;
    private final L target;
    private final Set<L> locationsSeenBetweenStartAndTarget = new LinkedHashSet<>();
    private final Word<JSONSymbol> witness;

    /**
     * Creates a triplet.
     * 
     * @param start   The state to start in
     * @param target  The state to end in
     * @param witness A witness that there is a path from {@code start} to
     *                {@code target}
     * @return The triplet
     */
    public static <L> InRelation<L> of(final L start, final L target, Word<JSONSymbol> witness) {
        return new InRelation<>(start, target, witness);
    }

    private InRelation(final L start, final L target, Word<JSONSymbol> witness) {
        this.start = start;
        this.target = target;
        this.witness = witness;
        this.locationsSeenBetweenStartAndTarget.add(start);
        this.locationsSeenBetweenStartAndTarget.add(target);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InRelation)) {
            return false;
        }

        final InRelation<?> other = (InRelation<?>) obj;
        return Objects.equals(this.start, other.start) && Objects.equals(this.target, other.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, target);
    }

    @Override
    public String toString() {
        return "(" + start + ", " + target + ", " + locationsSeenBetweenStartAndTarget + ")";
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

    public Set<L> getLocationsSeenBetweenStartAndTarget() {
        return locationsSeenBetweenStartAndTarget;
    }

    boolean addSeenLocations(InRelation<L> other) {
        return locationsSeenBetweenStartAndTarget.addAll(other.getLocationsSeenBetweenStartAndTarget());
    }

    boolean addSeenLocation(L location) {
        return locationsSeenBetweenStartAndTarget.add(location);
    }
}
