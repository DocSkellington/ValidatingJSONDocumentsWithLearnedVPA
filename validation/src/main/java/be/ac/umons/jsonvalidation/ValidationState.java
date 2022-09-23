package be.ac.umons.jsonvalidation;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The current state in an execution of {@link ValidationByAutomaton}.
 * 
 * <p>
 * It contains a set with the source and reached locations in the VPA and a stack.
 * </p>
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
