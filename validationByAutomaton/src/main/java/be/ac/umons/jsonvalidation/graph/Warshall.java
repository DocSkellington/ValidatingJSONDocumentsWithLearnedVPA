package be.ac.umons.jsonvalidation.graph;

import java.util.Collection;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.words.Word;

class Warshall {
    public static <L> boolean warshall(final ReachabilityRelation<L> relation, final Collection<L> locations, boolean computeWitnesses) {
        final ReachabilityRelation<L> newRelation = new ReachabilityRelation<>();
        for (final L pivot : locations) {
            for (final L start : locations) {
                for (final L target : locations) {
                    if (relation.areInRelation(start, pivot) && relation.areInRelation(pivot, target)) {
                        final InfoInRelation<L> startToPivot = relation.getCell(start, pivot);
                        final InfoInRelation<L> pivotToTarget = relation.getCell(pivot, target);

                        final Word<JSONSymbol> witness;
                        if (computeWitnesses) {
                            if (relation.getWitness(start, target) == null) {
                                witness = startToPivot.getWitness().concat(pivotToTarget.getWitness());
                            }
                            else {
                                witness = relation.getWitness(start, target);
                            }
                        }
                        else {
                            witness = null;
                        }

                        newRelation.add(start, target, witness);
                    }
                }
            }
        }
        return relation.addAll(newRelation);
    }
}
