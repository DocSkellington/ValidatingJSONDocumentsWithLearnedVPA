package be.ac.umons.jsonvalidation.validation.relation;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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

    /**
     * Creates a triplet.
     * 
     * @param start  The state to start in
     * @param target The state to end in
     * @return The triplet
     */
    public static <L> InRelation<L> of(final L start, final L target) {
        return new InRelation<>(start, target);
    }

    private InRelation(final L start, final L target) {
        this.start = start;
        this.target = target;
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

    public Set<L> getLocationsSeenBetweenStartAndTarget() {
        return locationsSeenBetweenStartAndTarget;
    }

    public boolean addSeenLocations(Set<L> locations) {
        return locationsSeenBetweenStartAndTarget.addAll(locations);
    }
}
