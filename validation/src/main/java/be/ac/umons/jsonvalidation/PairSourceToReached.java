package be.ac.umons.jsonvalidation;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A pair of two locations such that it is possible to go from the first to the
 * second.
 * 
 * <p>
 * Such a pair is used in the {@link ValidationByAutomaton} to store the
 * locations from which we started reading a word and the locations reached
 * after reading that word.
 * See {@link ValidationState}.
 * </p>
 * 
 * @author Gaëtan Staquet
 */
public class PairSourceToReached<L> {
    private final L sourceLocation;
    private final L reachedLocation;

    private PairSourceToReached(L source, L reached) {
        this.sourceLocation = source;
        this.reachedLocation = reached;
    }

    public L getSourceLocation() {
        return sourceLocation;
    }

    public L getReachedLocation() {
        return reachedLocation;
    }

    /**
     * Creates a new pair with the current source location, and the new reached
     * location.
     * 
     * @param reachedLocation The newly reached location
     * @return The new pair of locations
     */
    public PairSourceToReached<L> transitionToReached(final L reachedLocation) {
        return PairSourceToReached.of(this.sourceLocation, reachedLocation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PairSourceToReached)) {
            return false;
        }
        final PairSourceToReached<?> other = (PairSourceToReached<?>) obj;
        return Objects.equals(other.sourceLocation, this.sourceLocation)
                && Objects.equals(other.reachedLocation, this.reachedLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceLocation, reachedLocation);
    }

    @Override
    public String toString() {
        return "(" + sourceLocation + ", " + reachedLocation + ")";
    }

    public static <L> PairSourceToReached<L> of(L sourceLocation, L reachedLocation) {
        return new PairSourceToReached<>(sourceLocation, reachedLocation);
    }

    public static <L> Set<PairSourceToReached<L>> getIdentityPairs(Collection<L> locations) {
        // @formatter:off
        return locations.stream()
            .map(location -> PairSourceToReached.of(location, location))
            .collect(Collectors.toSet());
        // @formatter:on
    }
}
