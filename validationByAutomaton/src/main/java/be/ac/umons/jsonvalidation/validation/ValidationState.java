package be.ac.umons.jsonvalidation.validation;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A state used in a {@link ValidationByAutomaton}.
 * 
 * It contains a set with the source and reached locations in the VPA and a stack.
 * 
 * @author Gaëtan Staquet
 */
public class ValidationState<L> {
    private final Set<PairSourceToReached<L>> sourceToReachedLocations;
    private final ValidationStackContents<L> stack;

    public ValidationState(final Set<PairSourceToReached<L>> sourceToReachedLocations, final ValidationStackContents<L> stack) {
        this.sourceToReachedLocations = sourceToReachedLocations;
        this.stack = stack;
    }

    public Set<PairSourceToReached<L>> getSourceToReachedLocations() {
        return sourceToReachedLocations;
    }

    public Set<L> getReachedLocations() {
        // @formatter:off
        return sourceToReachedLocations.stream()
            .map(pair -> pair.getReachedLocation())
            .collect(Collectors.toSet());
        // @formatter:on
    }

    public ValidationStackContents<L> getStack() {
        return stack;
    }
}
