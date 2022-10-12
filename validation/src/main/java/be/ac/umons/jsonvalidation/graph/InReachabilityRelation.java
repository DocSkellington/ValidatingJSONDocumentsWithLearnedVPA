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
 * Stores two locations such that it is possible to go from the first location
 * to the second, alongside a witness of that fact.
 * 
 * @param <L> Location type
 * @author Gaëtan Staquet
 */
class InReachabilityRelation<L> {
    private final L start, target;
    private final Word<JSONSymbol> witness;

    public InReachabilityRelation(final L start, final L target, final Word<JSONSymbol> witness) {
        this.start = start;
        this.target = target;
        this.witness = witness;
    }

    public L getStart() {
        return start;
    }

    public L getTarget() {
        return target;
    }

    public Word<JSONSymbol> getWitness() {
        return witness;
    }

    @Override
    public String toString() {
        return "(" + witness + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, target, witness);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof InReachabilityRelation)) {
            return false;
        }

        final InReachabilityRelation<?> other = (InReachabilityRelation<?>) obj;
        return Objects.equals(other.witness, this.witness) && Objects.equals(other.start, this.start)
                && Objects.equals(other.target, this.target);
    }
}
