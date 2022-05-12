package be.ac.umons.permutationautomaton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.Location;

/**
 * The stack used in a {@link PermutationAutomatonState}.
 * 
 * It contains a list with the locations before the call symbol, the call
 * symbol, the set with all the keys seen so far, and a pointer to the rest of
 * the stack.
 * 
 * @author Gaëtan Staquet
 */
class PermutationAutomatonStackContents {
    private final List<Location> locations;
    private final JSONSymbol callSymbol;
    private final Set<JSONSymbol> seenKeys = new HashSet<>();
    private JSONSymbol currentKey = null;
    private @Nullable final PermutationAutomatonStackContents rest;

    private PermutationAutomatonStackContents(final List<Location> locations, final JSONSymbol symbol) {
        this(locations, symbol, null);
    }

    private PermutationAutomatonStackContents(final List<Location> locations, final JSONSymbol symbol,
            final @Nullable PermutationAutomatonStackContents rest) {
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

    public @Nullable PermutationAutomatonStackContents pop() {
        return rest;
    }

    public static PermutationAutomatonStackContents push(final List<Location> locations, final JSONSymbol symbol,
            final PermutationAutomatonStackContents rest) {
        return new PermutationAutomatonStackContents(locations, symbol, rest);
    }
}
