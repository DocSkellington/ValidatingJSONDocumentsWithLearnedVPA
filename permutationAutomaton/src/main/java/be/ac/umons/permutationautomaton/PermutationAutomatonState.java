package be.ac.umons.permutationautomaton;

import java.util.List;

import net.automatalib.automata.vpda.Location;

public class PermutationAutomatonState {
    private final List<Location> locations;
    private final AutomatonStackContents stack;

    public PermutationAutomatonState(final List<Location> locations, final AutomatonStackContents stack) {
        this.locations = locations;
        this.stack = stack;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public AutomatonStackContents getStack() {
        return stack;
    }
}
