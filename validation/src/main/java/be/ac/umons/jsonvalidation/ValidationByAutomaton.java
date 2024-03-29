/*
 * ValidatingJSONDocumentsWithLearnedVPA - Learning a visibly pushdown automaton
 * from a JSON schema, and using it to validate JSON documents.
 *
 * Copyright 2022 University of Mons, University of Antwerp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.ac.umons.jsonvalidation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Stopwatch;

import be.ac.umons.jsonvalidation.graph.KeyGraph;
import be.ac.umons.jsonvalidation.graph.NodeInGraph;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * An automaton-like object that checks whether a JSON document satisfies a JSON
 * schema using an automaton, no matter the key-value pairs order.
 *
 * <p>
 * It is constructed from a visibly pushdown automaton (VPA). Such an automaton
 * can be constructed by hand or learned through an active learning algorithm
 * (see the learning module in the repository).
 * </p>
 * 
 * <p>
 * A JSON document is accepted if there is a path from the initial location with
 * empty stack to an accepting location with empty stack.
 * </p>
 * 
 * <p>
 * Due to the permutations, it is not possible in general to know the exact
 * current locations in the VPA. Therefore, the permutation automaton performs a
 * kind of subset construction. This means that the permutation automaton is
 * deterministic, i.e., it is never required to backtrack while reading a
 * document.
 * </p>
 * 
 * @author Gaëtan Staquet
 */
public class ValidationByAutomaton<L> {
    private final KeyGraph<L> graph;
    private final OneSEVPA<L, JSONSymbol> automaton;
    private final VPDAlphabet<JSONSymbol> alphabet;
    private long maxTimePathsKeyGraph = 0;
    private long totalTimePathsKeyGraph = 0;
    private long numberPathsKeyGraph = 0;
    private long maxTimeSuccessorObject = 0;
    private long totalTimeSuccessorObject = 0;
    private long numberSuccessorObject = 0;
    private long maxTimeSuccessorArray = 0;
    private long totalTimeSuccessorArray = 0;
    private long numberSuccessorArray = 0;

    public ValidationByAutomaton(final OneSEVPA<L, JSONSymbol> automaton) {
        this(automaton, KeyGraph.graphFor(automaton, false));
    }

    public ValidationByAutomaton(final OneSEVPA<L, JSONSymbol> automaton, KeyGraph<L> graph) {
        if (!graph.isValid()) {
            throw new RuntimeException("The key graph is cyclic");
        }
        this.graph = graph;
        this.automaton = automaton;
        this.alphabet = automaton.getInputAlphabet();
    }

    public ValidationState<L> getInitialState() {
        final Set<L> setWithInitialLocation = new LinkedHashSet<>();
        setWithInitialLocation.add(automaton.getInitialLocation());
        return new ValidationState<>(PairSourceToReached.getIdentityPairs(setWithInitialLocation), null);
    }

