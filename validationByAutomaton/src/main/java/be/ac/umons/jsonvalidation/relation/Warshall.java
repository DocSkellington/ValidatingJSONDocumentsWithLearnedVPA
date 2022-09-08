package be.ac.umons.jsonvalidation.relation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

class Warshall {
    public static <L> boolean warshall(final ReachabilityRelation<L> relation, final Collection<L> locations, final boolean computeWitnesses) {
        final ReachabilityRelation<L> newRelation = new ReachabilityRelation<>();
        for (final L pivot : locations) {
            for (final L start : locations) {
                for (final L target : locations) {
                    if (relation.areInRelation(start, pivot) && relation.areInRelation(pivot, target)) {
                        final InfoInRelation<L> startToPivot = relation.getCell(start, pivot);
                        final InfoInRelation<L> pivotToTarget = relation.getCell(pivot, target);

                        Word<JSONSymbol> witness = relation.getWitness(start, target);
                        if (witness == null) {
                            witness = constructWitness(startToPivot.getWitness(), pivotToTarget.getWitness(), computeWitnesses);
                        }

                        final Set<L> seenLocations = new LinkedHashSet<>();
                        seenLocations.addAll(startToPivot.getLocationsBetweenStartAndTarget());
                        seenLocations.addAll(pivotToTarget.getLocationsBetweenStartAndTarget());

                        newRelation.add(start, target, witness, seenLocations);
                    }
                }
            }
        }
        return relation.addAll(newRelation);
    }

    private static Word<JSONSymbol> constructWitness(Word<JSONSymbol> witnessFromStartToMid, Word<JSONSymbol> witnessFromMidToTarget, boolean computeWitnesses) {
        if (computeWitnesses) {
            return witnessFromStartToMid.concat(witnessFromMidToTarget);
        }
        else {
            return null;
        }
    }
}
