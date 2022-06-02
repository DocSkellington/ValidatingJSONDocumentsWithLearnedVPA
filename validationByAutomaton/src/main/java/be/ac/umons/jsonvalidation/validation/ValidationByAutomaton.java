package be.ac.umons.jsonvalidation.validation;

import java.util.HashSet;
import java.util.Set;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.validation.relation.ReachabilityGraph;
import net.automatalib.automata.vpda.OneSEVPA;
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
public class ValidationByAutomaton<L> {
    private final ReachabilityGraph<L> graph;
    private final OneSEVPA<L, JSONSymbol> automaton;
    private final VPDAlphabet<JSONSymbol> alphabet;

    public ValidationByAutomaton(final OneSEVPA<L, JSONSymbol> automaton) {
        this(automaton, ReachabilityGraph.graphFor(automaton));
    }

    public ValidationByAutomaton(final OneSEVPA<L, JSONSymbol> automaton, ReachabilityGraph<L> graph) {
        this.graph = graph;
        this.automaton = automaton;
        this.alphabet = automaton.getInputAlphabet();
    }

    private ValidationState<L> getInitialState() {
        final Set<L> setWithInitialLocation = new HashSet<>();
        setWithInitialLocation.add(automaton.getInitialLocation());
        return new ValidationState<>(PairSourceToReached.getIdentityPairs(setWithInitialLocation), null);
    }

    public boolean accepts(Iterable<JSONSymbol> input) {
        ValidationState<L> state = getState(input);
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
            .filter(location -> automaton.isAcceptingLocation(location))
            .findAny().isPresent();
        // @formatter:on
    }

    public ValidationState<L> getState(Iterable<JSONSymbol> input) {
        ValidationState<L> state = getInitialState();
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

    public ValidationState<L> getSuccessor(ValidationState<L> state, JSONSymbol currentSymbol,
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

    private ValidationState<L> getInternalSuccessor(ValidationState<L> state, JSONSymbol currentIntSymbol,
            JSONSymbol nextSymbol) {
        if (currentIntSymbol.equals(JSONSymbol.commaSymbol)
                && state.getStack().peekCallSymbol().equals(JSONSymbol.openingCurlyBraceSymbol)) {
            return getCommaInObjectSuccessor(state, nextSymbol);
        }

        final Set<PairSourceToReached<L>> sourceToSuccessorLocations = new HashSet<>();
        for (final PairSourceToReached<L> sourceToReachedLocation : state.getSourceToReachedLocations()) {
            final L reachedAfterTransition = automaton
                    .getInternalSuccessor(sourceToReachedLocation.getReachedLocation(), currentIntSymbol);
            if (reachedAfterTransition != null) {
                sourceToSuccessorLocations.add(sourceToReachedLocation.transitionToReached(reachedAfterTransition));
            }
        }

        if (sourceToSuccessorLocations.isEmpty()) {
            return null;
        }
        return new ValidationState<>(sourceToSuccessorLocations, state.getStack());
    }

    private ValidationState<L> getCommaInObjectSuccessor(ValidationState<L> state,
            JSONSymbol nextSymbol) {
        final ValidationStackContents<L> currentStack = state.getStack();
        final JSONSymbol currentKey = currentStack.getCurrentKey();

        graph.markNodesToReject(state.getSourceToReachedLocations(), currentKey);

        if (!currentStack.addKey(nextSymbol)) {
            return null;
        }

        final Set<L> successorLocations = graph.getLocationsReadingKey(nextSymbol);
        if (successorLocations.isEmpty()) {
            return null;
        }
        return new ValidationState<>(PairSourceToReached.getIdentityPairs(successorLocations), currentStack);
    }

    private ValidationState<L> getCallSuccessor(ValidationState<L> state, JSONSymbol currentCallSymbol,
            JSONSymbol nextSymbol) {
        final Set<PairSourceToReached<L>> sourceToReachedLocations = state.getSourceToReachedLocations();
        final ValidationStackContents<L> currentStack = state.getStack();
        final ValidationStackContents<L> newStack = ValidationStackContents
                .push(sourceToReachedLocations, currentCallSymbol, currentStack);

        final Set<PairSourceToReached<L>> successorSourceToReachedLocations;
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

        return new ValidationState<>(successorSourceToReachedLocations, newStack);
    }

    private ValidationState<L> getReturnSuccessor(ValidationState<L> state, JSONSymbol retSymbol) {
        final ValidationStackContents<L> currentStack = state.getStack();
        if (currentStack == null) {
            return null;
        }

        final Set<PairSourceToReached<L>> sourceToReachedLocationsBeforeCall = currentStack
                .peekSourceToReachedLocationsBeforeCall();
        final JSONSymbol callSymbol = currentStack.peekCallSymbol();

        final Set<PairSourceToReached<L>> sourceToReachedLocations = state.getSourceToReachedLocations();

        final Set<PairSourceToReached<L>> successorSourceToReachedLocations = new HashSet<>();

        if (retSymbol.equals(JSONSymbol.closingBracketSymbol)) {
            if (!callSymbol.equals(JSONSymbol.openingBracketSymbol)) {
                return null;
            }

            for (final PairSourceToReached<L> sourceToReachedBeforeCall : sourceToReachedLocationsBeforeCall) {
                final int stackSymbol = automaton.encodeStackSym(sourceToReachedBeforeCall.getReachedLocation(),
                        callSymbol);
                for (final PairSourceToReached<L> currentSourceToReached : state.getSourceToReachedLocations()) {
                    final L target = automaton.getReturnSuccessor(currentSourceToReached.getReachedLocation(),
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

            final Set<L> acceptingLocations = graph
                    .getLocationsWithReturnTransitionOnUnmarkedPathsWithAllKeysSeen(
                            currentStack.getSeenKeys(), currentStack.peekReachedLocationsBeforeCall());

            for (final PairSourceToReached<L> sourceToReachedBeforeCall : sourceToReachedLocationsBeforeCall) {
                final int stackSymbol = automaton.encodeStackSym(sourceToReachedBeforeCall.getReachedLocation(),
                        callSymbol);
                for (final L beforeReturnLocation : acceptingLocations) {
                    final L target = automaton.getReturnSuccessor(beforeReturnLocation, retSymbol, stackSymbol);
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
        return new ValidationState<>(successorSourceToReachedLocations, currentStack.pop());
    }

}