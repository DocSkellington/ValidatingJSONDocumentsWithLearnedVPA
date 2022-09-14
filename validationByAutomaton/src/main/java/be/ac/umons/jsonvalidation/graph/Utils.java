package be.ac.umons.jsonvalidation.graph;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Word;

class Utils {

    public static <L> boolean warshall(final ReachabilityRelation<L> relation, final Collection<L> locations, boolean computeWitnesses) {
        final ReachabilityRelation<L> newRelation = new ReachabilityRelation<>();
        for (final L pivot : locations) {
            for (final L start : locations) {
                for (final L target : locations) {
                    if (relation.areInRelation(start, pivot) && relation.areInRelation(pivot, target)) {
                        final InReachabilityRelation<L> startToPivot = relation.getCell(start, pivot);
                        final InReachabilityRelation<L> pivotToTarget = relation.getCell(pivot, target);

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

    public static <L1, L2> Map<L1, L2> createMapLocationsOfPreviousToCurrent(OneSEVPA<L1, JSONSymbol> previousHypothesis, OneSEVPA<L2, JSONSymbol> currentHypothesis) {
        final Map<L1, L2> previousToCurrentLocations = new LinkedHashMap<>();
        for (final L1 locationInPrevious : previousHypothesis.getLocations()) {
            for (final L2 locationInCurrent : currentHypothesis.getLocations()) {
                if (previousHypothesis.getLocationId(locationInPrevious) == currentHypothesis.getLocationId(locationInCurrent)) {
                    previousToCurrentLocations.put(locationInPrevious, locationInCurrent);
                }
            }
        }

        return previousToCurrentLocations;
    }

}
