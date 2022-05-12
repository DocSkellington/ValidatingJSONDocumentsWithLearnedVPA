package be.ac.umons.permutationautomaton.relation;

import java.util.BitSet;
import java.util.Objects;

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;

public class NodeInGraph {

    private final InRelation inRelation;
    private final BitSet acceptingForLocation;
    private NodeStackContents stackForRejected;

    public NodeInGraph(InRelation inRelation, DefaultOneSEVPA<JSONSymbol> automaton) {
        this.inRelation = inRelation;
        this.acceptingForLocation = new BitSet(automaton.size());
        this.stackForRejected = null;
        final JSONSymbol callSymbol = JSONSymbol.openingCurlyBraceSymbol;
        final JSONSymbol returnSymbol = JSONSymbol.closingCurlyBraceSymbol;
        final int returnSymbolIndex = automaton.getInputAlphabet().getReturnSymbolIndex(returnSymbol);
        for (int i = 0 ; i < automaton.size() ; i++) {
            final int stackSym = automaton.encodeStackSym(automaton.getLocation(i), callSymbol);
            if (inRelation.getTarget().getReturnSuccessor(returnSymbolIndex, stackSym) != null) {
                acceptingForLocation.set(i);
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
        return isAcceptingForLocation(location.getIndex());
    }

    public boolean isAcceptingForLocation(int locId) {
        return acceptingForLocation.get(locId);
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

    public void markRejected() {
        stackForRejected.markRejected();
    }

    public boolean isRejected() {
        if (stackForRejected == null) {
            return false;
        }
        return stackForRejected.peek();
    }
}
