package be.ac.umons.learningjson;

import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.automata.vpda.Location;

/**
 * A state used in a {@link PermutationAutomaton}.
 * 
 * It contains a set with the source and reached locations in the VPA and a stack.
 * 
 * @author Gaëtan Staquet
 */
class PermutationAutomatonState {
    private final Set<PairSourceToReached> sourceToReachedLocations;
    private final PermutationAutomatonStackContents stack;

    public PermutationAutomatonState(final Set<PairSourceToReached> sourceToReachedLocations, final PermutationAutomatonStackContents stack) {
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

    public PermutationAutomatonStackContents getStack() {
        return stack;
    }
}
