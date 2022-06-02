package be.ac.umons.jsonvalidation.validation;

import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.automata.vpda.Location;

/**
 * A state used in a {@link ValidationByAutomaton}.
 * 
 * It contains a set with the source and reached locations in the VPA and a stack.
 * 
 * @author Gaëtan Staquet
 */
class ValidationState {
    private final Set<PairSourceToReached> sourceToReachedLocations;
    private final ValidationStackContents stack;

    public ValidationState(final Set<PairSourceToReached> sourceToReachedLocations, final ValidationStackContents stack) {
        this.sourceToReachedLocations = sourceToReachedLocations;
        this.stack = stack;
    }

    public Set<PairSourceToReached> getSourceToReachedLocations() {
        return sourceToReachedLocations;
    }

    public Set<Location> getReachedLocations() {
        // @formatter:off
        return sourceToReachedLocations.stream()
            .map(pair -> pair.getReachedLocation())
            .collect(Collectors.toSet());
        // @formatter:on
    }

    public ValidationStackContents getStack() {
        return stack;
    }
}
