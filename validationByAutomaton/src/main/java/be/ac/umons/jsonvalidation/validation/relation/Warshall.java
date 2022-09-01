package be.ac.umons.jsonvalidation.validation.relation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

class Warshall {
    public static <L> boolean warshall(final ReachabilityRelation<L> relation, final Collection<L> locations, final boolean computeWitnesses) {
        return warshall(relation.getMatrix(), locations, computeWitnesses);
    }

    private static <L> boolean warshall(final ReachabilityMatrix<L> matrix, final Collection<L> locations, final boolean computeWitnesses) {
        final ReachabilityMatrix<L> newMatrix = new ReachabilityMatrix<>();
        for (final L pivot : locations) {
            for (final L start : locations) {
                for (final L target : locations) {
                    if (matrix.areInRelation(start, pivot) && matrix.areInRelation(pivot, target)) {
                        final InfoInRelation<L> startToPivot = matrix.getInfoInRelation(start, pivot);
                        final InfoInRelation<L> pivotToTarget = matrix.getInfoInRelation(pivot, target);

                        Word<JSONSymbol> witness = matrix.getWitness(start, target);
                        if (witness == null) {
                            witness = ReachabilityRelation.constructWitness(startToPivot.getWitness(), pivotToTarget.getWitness(), computeWitnesses);
                        }

                        final Set<L> seenLocations = new LinkedHashSet<>();
                        seenLocations.addAll(startToPivot.getLocationsBetweenStartAndTarget());
                        seenLocations.addAll(pivotToTarget.getLocationsBetweenStartAndTarget());

                        newMatrix.add(start, target, witness, seenLocations);
                    }
                }
            }
        }
        return matrix.addAll(newMatrix);
    }
}
