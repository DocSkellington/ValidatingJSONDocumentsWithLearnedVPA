package be.ac.umons.learningjson.relation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.Location;

/**
 * A triplet {@code (s, k, s')} such that the VPA can go from {@code s} to
 * {@code s'} by reading a well-matched word starting with {@code k}.
 * 
 * The set of all locations seen when going from s to s' is stored alongside the
 * triplet.
 * 
 * @author Gaëtan Staquet
 */
class InRelation {
    private final Location start;
    private final JSONSymbol symbol;
    private final Location target;
    private final Set<Location> locationsSeenBetweenStartAndTarget = new HashSet<>();

    /**
     * Creates a triplet.
     * 
     * @param start  The state to start in
     * @param symbol The symbol that must begin a well-matched allowing to from
     *               {@code start} to {@code target}
     * @param target The state to end in
     * @return The triplet
     */
    public static InRelation of(final Location start, final JSONSymbol symbol, final Location target) {
        return new InRelation(start, symbol, target);
    }

    private InRelation(final Location start, final JSONSymbol symbol, final Location target) {
        this.start = start;
        this.symbol = symbol;
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

        final InRelation other = (InRelation) obj;
        return Objects.equals(this.start, other.start) && Objects.equals(this.symbol, other.symbol)
                && Objects.equals(this.target, other.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, target);
    }

    @Override
    public String toString() {
        return "(" + start + ", " + symbol + ", " + target + ", " + locationsSeenBetweenStartAndTarget + ")";
    }

    public Location getStart() {
        return start;
    }

    public JSONSymbol getSymbol() {
        return symbol;
    }

    public Location getTarget() {
        return target;
    }

    public Set<Location> getLocationsSeenBetweenStartAndTarget() {
        return locationsSeenBetweenStartAndTarget;
    }

    public void addSeenLocations(Set<Location> locations) {
        locationsSeenBetweenStartAndTarget.addAll(locations);
    }
}
