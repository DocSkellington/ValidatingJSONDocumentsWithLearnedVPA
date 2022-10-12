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

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The current state in an execution of {@link ValidationByAutomaton}.
 * 
 * <p>
 * It contains a set with the source and reached locations in the VPA and a stack.
 * </p>
 * 
 * @author Gaëtan Staquet
 */
public class ValidationState<L> {
    private final Set<PairSourceToReached<L>> sourceToReachedLocations;
    private final ValidationStackContents<L> stack;

    public ValidationState(final Set<PairSourceToReached<L>> sourceToReachedLocations, final ValidationStackContents<L> stack) {
        this.sourceToReachedLocations = sourceToReachedLocations;
        this.stack = stack;
    }

    public Set<PairSourceToReached<L>> getSourceToReachedLocations() {
        return sourceToReachedLocations;
    }

    public Set<L> getReachedLocations() {
        // @formatter:off
        return sourceToReachedLocations.stream()
            .map(pair -> pair.getReachedLocation())
            .collect(Collectors.toSet());
        // @formatter:on
    }

    public ValidationStackContents<L> getStack() {
        return stack;
    }
}
