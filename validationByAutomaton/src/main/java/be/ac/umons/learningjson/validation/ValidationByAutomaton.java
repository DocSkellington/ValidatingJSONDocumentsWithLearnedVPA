package be.ac.umons.learningjson.validation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.learningjson.validation.relation.DotWriter;
import be.ac.umons.learningjson.validation.relation.ReachabilityGraph;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.VPDAlphabet;

/**
 * An automaton that allows permutations of pairs key-value inside a JSON
 * document.
 *
 * It is constructed from a visibly pushdown automaton (VPA). Such an automaton
 * can be constructed by hand or learned through an active learning algorithm
 * (see the other project in the repository).
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
public class ValidationByAutomaton {
    private final ReachabilityGraph graph;
    private final DefaultOneSEVPA<JSONSymbol> automaton;
    private final VPDAlphabet<JSONSymbol> alphabet;

    // TODO: delete
    public ReachabilityGraph getGraph() {
        return graph;
    }

    public ValidationByAutomaton(final DefaultOneSEVPA<JSONSymbol> automaton) {
        System.out.println("Start of automaton");
        this.graph = new ReachabilityGraph(automaton);
        System.out.println("Graph created ");
        try {
            DotWriter.write(graph, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.automaton = automaton;
        this.alphabet = automaton.getInputAlphabet();
    }

    private ValidationState getInitialState() {
        final Set<Location> setWithInitialLocation = new HashSet<>();
        setWithInitialLocation.add(automaton.getInitialLocation());
        return new ValidationState(PairSourceToReached.getIdentityPairs(setWithInitialLocation), null);
    }

    public boolean accepts(Iterable<JSONSymbol> input) {
        ValidationState state = getState(input);
        if (state == null) {
            return false;
        }
        if (state.getStack() != null) {
            return false;
        }
        // Do we have at least one accepting location?
        // @formatter:off
        return state.getSourceToReachedLocations().stream()
            .map(pair -> pair.getReachedLocation())
            .filter(location -> location.isAccepting())
            .findAny().isPresent();
        // @formatter:on
    }

    ValidationState getState(Iterable<JSONSymbol> input) {
        ValidationState state = getInitialState();
        JSONSymbol symbolToRead = null;
        boolean ready = false;
        for (JSONSymbol nextSymbol : input) {
            if (ready) {
                state = getSuccessor(state, symbolToRead, nextSymbol);
                if (state == null) {
                    return null;
                }
            } else {
                ready = true;
            }
            symbolToRead = nextSymbol;
        }
        if (symbolToRead != null) {
            state = getSuccessor(state, symbolToRead, null);
        }
        return state;
    }

    ValidationState getSuccessor(ValidationState state, JSONSymbol currentSymbol,
            JSONSymbol nextSymbol) {
        if (state == null || state.getSourceToReachedLocations().isEmpty()) {
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

    private ValidationState getInternalSuccessor(ValidationState state, JSONSymbol currentIntSymbol,
            JSONSymbol nextSymbol) {
        if (currentIntSymbol.equals(JSONSymbol.commaSymbol)
                && state.getStack().peekCallSymbol().equals(JSONSymbol.openingCurlyBraceSymbol)) {
            return getCommaInObjectSuccessor(state, nextSymbol);
        }

        final Set<PairSourceToReached> sourceToSuccessorLocations = new HashSet<>();
        for (final PairSourceToReached sourceToReachedLocation : state.getSourceToReachedLocations()) {
            final Location reachedAfterTransition = automaton
                    .getInternalSuccessor(sourceToReachedLocation.getReachedLocation(), currentIntSymbol);
            if (reachedAfterTransition != null) {
                sourceToSuccessorLocations.add(sourceToReachedLocation.transitionToReached(reachedAfterTransition));
            }
        }

        if (sourceToSuccessorLocations.isEmpty()) {
            return null;
        }
        return new ValidationState(sourceToSuccessorLocations, state.getStack());
    }

    private ValidationState getCommaInObjectSuccessor(ValidationState state,
            JSONSymbol nextSymbol) {
        final ValidationStackContents currentStack = state.getStack();
        final JSONSymbol currentKey = currentStack.getCurrentKey();

        graph.markNodesToReject(state.getSourceToReachedLocations(), currentKey);

        if (!currentStack.addKey(nextSymbol)) {
            return null;
        }

        final Set<Location> successorLocations = graph.getLocationsReadingKey(nextSymbol);
        if (successorLocations.isEmpty()) {
            return null;
        }
        return new ValidationState(PairSourceToReached.getIdentityPairs(successorLocations), currentStack);
    }

    private ValidationState getCallSuccessor(ValidationState state, JSONSymbol currentCallSymbol,
            JSONSymbol nextSymbol) {
        final Set<PairSourceToReached> sourceToReachedLocations = state.getSourceToReachedLocations();
        final ValidationStackContents currentStack = state.getStack();
        final ValidationStackContents newStack = ValidationStackContents
                .push(sourceToReachedLocations, currentCallSymbol, currentStack);

        final Set<PairSourceToReached> successorSourceToReachedLocations;
        if (currentCallSymbol.equals(JSONSymbol.openingCurlyBraceSymbol)) {
            successorSourceToReachedLocations = PairSourceToReached
                    .getIdentityPairs(graph.getLocationsReadingKey(nextSymbol));
            newStack.addKey(nextSymbol);
        } else {
            successorSourceToReachedLocations = new HashSet<>();
            successorSourceToReachedLocations
                    .add(PairSourceToReached.of(automaton.getInitialLocation(), automaton.getInitialLocation()));
        }

        graph.addLayerInStack();

        if (successorSourceToReachedLocations.isEmpty()) {
            return null;
        }

        return new ValidationState(successorSourceToReachedLocations, newStack);
    }

    private ValidationState getReturnSuccessor(ValidationState state, JSONSymbol retSymbol) {
        final ValidationStackContents currentStack = state.getStack();
        if (currentStack == null) {
            return null;
        }

        final Set<PairSourceToReached> sourceToReachedLocationsBeforeCall = currentStack
                .peekSourceToReachedLocationsBeforeCall();
        final JSONSymbol callSymbol = currentStack.peekCallSymbol();

        final Set<PairSourceToReached> sourceToReachedLocations = state.getSourceToReachedLocations();

        final Set<PairSourceToReached> successorSourceToReachedLocations = new HashSet<>();

        if (retSymbol.equals(JSONSymbol.closingBracketSymbol)) {
            if (!callSymbol.equals(JSONSymbol.openingBracketSymbol)) {
                return null;
            }

            for (final PairSourceToReached sourceToReachedBeforeCall : sourceToReachedLocationsBeforeCall) {
                final int stackSymbol = automaton.encodeStackSym(sourceToReachedBeforeCall.getReachedLocation(),
                        callSymbol);
                for (final PairSourceToReached currentSourceToReached : state.getSourceToReachedLocations()) {
                    final Location target = automaton.getReturnSuccessor(currentSourceToReached.getReachedLocation(),
                            retSymbol, stackSymbol);
                    if (target != null) {
                        successorSourceToReachedLocations.add(sourceToReachedBeforeCall.transitionToReached(target));
                    }
                }
            }
        } else if (retSymbol.equals(JSONSymbol.closingCurlyBraceSymbol)) {
            if (!callSymbol.equals(JSONSymbol.openingCurlyBraceSymbol)) {
                return null;
            }
            final JSONSymbol currentKey = currentStack.getCurrentKey();
            graph.markNodesToReject(sourceToReachedLocations, currentKey);

            final Set<Location> acceptingLocations = graph
                    .getLocationsWithReturnTransitionOnUnmarkedPathsWithAllKeysSeen(
                            currentStack.getSeenKeys(), currentStack.peekReachedLocationsBeforeCall());

            for (final PairSourceToReached sourceToReachedBeforeCall : sourceToReachedLocationsBeforeCall) {
                final int stackSymbol = automaton.encodeStackSym(sourceToReachedBeforeCall.getReachedLocation(),
                        callSymbol);
                for (final Location beforeReturnLocation : acceptingLocations) {
                    final Location target = automaton.getReturnSuccessor(beforeReturnLocation, retSymbol, stackSymbol);
                    if (target != null) {
                        successorSourceToReachedLocations.add(sourceToReachedBeforeCall.transitionToReached(target));
                    }
                }
            }
        } else {
            return null;
        }

        graph.popLayerInStack();

        if (successorSourceToReachedLocations.isEmpty()) {
            return null;
        }
        return new ValidationState(successorSourceToReachedLocations, currentStack.pop());
    }

}
