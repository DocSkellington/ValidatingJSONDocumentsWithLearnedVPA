package be.ac.umons.permutationautomaton;

import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.automata.vpda.Location;

/**
 * A state used in a {@link PermutationAutomaton}.
 * 
 * It contains a list with the current locations in the VPA and a stack.
 * 
 * @author Gaëtan Staquet
 */
class PermutationAutomatonState {
    private final Set<PairLocations> sourceToReachedLocations;
    private final PermutationAutomatonStackContents stack;

    public PermutationAutomatonState(final Set<PairLocations> sourceToReachedLocations, final PermutationAutomatonStackContents stack) {
        this.sourceToReachedLocations = sourceToReachedLocations;
        this.stack = stack;
    }

    public Set<PairLocations> getSourceToReachedLocations() {
        return sourceToReachedLocations;
    }

    public Set<Location> getReachedLocations() {
        return sourceToReachedLocations.stream()
            .map(pair -> pair.getReachedLocation())
            .collect(Collectors.toSet());
    }

    public PermutationAutomatonStackContents getStack() {
        return stack;
    }
}
