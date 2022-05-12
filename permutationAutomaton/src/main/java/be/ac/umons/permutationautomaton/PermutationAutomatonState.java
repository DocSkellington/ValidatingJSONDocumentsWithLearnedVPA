package be.ac.umons.permutationautomaton;

import java.util.List;

import net.automatalib.automata.vpda.Location;

/**
 * A state used in a {@link PermutationAutomaton}.
 * 
 * It contains a list with the current locations in the VPA and a stack.
 * 
 * @author Gaëtan Staquet
 */
class PermutationAutomatonState {
    private final List<Location> locations;
    private final PermutationAutomatonStackContents stack;

    public PermutationAutomatonState(final List<Location> locations, final PermutationAutomatonStackContents stack) {
        this.locations = locations;
        this.stack = stack;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public PermutationAutomatonStackContents getStack() {
        return stack;
    }
}
