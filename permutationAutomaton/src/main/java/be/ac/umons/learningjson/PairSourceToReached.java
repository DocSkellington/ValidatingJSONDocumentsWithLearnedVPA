package be.ac.umons.learningjson;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.automata.vpda.Location;

/**
 * A pair of two locations such that it is possible to go from the first to the
 * second.
 * 
 * Such a pair is used in the {@link PermutationAutomaton} to store the states
 * from which we started reading a word and the states reached after reading
 * that word.
 * 
 * @author Gaëtan Staquet
 */
public class PairSourceToReached {
    private final Location sourceLocation;
    private final Location reachedLocation;

    private PairSourceToReached(Location source, Location reached) {
        this.sourceLocation = source;
        this.reachedLocation = reached;
    }

    public Location getReachedLocation() {
        return reachedLocation;
    }

    public PairSourceToReached transitionToReached(final Location reachedLocation) {
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
        final PairSourceToReached other = (PairSourceToReached) obj;
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

    public static PairSourceToReached of(Location sourceLocation, Location reachedLocation) {
        return new PairSourceToReached(sourceLocation, reachedLocation);
    }

    public static Set<PairSourceToReached> getIdentityPairs(Collection<Location> locations) {
        // @formatter:off
        return locations.stream()
            .map(location -> PairSourceToReached.of(location, location))
            .collect(Collectors.toSet());
        // @formatter:on
    }
}
