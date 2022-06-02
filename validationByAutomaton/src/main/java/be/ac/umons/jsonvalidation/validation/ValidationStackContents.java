package be.ac.umons.jsonvalidation.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.Location;

/**
 * The stack used in a {@link ValidationState}.
 * 
 * It contains a set with the source-to-reached locations before the call
 * symbol, the call symbol, the set with all the keys seen so far, and a pointer
 * to the rest of the stack.
 * 
 * @author Gaëtan Staquet
 */
class ValidationStackContents {
    private final Set<PairSourceToReached> sourceToReachedLocationsBeforeCall;
    private final JSONSymbol callSymbol;
    private final Set<JSONSymbol> seenKeys = new HashSet<>();
    private JSONSymbol currentKey = null;
    private @Nullable final ValidationStackContents rest;

    private ValidationStackContents(final Set<PairSourceToReached> sourceToReachedLocations,
            final JSONSymbol symbol, final @Nullable ValidationStackContents rest) {
        this.sourceToReachedLocationsBeforeCall = sourceToReachedLocations;
        this.callSymbol = symbol;
        this.rest = rest;
    }

    public boolean addKey(JSONSymbol key) {
        currentKey = key;
        return seenKeys.add(key);
    }

    public Set<PairSourceToReached> peekSourceToReachedLocationsBeforeCall() {
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

    public @Nullable ValidationStackContents pop() {
        return rest;
    }

    public static ValidationStackContents push(final Set<PairSourceToReached> sourceToReachedLocations,
            final JSONSymbol symbol, final ValidationStackContents rest) {
        return new ValidationStackContents(sourceToReachedLocations, symbol, rest);
    }
}
