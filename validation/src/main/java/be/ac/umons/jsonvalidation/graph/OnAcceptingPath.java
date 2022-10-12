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

package be.ac.umons.jsonvalidation.graph;

import java.util.Objects;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

/**
 * Stores a location that is on an accepting path, alongside a witness from the
 * initial location to that location, and from the location to a final location.
 * 
 * @param <L> Location type
 * @author Gaëtan Staquet
 */
class OnAcceptingPath<L> {
    private final L intermediate;
    private final Word<JSONSymbol> witnessToIntermediate;
    private final Word<JSONSymbol> witnessFromIntermediate;

    public OnAcceptingPath(final L intermediate, final Word<JSONSymbol> witnessToIntermediate,
            final Word<JSONSymbol> witnessFromIntermediate) {
        this.intermediate = intermediate;
        this.witnessToIntermediate = witnessToIntermediate;
        this.witnessFromIntermediate = witnessFromIntermediate;
    }

    public L getIntermediate() {
        return intermediate;
    }

    public Word<JSONSymbol> getWitnessToIntermediate() {
        return witnessToIntermediate;
    }

    public Word<JSONSymbol> getWitnessFromIntermediate() {
        return witnessFromIntermediate;
    }

    @Override
    public String toString() {
        return "(" + getWitnessToIntermediate() + ", " + getWitnessFromIntermediate() + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWitnessToIntermediate(), getWitnessFromIntermediate());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof OnAcceptingPath)) {
            return false;
        }

        final OnAcceptingPath<?> other = (OnAcceptingPath<?>) obj;
        return Objects.equals(other.witnessToIntermediate, this.witnessToIntermediate)
                && Objects.equals(other.witnessFromIntermediate, this.witnessFromIntermediate);
    }
}
