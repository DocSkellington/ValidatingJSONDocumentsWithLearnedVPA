package be.ac.umons.jsonvalidation.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

class ReachabilityMatrix<L, C> implements Iterable<C> {
    private final Map<L, Map<L, C>> matrix = new LinkedHashMap<>();

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
    public C getCell(final L start, final L target) {
        final Map<L, C> row = matrix.get(start);
        if (row == null) {
            return null;
        }
        return row.get(target);
    }

    public int size() {
        // @formatter:off
        return matrix.values().stream()
            .mapToInt(map -> map.size())
            .sum();
        // @formatter:on
    }

    public Collection<C> getLocationsAndInfoInRelationWithStart(final L start) {
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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof ReachabilityMatrix)) {
            return false;
        }

        ReachabilityMatrix<?,?> other = (ReachabilityMatrix<?,?>)obj;
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
