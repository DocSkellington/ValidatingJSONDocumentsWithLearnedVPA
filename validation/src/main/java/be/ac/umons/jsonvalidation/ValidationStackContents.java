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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.graph.NodeInGraph;

/**
 * The stack used in a {@link ValidationState}.
 * 
 * <p>
 * It contains a set with the source-to-reached locations before the call
 * symbol, the call symbol, the set with all the keys seen so far, a set of
 * nodes to reject in the graph, and a pointer to the rest of the stack.
 * </p>
 * 
 * @author Gaëtan Staquet
 */
class ValidationStackContents<L> {
    private final Set<PairSourceToReached<L>> sourceToReachedLocationsBeforeCall;
    private final JSONSymbol callSymbol;
    private final Set<JSONSymbol> seenKeys = new LinkedHashSet<>();
    private final Set<NodeInGraph<L>> rejectedNodes = new LinkedHashSet<>();
    private JSONSymbol currentKey = null;
    private @Nullable final ValidationStackContents<L> rest;

    private ValidationStackContents(final Set<PairSourceToReached<L>> sourceToReachedLocations,
            final JSONSymbol symbol, final @Nullable ValidationStackContents<L> rest) {
        this.sourceToReachedLocationsBeforeCall = sourceToReachedLocations;
        this.callSymbol = symbol;
        this.rest = rest;
    }

    public boolean addKey(JSONSymbol key) {
        currentKey = key;
        return seenKeys.add(key);
    }

    public Set<PairSourceToReached<L>> peekSourceToReachedLocationsBeforeCall() {
        return sourceToReachedLocationsBeforeCall;
    }

    public Set<L> peekReachedLocationsBeforeCall() {
        // @formatter:off
        return sourceToReachedLocationsBeforeCall.stream()
            .map(pair -> pair.getReachedLocation())
            .collect(Collectors.toSet());
        // @formatter:on
    }

    public JSONSymbol peekCallSymbol() {
        return callSymbol;
    }

    public Set<NodeInGraph<L>> peekRejectedNodes() {
        return rejectedNodes;
    }

    public void markRejected(NodeInGraph<L> node) {
        rejectedNodes.add(node);
    }

    public Set<JSONSymbol> peekSeenKeys() {
        return seenKeys;
    }

    public JSONSymbol peekCurrentKey() {
        return currentKey;
    }

    public @Nullable ValidationStackContents<L> pop() {
        return rest;
    }

    public static <L> ValidationStackContents<L> push(final Set<PairSourceToReached<L>> sourceToReachedLocations,
            final JSONSymbol symbol, final ValidationStackContents<L> rest) {
        return new ValidationStackContents<>(sourceToReachedLocations, symbol, rest);
    }
}
