package be.ac.umons.permutationautomaton;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.permutationautomaton.relation.NodeInGraph;
import be.ac.umons.permutationautomaton.relation.ReachabilityGraph;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.VPDAlphabet;

public class PermutationAutomaton {
    private final ReachabilityGraph graph;
    private final DefaultOneSEVPA<JSONSymbol> automaton;
    private final VPDAlphabet<JSONSymbol> alphabet;

    public PermutationAutomaton(final DefaultOneSEVPA<JSONSymbol> automaton) {
        this.graph = new ReachabilityGraph(automaton);
        this.automaton = automaton;
        this.alphabet = automaton.getInputAlphabet();
    }

    PermutationAutomatonState getInitialState() {
        return new PermutationAutomatonState(Collections.singletonList(automaton.getInitialLocation()), null);
    }

    public boolean accepts(Iterable<JSONSymbol> input) {
        PermutationAutomatonState state = getState(input);
        if (state == null) {
            return false;
        }
        if (state.getStack() != null) {
            return false;
        }
        // @formatter:off
        return state.getLocations().stream()
            .filter(location -> location.isAccepting())
            .findAny().isPresent();
        // @formatter:on
    }

    PermutationAutomatonState getState(Iterable<JSONSymbol> input) {
        PermutationAutomatonState state = getInitialState();
        JSONSymbol previousSymbol = null;
        boolean ready = false;
        for (JSONSymbol symbol : input) {
            if (ready) {
                state = getSuccessor(state, previousSymbol, symbol);
                if (state == null) {
                    return null;
                }
            }
            else {
                ready = true;
            }
            previousSymbol = symbol;
        }
        state = getSuccessor(state, previousSymbol, null);
        return state;
    }

    PermutationAutomatonState getSuccessor(PermutationAutomatonState state, JSONSymbol currentSymbol, JSONSymbol nextSymbol) {
        if (state == null || state.getLocations().isEmpty()) {
            return null;
        }

        switch (alphabet.getSymbolType(currentSymbol)) {
            case CALL:
                return getCallSuccessor(state, currentSymbol, nextSymbol);
            case INTERNAL:
                return getInternalSuccessor(state, currentSymbol, nextSymbol);
            case RETURN:
                return getReturnSuccessor(state, currentSymbol);
            default:
                return null;
        }
    }

    private PermutationAutomatonState getInternalSuccessor(PermutationAutomatonState state, JSONSymbol currentIntSymbol, JSONSymbol nextSymbol) {
        if (currentIntSymbol.equals(JSONSymbol.commaSymbol) && state.getStack().peekCallSymbol().equals(JSONSymbol.openingCurlyBraceSymbol)) {
            return getCommaInObjectSuccessor(state, nextSymbol);
        }

        final List<Location> successorLocations = new LinkedList<>();
        boolean oneNotNull = false;
        for (Location location : state.getLocations()) {
            if (location == null) {
                successorLocations.add(null);
            }
            else {
                Location target = automaton.getInternalSuccessor(location, currentIntSymbol);
                successorLocations.add(target);
                if (target != null) {
                    oneNotNull = true;
                }
            }
        }
        if (oneNotNull) {
            return new PermutationAutomatonState(successorLocations, state.getStack());
        }
        else {
            return null;
        }
    }

    private void markNodesToReject(final List<Location> currentLocations, final JSONSymbol currentKey) {
        final List<NodeInGraph> nodesForKey = graph.getNodesForKey(currentKey);
        assert nodesForKey.size() == currentLocations.size();

        final Iterator<Location> itrLocations = currentLocations.iterator();
        final Iterator<NodeInGraph> itrNodes = nodesForKey.iterator();
        
        while (itrLocations.hasNext()) {
            assert itrNodes.hasNext();

            final Location location = itrLocations.next();
            final NodeInGraph node = itrNodes.next();
            if (location == null || !Objects.equals(node.getTargetLocation(), location)) {
                node.markRejected();
            }
        }
    }

