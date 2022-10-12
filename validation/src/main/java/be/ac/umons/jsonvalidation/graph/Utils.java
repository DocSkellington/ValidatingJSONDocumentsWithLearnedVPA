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
import java.util.LinkedHashMap;
import java.util.Map;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Word;

/**
 * Utilities for the relations and the graph.
 * 
 * @author Gaëtan Staquet
 */
class Utils {
    private Utils() {
    }

    /**
     * Computes the transitive closure of the reachability relation using Warshall's
     * algorithm.
     * 
     * @param <L>              Location type
     * @param relation         The relation
     * @param locations        The locations of the VPA
     * @param computeWitnesses Whether to compute the witness
     * @return True if and only if at least one new element was added to the
     *         relation
     */
    public static <L> boolean warshall(final ReachabilityRelation<L> relation, final Collection<L> locations,
            boolean computeWitnesses) {
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
                            } else {
                                witness = relation.getWitness(start, target);
                            }
                        } else {
                            witness = null;
                        }

                        newRelation.add(start, target, witness);
                    }
                }
            }
        }
        return relation.addAll(newRelation);
    }

    /**
     * Creates a map that takes into input a location of the previous hypothesis and
     * returns the corresponding location in the new location.
     * 
     * @param <L1>               The previous hypothesis' location type
     * @param <L2>               The current hypothesis' location type
     * @param previousHypothesis The previous hypothesis
     * @param currentHypothesis  The current hypothesis
     * @return The map
     */
    public static <L1, L2> Map<L1, L2> createMapLocationsOfPreviousToCurrent(
            OneSEVPA<L1, JSONSymbol> previousHypothesis, OneSEVPA<L2, JSONSymbol> currentHypothesis) {
        final Map<L1, L2> previousToCurrentLocations = new LinkedHashMap<>();
        for (final L1 locationInPrevious : previousHypothesis.getLocations()) {
            for (final L2 locationInCurrent : currentHypothesis.getLocations()) {
                if (previousHypothesis.getLocationId(locationInPrevious) == currentHypothesis
                        .getLocationId(locationInCurrent)) {
                    previousToCurrentLocations.put(locationInPrevious, locationInCurrent);
                }
            }
        }

        return previousToCurrentLocations;
    }

}
