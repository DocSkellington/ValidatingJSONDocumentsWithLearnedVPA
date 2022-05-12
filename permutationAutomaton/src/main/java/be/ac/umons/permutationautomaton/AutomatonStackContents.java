package be.ac.umons.permutationautomaton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.Location;

class AutomatonStackContents {
    private final List<Location> locations;
    private final JSONSymbol callSymbol;
    private final Set<JSONSymbol> seenKeys = new HashSet<>();
    private JSONSymbol currentKey = null;
    private @Nullable final AutomatonStackContents rest;

    private AutomatonStackContents(final List<Location> locations, final JSONSymbol symbol) {
        this(locations, symbol, null);
    }

    private AutomatonStackContents(final List<Location> locations, final JSONSymbol symbol, final @Nullable AutomatonStackContents rest) {
        this.locations = locations;
        this.callSymbol = symbol;
        this.rest = rest;
    }

    public boolean addKey(JSONSymbol key) {
        currentKey = key;
        return seenKeys.add(key);
    }

    public List<Location> peekLocations() {
        return locations;
    }

    public JSONSymbol peekCallSymbol() {
        return callSymbol;
    }

    public Set<JSONSymbol> getSeenKeys() {
        return seenKeys;
    }

    public JSONSymbol getCurrentKey() {
        return currentKey;
    }

    public @Nullable AutomatonStackContents pop() {
        return rest;
    }

    public AutomatonStackContents push(final List<Location> locations, final JSONSymbol symbol) {
        return new AutomatonStackContents(locations, symbol, this);
    }

    public static AutomatonStackContents push(final List<Location> locations, final JSONSymbol symbol, final AutomatonStackContents rest) {
        return new AutomatonStackContents(locations, symbol, rest);
    }
}
