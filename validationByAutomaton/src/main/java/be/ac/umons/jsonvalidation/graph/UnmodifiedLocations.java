package be.ac.umons.jsonvalidation.graph;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import be.ac.umons.jsonvalidation.JSONSymbol;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;

class UnmodifiedLocations {

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

    public static <L1, L2> Set<L1> findUnmodifiedLocations(OneSEVPA<L1, JSONSymbol> previousHypothesis, OneSEVPA<L2, JSONSymbol> currentHypothesis, Map<L1, L2> previousToCurrentLocations) {
        final Set<L1> unmodifiedLocations = new LinkedHashSet<>();
        for (final L1 locationInPrevious : previousHypothesis.getLocations()) {
            if (isLocationUnmodified(locationInPrevious, previousHypothesis, currentHypothesis, previousToCurrentLocations)) {
                unmodifiedLocations.add(locationInPrevious);
            }
        }

        return unmodifiedLocations;
    }
    
    private static <L1, L2> boolean isLocationUnmodified(final L1 locationInPrevious, final OneSEVPA<L1, JSONSymbol> previousHypothesis, final OneSEVPA<L2, JSONSymbol> currentHypothesis, final Map<L1, L2> previousToCurrentLocations) {
        final Alphabet<JSONSymbol> internalAlphabet = currentHypothesis.getInputAlphabet().getInternalAlphabet();
        final Alphabet<JSONSymbol> callAlphabet = currentHypothesis.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = currentHypothesis.getInputAlphabet().getReturnAlphabet();

        final L2 locationInCurrent = previousToCurrentLocations.get(locationInPrevious);

        // We check whether there is an internal transition that already existed in the previous hypothesis and that leads to a different location in the current hypothesis
        for (final JSONSymbol internalSym : internalAlphabet) {
            final L1 targetInPrevious = previousHypothesis.getInternalSuccessor(locationInPrevious, internalSym);
            final L2 targetInCurrent = currentHypothesis.getInternalSuccessor(locationInCurrent, internalSym);
            if (targetInCurrent != previousToCurrentLocations.get(targetInPrevious)) {
                return false;
            }
        }

        // We do the same for return transitions
        for (final JSONSymbol callSymbol : callAlphabet) {
            for (final JSONSymbol returnSymbol : returnAlphabet) {
                for (final L1 locationBeforeCallInPrevious : previousHypothesis.getLocations()) {
                    final L2 locationBeforeCallInCurrent = previousToCurrentLocations.get(locationBeforeCallInPrevious);

                    final int stackSymInPrevious = previousHypothesis.encodeStackSym(locationBeforeCallInPrevious, callSymbol);
                    final int stackSymInCurrent = currentHypothesis.encodeStackSym(locationBeforeCallInCurrent, callSymbol);

                    final L1 targetInPrevious = previousHypothesis.getReturnSuccessor(locationInPrevious, returnSymbol, stackSymInPrevious);
                    final L2 targetInCurrent = currentHypothesis.getReturnSuccessor(locationInCurrent, returnSymbol, stackSymInCurrent);

                    if (targetInCurrent != previousToCurrentLocations.get(targetInPrevious)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
}
