package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

class ReachabilityMatrix<L> implements Iterable<InfoInRelation<L>> {
    private final Map<L, Map<L, InfoInRelation<L>>> matrix = new LinkedHashMap<>();

    public boolean areInRelation(final L start, final L target) {
        return matrix.containsKey(start) && matrix.get(start).containsKey(target);
    }

    @Nullable
    public Word<JSONSymbol> getWitness(final L start, final L target) {
        final Map<L, InfoInRelation<L>> row = matrix.get(start);
        if (row == null) {
            return null;
        }
        final InfoInRelation<L> cell = row.get(target);
        if (cell == null) {
            return null;
        }
        return cell.getWitness();
    }

    public int size() {
        // @formatter:off
        return matrix.values().stream()
            .mapToInt(map -> map.size())
            .sum();
        // @formatter:on
    }

    public boolean add(final L start, final L target, final Word<JSONSymbol> witness) {
        final Set<L> locationsBetweenStartAndTarget = new LinkedHashSet<>();
        locationsBetweenStartAndTarget.add(start);
        locationsBetweenStartAndTarget.add(target);
        return add(start, target, witness, locationsBetweenStartAndTarget);
    }

    public boolean add(final L start, final L target, final Word<JSONSymbol> witness, final Set<L> locationsBetweenStartAndTarget) {
        return add(start, target, new InfoInRelation<>(start, target, witness, locationsBetweenStartAndTarget));
    }

    public boolean add(final L start, final L target, final InfoInRelation<L> infoInRelation) {
        if (matrix.containsKey(start)) {
            if (matrix.get(start).containsKey(target)) {
                return matrix.get(start).get(target).addSeenLocations(infoInRelation.getLocationsBetweenStartAndTarget());
            }
            else {
                matrix.get(start).put(target, infoInRelation);
                return true;
            }
        }
        else {
            matrix.put(start, new LinkedHashMap<>());
            matrix.get(start).put(target, infoInRelation);
            return true;
        }
    }

    public boolean addAll(final ReachabilityMatrix<L> matrix) {
        boolean change = false;
        for (final Map.Entry<L, Map<L, InfoInRelation<L>>> startToEntry : matrix.matrix.entrySet()) {
            for (final Map.Entry<L, InfoInRelation<L>> targetToInfo : startToEntry.getValue().entrySet()) {
                change = this.add(startToEntry.getKey(), targetToInfo.getKey(), targetToInfo.getValue()) || change;
            }
        }
        return change;
    }

    public Collection<InfoInRelation<L>> getLocationsAndInfoInRelationWith(final L start) {
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

        ReachabilityMatrix<?> other = (ReachabilityMatrix<?>)obj;
        return Objects.equals(other.matrix, this.matrix);
    }

    private class IteratorOverReachabilityMatrix implements Iterator<InfoInRelation<L>> {
        private Iterator<L> rowIterator;
        private Iterator<InfoInRelation<L>> cellIterator;

        public IteratorOverReachabilityMatrix() {
            this.rowIterator = matrix.keySet().iterator();
            hasNext();
        }

        @Override
        public boolean hasNext() {
            if (cellIterator.hasNext()) {
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
        public InfoInRelation<L> next() {
            return cellIterator.next();
        }
    }

    @Override
    public Iterator<InfoInRelation<L>> iterator() {
        return new IteratorOverReachabilityMatrix();
    }
}
