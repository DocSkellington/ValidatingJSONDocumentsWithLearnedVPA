package be.ac.umons.jsonvalidation.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.JSONSymbol;

/**
 * The stack used in a {@link ValidationState}.
 * 
 * It contains a set with the source-to-reached locations before the call
 * symbol, the call symbol, the set with all the keys seen so far, and a pointer
 * to the rest of the stack.
 * 
 * @author Gaëtan Staquet
 */
class ValidationStackContents<L> {
    private final Set<PairSourceToReached<L>> sourceToReachedLocationsBeforeCall;
    private final JSONSymbol callSymbol;
    private final Set<JSONSymbol> seenKeys = new HashSet<>();
    private JSONSymbol currentKey = null;
    private @Nullable final ValidationStackContents<L> rest;

    private ValidationStackContents(final Set<PairSourceToReached<L>> sourceToReachedLocations,
            final JSONSymbol symbol, final @Nullable ValidationStackContents<L> rest) {
        this.sourceToReachedLocationsBeforeCall = sourceToReachedLocations;
        this.callSymbol = symbol;
        this.rest = rest;
    }

    public boolean addKey(JSONSymbol key) {
        currentKey = key;
        return seenKeys.add(key);
    }

    public Set<PairSourceToReached<L>> peekSourceToReachedLocationsBeforeCall() {
        return sourceToReachedLocationsBeforeCall;
    }

    public Set<L> peekReachedLocationsBeforeCall() {
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

    public @Nullable ValidationStackContents<L> pop() {
        return rest;
    }

    public static <L> ValidationStackContents<L> push(final Set<PairSourceToReached<L>> sourceToReachedLocations,
            final JSONSymbol symbol, final ValidationStackContents<L> rest) {
        return new ValidationStackContents<>(sourceToReachedLocations, symbol, rest);
    }
}