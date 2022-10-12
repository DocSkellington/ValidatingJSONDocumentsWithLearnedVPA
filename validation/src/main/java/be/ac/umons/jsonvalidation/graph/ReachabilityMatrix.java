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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * A matrix for {@link ReachabilityRelation} and
 * {@link OnAcceptingPathRelation}.
 * 
 * @param <L> Location type
 * @param <C> Type of the data stored in the matrix
 * @author Gaëtan Staquet
 */
class ReachabilityMatrix<L, C> implements Iterable<C> {
    private final Map<L, Map<L, C>> matrix = new LinkedHashMap<>();

    /**
     * Tests whether the two locations are in relation.
     * 
     * @param start  The first location
     * @param target The second location
     * @return True if and only if start and target are in relation
     */
    public boolean areInRelation(final L start, final L target) {
        return getCell(start, target) != null;
    }

    private void createRowFor(final L start) {
        if (!matrix.containsKey(start)) {
            matrix.put(start, new LinkedHashMap<>());
        }
    }

    protected void set(final L start, final L target, final C cell) {
        if (!matrix.containsKey(start)) {
            createRowFor(start);
        }
        matrix.get(start).put(target, cell);
    }

    @Nullable
    C getCell(final L start, final L target) {
        final Map<L, C> row = matrix.get(start);
        if (row == null) {
            return null;
        }
        return row.get(target);
    }

    /**
     * Returns the size of the relation, i.e., the number of cells in the matrix.
     * 
     * @return The size
     */
    public int size() {
        // @formatter:off
        return matrix.values().stream()
            .mapToInt(map -> map.size())
            .sum();
        // @formatter:on
    }

    Collection<C> getLocationsAndInfoInRelationWithStart(final L start) {
        return matrix.get(start).values();
    }

    @Override
    public String toString() {
        return matrix.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(matrix);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof ReachabilityMatrix)) {
            return false;
        }

        final ReachabilityMatrix<?, ?> other = (ReachabilityMatrix<?, ?>) obj;
        return Objects.equals(other.matrix, this.matrix);
    }

    private class IteratorOverReachabilityMatrix implements Iterator<C> {
        private Iterator<L> rowIterator;
        private Iterator<C> cellIterator;

        public IteratorOverReachabilityMatrix() {
            this.rowIterator = matrix.keySet().iterator();
            hasNext();
        }

        @Override
        public boolean hasNext() {
            if (cellIterator != null && cellIterator.hasNext()) {
                return true;
            }
            if (rowIterator.hasNext()) {
                L nextRow = rowIterator.next();
                cellIterator = matrix.get(nextRow).values().iterator();
                return true;
            }
            return false;
        }

        @Override
        public C next() {
            return cellIterator.next();
        }
    }

    @Override
    public Iterator<C> iterator() {
        return new IteratorOverReachabilityMatrix();
    }
}
