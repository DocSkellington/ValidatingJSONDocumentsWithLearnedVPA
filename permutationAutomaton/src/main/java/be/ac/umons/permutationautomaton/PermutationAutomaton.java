package be.ac.umons.permutationautomaton;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.permutationautomaton.relation.ReachabilityGraph;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.VPDAlphabet;

/**
 * An automaton that allows permutations of pairs key-value inside a JSON
 * document.
 *
 * It is constructed from a visibly pushdown automaton (VPA). Such an automaton
 * can be
 * constructed by hand or learned through an active learning algorithm (see the
 * other project in the repository).
 * 
 * A JSON document is accepted if there is a path from the initial location with
 * empty stack to an accepting location with empty stack.
 * 
 * Due to the permutations, it is not possible in general to know the exact
 * current locations in the VPA. Therefore, the permutation automaton performs a
 * kind of subset construction. This means that the permutation automaton is
 * deterministic, i.e., it is never required to backtrack while reading a
 * document.
 * 
 * @author Gaëtan Staquet
 */
public class PermutationAutomaton {
    private final ReachabilityGraph graph;
    private final DefaultOneSEVPA<JSONSymbol> automaton;
    private final VPDAlphabet<JSONSymbol> alphabet;

    public PermutationAutomaton(final DefaultOneSEVPA<JSONSymbol> automaton) {
        this.graph = new ReachabilityGraph(automaton);
        this.automaton = automaton;
        this.alphabet = automaton.getInputAlphabet();
    }

    private PermutationAutomatonState getInitialState() {
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
            } else {
                ready = true;
            }
            previousSymbol = symbol;
        }
        state = getSuccessor(state, previousSymbol, null);
        return state;
    }

    PermutationAutomatonState getSuccessor(PermutationAutomatonState state, JSONSymbol currentSymbol,
            JSONSymbol nextSymbol) {
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

    private PermutationAutomatonState getInternalSuccessor(PermutationAutomatonState state, JSONSymbol currentIntSymbol,
            JSONSymbol nextSymbol) {
        if (currentIntSymbol.equals(JSONSymbol.commaSymbol)
                && state.getStack().peekCallSymbol().equals(JSONSymbol.openingCurlyBraceSymbol)) {
            return getCommaInObjectSuccessor(state, nextSymbol);
        }

        final List<Location> successorLocations = new LinkedList<>();
        boolean oneNotNull = false;
        for (Location location : state.getLocations()) {
            if (location == null) {
                successorLocations.add(null);
            } else {
                Location target = automaton.getInternalSuccessor(location, currentIntSymbol);
                successorLocations.add(target);
                if (target != null) {
                    oneNotNull = true;
                }
            }
        }
        if (oneNotNull) {
            return new PermutationAutomatonState(successorLocations, state.getStack());
        } else {
            return null;
        }
    }

    private PermutationAutomatonState getCommaInObjectSuccessor(PermutationAutomatonState state,
            JSONSymbol nextSymbol) {
        final List<Location> currentLocations = state.getLocations();
        final PermutationAutomatonStackContents currentStack = state.getStack();
        final JSONSymbol currentKey = currentStack.getCurrentKey();

        graph.markNodesToReject(currentLocations, currentKey);

        if (!currentStack.addKey(nextSymbol)) {
            return null;
        }
        final List<Location> successorLocations = graph.getLocationsReadingKey(nextSymbol);

        if (successorLocations.isEmpty()) {
            return null;
        }

        return new PermutationAutomatonState(successorLocations, currentStack);
    }

    private PermutationAutomatonState getCallSuccessor(PermutationAutomatonState state, JSONSymbol currentCallSymbol,
            JSONSymbol nextSymbol) {
        final List<Location> currentLocations = state.getLocations();
        final PermutationAutomatonStackContents currentStack = state.getStack();
        final PermutationAutomatonStackContents newStack = PermutationAutomatonStackContents.push(currentLocations,
                currentCallSymbol, currentStack);

        final List<Location> successorLocations;
        if (currentCallSymbol.equals(JSONSymbol.openingCurlyBraceSymbol)) {
            successorLocations = graph.getLocationsReadingKey(nextSymbol);
            newStack.addKey(nextSymbol);
        } else {
            successorLocations = new LinkedList<>();
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
        final PermutationAutomatonStackContents currentStack = state.getStack();
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

            // If we have read an array, we take the transitions that pop the symbols that
            // were added when reading [
            for (Location location : currentLocations) {
                for (int stackSym : stackSymbols) {
                    Location target = automaton.getReturnSuccessor(location, retSymbol, stackSym);
                    successorLocations.add(target);
                }
            }
        } else if (retSymbol.equals(JSONSymbol.closingCurlyBraceSymbol)) {
            if (!callSymbol.equals(JSONSymbol.openingCurlyBraceSymbol)) {
                return null;
            }
            final JSONSymbol currentKey = currentStack.getCurrentKey();

            graph.markNodesToReject(currentLocations, currentKey);

            // TODO: check if the size and order of successorsLocations is correct, in a
            // nested document
            final Set<Location> acceptingNodes = graph.getLocationsWithReturnTransitionOnUnmarkedPathsWithAllKeysSeen(
                    currentStack.getSeenKeys(), currentStack.peekLocations());
            for (final Location beforeRetLocation : acceptingNodes) {
                for (final int stackSym : stackSymbols) {
                    final Location afterRetLocation = automaton.getReturnSuccessor(beforeRetLocation, retSymbol,
                            stackSym);
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
