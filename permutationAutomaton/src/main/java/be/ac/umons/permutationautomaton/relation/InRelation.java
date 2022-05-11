package be.ac.umons.permutationautomaton.relation;

import java.util.Objects;

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.Location;

class InRelation {
    private final Location start;
    private final JSONSymbol symbol;
    private final Location target;

    public static InRelation of(Location start, JSONSymbol symbol, Location target) {
        return new InRelation(start, symbol, target);
    }

    private InRelation(Location start, JSONSymbol symbol, Location target) {
        this.start = start;
        this.symbol = symbol;
        this.target = target;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InRelation)) {
            return false;
        }

        InRelation other = (InRelation) obj;
        return Objects.equals(this.start, other.start) && Objects.equals(this.target, other.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, target);
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
}