    private PermutationAutomatonState getCommaInObjectSuccessor(PermutationAutomatonState state, JSONSymbol nextSymbol) {
        final List<Location> currentLocations = state.getLocations();
        final AutomatonStackContents currentStack = state.getStack();
        final JSONSymbol currentKey = currentStack.getCurrentKey();

        markNodesToReject(currentLocations, currentKey);

        if (!currentStack.addKey(nextSymbol)) {
            return null;
        }
        // @formatter:off
        List<Location> successorLocations = graph.getNodesForKey(nextSymbol).stream()
            .map(node -> node.getStartLocation())
            .collect(Collectors.toList());
        // @formatter:on

        if (successorLocations.isEmpty()) {
            return null;
        }

        return new PermutationAutomatonState(successorLocations, currentStack);
    }

    private PermutationAutomatonState getCallSuccessor(PermutationAutomatonState state, JSONSymbol currentCallSymbol, JSONSymbol nextSymbol) {
        final List<Location> currentLocations = state.getLocations();
        final AutomatonStackContents currentStack = state.getStack();
        final AutomatonStackContents newStack = AutomatonStackContents.push(currentLocations, currentCallSymbol, currentStack);

        final List<Location> successorLocations = new LinkedList<>();
        if (currentCallSymbol.equals(JSONSymbol.openingCurlyBraceSymbol)) {
            for (NodeInGraph node : graph.getNodesForKey(nextSymbol)) {
                successorLocations.add(node.getStartLocation());
            }
            newStack.addKey(nextSymbol);
        }
        else {
            successorLocations.add(automaton.getInitialLocation());
        }

        graph.addLayerInStack();

        if (successorLocations.isEmpty()) {
            return null;
        }

        return new PermutationAutomatonState(successorLocations, newStack);
    }

    private PermutationAutomatonState getReturnSuccessor(PermutationAutomatonState state, JSONSymbol retSymbol) {
        final List<Location> currentLocations = state.getLocations();
        final AutomatonStackContents currentStack = state.getStack();
        if (currentStack == null) {
            return null;
        }
        final JSONSymbol callSymbol = currentStack.peekCallSymbol();

        // @formatter:off
        final Set<Integer> stackSymbols = currentStack.peekLocations().stream()
            .map(location -> automaton.encodeStackSym(location, callSymbol))
            .collect(Collectors.toSet());
        // @formatter:on

        final List<Location> successorLocations = new LinkedList<>();
        
        if (retSymbol.equals(JSONSymbol.closingBracketSymbol)) {
            if (!callSymbol.equals(JSONSymbol.openingBracketSymbol)) {
                return null;
            }

            // If we have read an array, we take the transitions that pop the symbols that were added when reading [
            for (Location location : currentLocations) {
                for (int stackSym : stackSymbols) {
                    Location target = automaton.getReturnSuccessor(location, retSymbol, stackSym);
                    successorLocations.add(target);
                }
            }
        }
        else if (retSymbol.equals(JSONSymbol.closingCurlyBraceSymbol)) {
            if (!callSymbol.equals(JSONSymbol.openingCurlyBraceSymbol)) {
                return null;
            }
            final JSONSymbol currentKey = currentStack.getCurrentKey();

            markNodesToReject(currentLocations, currentKey);

            final Set<NodeInGraph> acceptingNodes = graph.getNodesAcceptingForLocationsAndNotInRejectedPath(currentStack.getSeenKeys(), currentStack.peekLocations());
            for (NodeInGraph acceptingNode : acceptingNodes) {
                Location beforeRetLocation = acceptingNode.getTargetLocation();
                for (int stackSym : stackSymbols) {
                    Location afterRetLocation = automaton.getReturnSuccessor(beforeRetLocation, retSymbol, stackSym);
                    if (afterRetLocation != null) {
                        successorLocations.add(afterRetLocation);
                    }
                }
            }
        }

        graph.popLayerInStack();

        if (successorLocations.isEmpty()) {
            return null;
        }
        return new PermutationAutomatonState(successorLocations, currentStack.pop());
    }

}
