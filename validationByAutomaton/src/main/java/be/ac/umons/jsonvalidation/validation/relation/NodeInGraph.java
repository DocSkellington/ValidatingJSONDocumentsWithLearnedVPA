package be.ac.umons.jsonvalidation.validation.relation;

import java.util.BitSet;
import java.util.Objects;
import java.util.Set;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.validation.PairSourceToReached;
import net.automatalib.automata.vpda.OneSEVPA;

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
class NodeInGraph<L> {

    private final InRelation<L> inRelation;
    private final JSONSymbol symbol;
    private final BitSet acceptingForLocation;
    private final BitSet onPathToAcceptingForLocation;
    private NodeStackContents stackForRejected;

    public NodeInGraph(InRelation<L> inRelation, JSONSymbol symbol, OneSEVPA<L, JSONSymbol> automaton, Set<L> binLocations) {
        this.inRelation = inRelation;
        this.symbol = symbol;
        this.acceptingForLocation = new BitSet(automaton.size());
        this.onPathToAcceptingForLocation = new BitSet(automaton.size());
        this.stackForRejected = null;

        final JSONSymbol callSymbol = JSONSymbol.openingCurlyBraceSymbol;
        final JSONSymbol returnSymbol = JSONSymbol.closingCurlyBraceSymbol;

        for (int i = 0; i < automaton.size(); i++) {
            // If the location before the call or the location after the return are the bin
            // locations, we ignore them as we do not want the bin state in the graph
            final L locationBeforeCall = automaton.getLocation(i);
            if (binLocations.contains(locationBeforeCall)) {
                continue;
            }

            final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSymbol);
            final L locationAfterReturn = automaton.getReturnSuccessor(inRelation.getTarget(), returnSymbol, stackSym);
            if (locationAfterReturn != null && !binLocations.contains(locationAfterReturn)) {
                acceptingForLocation.set(i);
                onPathToAcceptingForLocation.set(i);
            }
        }
    }

    InRelation<L> getInRelation() {
        return inRelation;
    }

    JSONSymbol getSymbol() {
        return symbol;
    }

    public L getStartLocation() {
        return getInRelation().getStart();
    }

    public L getTargetLocation() {
        return getInRelation().getTarget();
    }

    public boolean isAcceptingForLocation(int locationId) {
        return acceptingForLocation.get(locationId);
    }

    public boolean isOnPathToAcceptingForLocation(int locationId) {
        return onPathToAcceptingForLocation.get(locationId);
    }

    void setOnPathToAcceptingLocation(int locationId) {
        onPathToAcceptingForLocation.set(locationId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NodeInGraph)) {
            return false;
        }
        NodeInGraph<?> other = (NodeInGraph<?>) obj;
        return Objects.equals(this.inRelation, other.inRelation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inRelation);
    }

    @Override
    public String toString() {
        return inRelation.toString() + ", " + symbol;
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

    public PairSourceToReached<L> toPairLocations() {
        return PairSourceToReached.of(inRelation.getStart(), inRelation.getTarget());
    }
}
