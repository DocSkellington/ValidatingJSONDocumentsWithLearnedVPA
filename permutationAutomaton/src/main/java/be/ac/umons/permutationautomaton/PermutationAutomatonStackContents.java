package be.ac.umons.permutationautomaton;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final Set<PairLocations> sourceToReachedLocationsBeforeCall;
    private final JSONSymbol callSymbol;
    private final Set<JSONSymbol> seenKeys = new HashSet<>();
    private JSONSymbol currentKey = null;
    private @Nullable final PermutationAutomatonStackContents rest;

    private PermutationAutomatonStackContents(final Set<PairLocations> sourceToReachedLocations, final JSONSymbol symbol,
            final @Nullable PermutationAutomatonStackContents rest) {
        this.sourceToReachedLocationsBeforeCall = sourceToReachedLocations;
        this.callSymbol = symbol;
        this.rest = rest;
    }

    public boolean addKey(JSONSymbol key) {
        currentKey = key;
        return seenKeys.add(key);
    }

    public Set<PairLocations> peekSourceToReachedLocationsBeforeCall() {
        return sourceToReachedLocationsBeforeCall;
    }

    public Set<Location> peekReachedLocationsBeforeCall() {
        // @formatter:off
        return sourceToReachedLocationsBeforeCall.stream()
            .map(pair -> pair.getReachedLocation())
            .collect(Collectors.toSet());
        // @formatter:on
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

    public static PermutationAutomatonStackContents push(final Set<PairLocations> sourceToReachedLocations, final JSONSymbol symbol,
            final PermutationAutomatonStackContents rest) {
        return new PermutationAutomatonStackContents(sourceToReachedLocations, symbol, rest);
    }
}
