package be.ac.umons.permutationautomaton.relation;

import java.util.BitSet;
import java.util.Objects;

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;

class NodeInGraph {

    private final InRelation inRelation;
    private final BitSet acceptingForLocation;

    public NodeInGraph(InRelation inRelation, DefaultOneSEVPA<JSONSymbol> automaton) {
        this.inRelation = inRelation;
        this.acceptingForLocation = new BitSet(automaton.size());
        final JSONSymbol callSymbol = JSONSymbol.toSymbol("{");
        final JSONSymbol returnSymbol = JSONSymbol.toSymbol("}");
        final int returnSymbolIndex = automaton.getInputAlphabet().getReturnSymbolIndex(returnSymbol);
        for (int i = 0 ; i < automaton.size() ; i++) {
            final int stackSym = automaton.encodeStackSym(automaton.getLocation(i), callSymbol);
            if (inRelation.getTarget().getReturnSuccessor(returnSymbolIndex, stackSym) != null) {
                acceptingForLocation.set(i);
            }
        }
    }

    public InRelation getInRelation() {
        return inRelation;
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
}
