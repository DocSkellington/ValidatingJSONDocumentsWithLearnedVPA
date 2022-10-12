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

import java.util.BitSet;
import java.util.Objects;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.PairSourceToReached;
import net.automatalib.automata.vpda.OneSEVPA;

/**
 * A node in a {@link KeyGraph}.
 * 
 * It is labeled by a triplet from the reachability relation. For each location
 * {@code p} in the VPA, it has a boolean indicating whether the target state of
 * the triplet can read a return symbol and pop the stack symbol corresponding
 * to {@code p}.
 * 
 * @param <L> Location type
 * @author Gaëtan Staquet
 */
public class NodeInGraph<L> {

    private final PairSourceToReached<L> pairLocations;
    private final JSONSymbol symbol;
    private final BitSet acceptingForLocation;
    private final BitSet onPathToAcceptingForLocation;

    public NodeInGraph(final L startLocation, final L targetLocation, final JSONSymbol symbol,
            final OneSEVPA<L, JSONSymbol> automaton, final L binLocation) {
        this.pairLocations = PairSourceToReached.of(startLocation, targetLocation);
        this.symbol = symbol;
        this.acceptingForLocation = new BitSet(automaton.size());
        this.onPathToAcceptingForLocation = new BitSet(automaton.size());

        final JSONSymbol callSymbol = JSONSymbol.openingCurlyBraceSymbol;
        final JSONSymbol returnSymbol = JSONSymbol.closingCurlyBraceSymbol;

        for (int i = 0; i < automaton.size(); i++) {
            // If the location before the call or the location after the return are the bin
            // locations, we ignore them as we do not want the bin state in the graph
            final L locationBeforeCall = automaton.getLocation(i);
            if (Objects.equals(locationBeforeCall, binLocation)) {
                continue;
            }

            final int stackSym = automaton.encodeStackSym(locationBeforeCall, callSymbol);
            final L locationAfterReturn = automaton.getReturnSuccessor(targetLocation, returnSymbol, stackSym);
            if (locationAfterReturn != null && !(Objects.equals(locationAfterReturn, binLocation))) {
                acceptingForLocation.set(i);
                onPathToAcceptingForLocation.set(i);
            }
        }
    }

    public JSONSymbol getSymbol() {
        return symbol;
    }

    public L getStartLocation() {
        return pairLocations.getSourceLocation();
    }

    public L getTargetLocation() {
        return pairLocations.getReachedLocation();
    }

    public PairSourceToReached<L> getPairLocations() {
        return pairLocations;
    }

    public boolean isAcceptingForLocation(int locationId) {
        return acceptingForLocation.get(locationId);
    }

    public boolean isOnPathToAcceptingForLocation(int locationId) {
        return onPathToAcceptingForLocation.get(locationId);
    }

    void setOnPathToAcceptingLocation(int locationId) {
        onPathToAcceptingForLocation.set(locationId);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NodeInGraph)) {
            return false;
        }
        final NodeInGraph<?> other = (NodeInGraph<?>) obj;
        return Objects.equals(this.pairLocations, other.pairLocations) && Objects.equals(this.symbol, other.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pairLocations, this.symbol);
    }

    @Override
    public String toString() {
        return pairLocations.toString() + ", " + symbol;
    }
}
