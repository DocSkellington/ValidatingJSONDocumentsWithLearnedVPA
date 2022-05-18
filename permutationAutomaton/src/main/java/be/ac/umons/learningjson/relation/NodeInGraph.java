package be.ac.umons.learningjson.relation;

import java.util.BitSet;
import java.util.Objects;
import java.util.Set;

import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.learningjson.PairSourceToReached;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;

/**
 * A node in a {@link ReachabilityGraph}.
 * 
 * It is labeled by a triplet from the reachability relation. For each location
 * {@code p} in the VPA, it has a boolean indicating whether the target state of
 * the triplet can read a return symbol and pop the stack symbol corresponding
 * to {@code p}.
 * 
 * It also has a stack storing whether the node is rejected for each stacked
 * level.
 * 
 * @author Gaëtan Staquet
 */
class NodeInGraph {

    private final InRelation inRelation;
    private final BitSet acceptingForLocation;
    private final BitSet onPathToAcceptingForLocation;
    private NodeStackContents stackForRejected;

    public NodeInGraph(InRelation inRelation, DefaultOneSEVPA<JSONSymbol> automaton, Set<Location> binLocations) {
        this.inRelation = inRelation;
        this.acceptingForLocation = new BitSet(automaton.size());
        this.onPathToAcceptingForLocation = new BitSet(automaton.size());
        this.stackForRejected = null;

        final JSONSymbol callSymbol = JSONSymbol.openingCurlyBraceSymbol;
        final JSONSymbol returnSymbol = JSONSymbol.closingCurlyBraceSymbol;
        final int returnSymbolIndex = automaton.getInputAlphabet().getReturnSymbolIndex(returnSymbol);

        for (int i = 0; i < automaton.size(); i++) {
            // If the location before the call or the location after the return are the bin
            // locations, we ignore them as we do not want the bin state in the graph
            final Location locationBeforeCall = automaton.getLocation(i);
            if (binLocations.contains(locationBeforeCall)) {
                continue;
            }

            final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSymbol);
            final Location locationAfterReturn = inRelation.getTarget().getReturnSuccessor(returnSymbolIndex, stackSym);
            if (locationAfterReturn != null && !binLocations.contains(locationAfterReturn)) {
                acceptingForLocation.set(i);
                onPathToAcceptingForLocation.set(i);
            }
        }
    }

    InRelation getInRelation() {
        return inRelation;
    }

    public Location getStartLocation() {
        return getInRelation().getStart();
    }

    public Location getTargetLocation() {
        return getInRelation().getTarget();
    }

    public boolean isAcceptingForLocation(Location location) {
        return acceptingForLocation.get(location.getIndex());
    }

    public boolean isOnPathToAcceptingForLocation(Location location) {
        return onPathToAcceptingForLocation.get(location.getIndex());
    }

    void setOnPathToAcceptingLocation(Location location) {
        onPathToAcceptingForLocation.set(location.getIndex());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NodeInGraph)) {
            return false;
        }
        NodeInGraph other = (NodeInGraph) obj;
        return Objects.equals(this.inRelation, other.inRelation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inRelation);
    }

    @Override
    public String toString() {
        return inRelation.toString();
    }

    void addLayerInStack() {
        stackForRejected = NodeStackContents.push(false, stackForRejected);
    }

    void popLayerInStack() {
        stackForRejected = stackForRejected.pop();
    }

    void markRejected() {
        stackForRejected.markRejected();
    }

    public boolean isRejected() {
        if (stackForRejected == null) {
            return false;
        }
        return stackForRejected.peek();
    }

    public PairSourceToReached toPairLocations() {
        return PairSourceToReached.of(inRelation.getStart(), inRelation.getTarget());
    }
}
