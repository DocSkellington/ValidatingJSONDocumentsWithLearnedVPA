package be.ac.umons.jsonvalidation.graph;

import java.util.Objects;
import java.util.Set;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

class InfoInRelation<L> {
    private final L start, target;
    private final Word<JSONSymbol> witness;
    private final Set<L> locationsBetweenStartAndTarget;
    private final Set<L> locationsOnAPathBetweenStartAndTarget;

    public InfoInRelation(final L start, final L target, final Word<JSONSymbol> witness, final Set<L> locationsBetweenStartAndTarget, final Set<L> locationsOnAPathBetweenStartAndTarget) {
        this.start = start;
        this.target = target;
        this.witness = witness;
        this.locationsBetweenStartAndTarget = locationsBetweenStartAndTarget;
        this.locationsOnAPathBetweenStartAndTarget = locationsOnAPathBetweenStartAndTarget;
        assert this.locationsBetweenStartAndTarget.contains(start);
        assert this.locationsBetweenStartAndTarget.contains(target);
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

    public Set<L> getAllLocationsBetweenStartAndTarget() {
        return locationsBetweenStartAndTarget;
    }

    public Set<L> getLocationsOnAPathBetweenStartAndTarget() {
        return locationsOnAPathBetweenStartAndTarget;
    }

    public boolean addSeenLocations(final Set<L> locations) {
        return locationsBetweenStartAndTarget.addAll(locations);
    }
    
    public boolean addSeenLocation(final L location) {
        return locationsBetweenStartAndTarget.add(location);
    }

    @Override
    public String toString() {
        return "(" +  witness + ", " + locationsBetweenStartAndTarget + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(witness, locationsBetweenStartAndTarget);
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
        return Objects.equals(other.witness, this.witness) && Objects.equals(other.locationsBetweenStartAndTarget, this.locationsBetweenStartAndTarget);
    }
}