    public boolean isAccepting(ValidationState<L> state) {
        if (state == null || state.getStack() != null) {
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

    public long getMaximalTimePathsKeyGraph() {
        return maxTimePathsKeyGraph;
    }

    public long getNumberOfTimesPathsKeyGraphComputed() {
        return numberPathsKeyGraph;
    }

    public long getTotalTimePathsKeyGraph() {
        return totalTimePathsKeyGraph;
    }

    public long getMaximalTimeSuccessorObject() {
        return maxTimeSuccessorObject;
    }

    public long getNumberOfTimesSuccessorObject() {
        return numberSuccessorObject;
    }

    public long getTotalTimeSuccessorObject() {
        return totalTimeSuccessorObject;
    }

    public long getMaximalTimeSuccessorArray() {
        return maxTimeSuccessorArray;
    }

    public long getNumberOfTimesSuccessorArray() {
        return numberSuccessorArray;
    }

    public long getTotalTimeSuccessorArray() {
        return totalTimeSuccessorArray;
    }

    public void resetTimeAndNumber() {
        totalTimePathsKeyGraph = totalTimeSuccessorArray = totalTimeSuccessorObject = 0;
        maxTimePathsKeyGraph = maxTimeSuccessorArray = maxTimeSuccessorObject = 0;
        numberPathsKeyGraph = numberSuccessorArray = numberSuccessorObject = 0;
    }

    public boolean accepts(List<JSONSymbol> input) {
        if (input.isEmpty() || !input.get(0).equals(JSONSymbol.openingCurlyBraceSymbol)) {
            return false;
        }
        return isAccepting(getState(input));
    }

    public boolean accepts(Word<JSONSymbol> input) {
        if (input.isEmpty() || !input.getSymbol(0).equals(JSONSymbol.openingCurlyBraceSymbol)) {
            return false;
        }
        return isAccepting(getState(input));
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

        final Set<PairSourceToReached<L>> sourceToSuccessorLocations = new LinkedHashSet<>();
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
        final JSONSymbol currentKey = currentStack.peekCurrentKey();

        markNodesToReject(currentStack, state.getSourceToReachedLocations(), currentKey);

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
        if (currentCallSymbol.equals(JSONSymbol.openingCurlyBraceSymbol)
                && !nextSymbol.equals(JSONSymbol.closingCurlyBraceSymbol)) {
            successorSourceToReachedLocations = PairSourceToReached
                    .getIdentityPairs(graph.getLocationsReadingKey(nextSymbol));
            newStack.addKey(nextSymbol);
        } else {
            successorSourceToReachedLocations = new LinkedHashSet<>();
            successorSourceToReachedLocations
                    .add(PairSourceToReached.of(automaton.getInitialLocation(), automaton.getInitialLocation()));
        }

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

        final Set<PairSourceToReached<L>> successorSourceToReachedLocations = new LinkedHashSet<>();

        if (retSymbol.equals(JSONSymbol.closingCurlyBraceSymbol) && currentStack.peekCurrentKey() != null) {
            if (!callSymbol.equals(JSONSymbol.openingCurlyBraceSymbol)) {
                return null;
            }
            final JSONSymbol currentKey = currentStack.peekCurrentKey();
            markNodesToReject(currentStack, sourceToReachedLocations, currentKey);

            final Stopwatch watch = Stopwatch.createStarted();
            final Set<L> acceptingLocations = graph.getLocationsWithReturnTransitionOnUnmarkedPathsWithAllKeysSeen(
                    currentStack.peekSeenKeys(), currentStack.peekReachedLocationsBeforeCall(),
                    currentStack.peekRejectedNodes());
            long time = watch.stop().elapsed().toMillis();
            maxTimePathsKeyGraph = Math.max(time, maxTimePathsKeyGraph);
            totalTimePathsKeyGraph += time;
            numberPathsKeyGraph++;

            watch.reset().start();
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
            time = watch.stop().elapsed().toMillis();
            maxTimeSuccessorObject = Math.max(time, maxTimeSuccessorObject);
            totalTimeSuccessorObject += time;
            numberSuccessorObject++;
        } else if (retSymbol.equals(JSONSymbol.closingBracketSymbol)
                || (retSymbol.equals(JSONSymbol.closingCurlyBraceSymbol) && currentStack.peekCurrentKey() == null)) {
            if (retSymbol.equals(JSONSymbol.closingBracketSymbol)
                    && !callSymbol.equals(JSONSymbol.openingBracketSymbol)) {
                return null;
            }
            if (retSymbol.equals(JSONSymbol.closingCurlyBraceSymbol)
                    && !callSymbol.equals(JSONSymbol.openingCurlyBraceSymbol)) {
                return null;
            }

            final Stopwatch watch = Stopwatch.createStarted();
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
            final long time = watch.stop().elapsed().toMillis();
            maxTimeSuccessorArray = Math.max(time, maxTimeSuccessorArray);
            totalTimeSuccessorArray += time;
            numberSuccessorArray++;
        } else {
            return null;
        }

        if (successorSourceToReachedLocations.isEmpty()) {
            return null;
        }
        return new ValidationState<>(successorSourceToReachedLocations, currentStack.pop());
    }

    private void markNodesToReject(final ValidationStackContents<L> topStack,
            final Collection<PairSourceToReached<L>> sourceToReachedLocations, final JSONSymbol lastKeyProcessed) {
        final Collection<NodeInGraph<L>> nodesForKey = graph.getNodesForKey(lastKeyProcessed);

        for (NodeInGraph<L> node : nodesForKey) {
            if (!sourceToReachedLocations.contains(node.getPairLocations())) {
                topStack.markRejected(node);
            }
        }
    }
}
