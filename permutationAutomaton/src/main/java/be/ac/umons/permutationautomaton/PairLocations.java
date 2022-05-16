package be.ac.umons.permutationautomaton;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.automata.vpda.Location;

public class PairLocations {
    private final Location sourceLocation;
    private final Location reachedLocation;

    private PairLocations(Location source, Location reached) {
        this.sourceLocation = source;
        this.reachedLocation = reached;
    }

    public Location getSourceLocation() {
        return sourceLocation;
    }

    public Location getReachedLocation() {
        return reachedLocation;
    }

    public PairLocations compose(final PairLocations otherLocation) {
        if (Objects.equals(this.reachedLocation, otherLocation.sourceLocation)) {
            return PairLocations.of(this.sourceLocation, otherLocation.reachedLocation);
        }
        return null;
    }

    public PairLocations transition(final Location reachedLocation) {
        return PairLocations.of(this.sourceLocation, reachedLocation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PairLocations)) {
            return false;
        }
        final PairLocations other = (PairLocations) obj;
        return Objects.equals(other.sourceLocation, this.sourceLocation) && Objects.equals(other.reachedLocation, this.reachedLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceLocation, reachedLocation);
    }

    @Override
    public String toString() {
        return "(" + sourceLocation + ", " + reachedLocation + ")";
    }

    public static PairLocations of(Location sourceLocation, Location reachedLocation) {
        return new PairLocations(sourceLocation, reachedLocation);
    }

    public static Set<PairLocations> getIdentityPairs(Collection<Location> locations) {
        // @formatter:off
        return locations.stream()
            .map(location -> PairLocations.of(location, location))
            .collect(Collectors.toSet());
        // @formatter:on
    }
}
