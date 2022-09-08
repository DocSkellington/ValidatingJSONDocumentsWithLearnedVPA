package be.ac.umons.jsonlearning.random;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONObject;

import be.ac.umons.jsonlearning.IVPDAJSONEquivalenceOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.relation.ReachabilityRelation;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;

public class VPDAJSONEquivalenceOracle extends AbstractJSONEquivalenceOracle<OneSEVPA<?, JSONSymbol>>
        implements IVPDAJSONEquivalenceOracle {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(VPDAJSONEquivalenceOracle.class);

    private final Set<JSONObject> documentsToTest;
    private DefaultOneSEVPA<JSONSymbol> previousHypothesis = null;
    private ReachabilityRelation<Location> previousReachabilityRelation = null;

    public VPDAJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet, Set<JSONObject> documentsToTest) {
        super(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys,
                alphabet);
        this.documentsToTest = documentsToTest;
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(OneSEVPA<?, JSONSymbol> hypo,
            Collection<? extends JSONSymbol> inputs) {
        for (JSONObject document : documentsToTest) {
            DefaultQuery<JSONSymbol, Boolean> query = checkDocument(hypo, document);
            if (query != null) {
                return query;
            }
        }

        DefaultQuery<JSONSymbol, Boolean> query = counterexampleByLoopingOverInitial(hypo, getRandom());
        if (query != null) {
            return query;
        }
        
        query = super.findCounterExample(hypo);
        if (query != null) {
            return query;
        }

        DefaultOneSEVPA<JSONSymbol> hypothesis = convertHypothesis(hypo);
        return findCounterExampleFromKeyGraph(hypothesis);
    }

    private DefaultQuery<JSONSymbol, Boolean> findCounterExampleFromKeyGraph(DefaultOneSEVPA<JSONSymbol> currentHypothesis) {
        LOGGER.info("Creating graph");
        final Pair<DefaultQuery<JSONSymbol, Boolean>, ReachabilityRelation<Location>> queryAndRelation;
        if (previousHypothesis == null) {
            queryAndRelation = counterexampleAndRelationFromKeyGraph(currentHypothesis);
        }
        else {
            queryAndRelation = counterexampleAndRelationFromKeyGraph(previousHypothesis, previousReachabilityRelation, currentHypothesis);
        }

        if (queryAndRelation == null) {
            return null;
        }

        this.previousReachabilityRelation = queryAndRelation.getSecond();
        this.previousHypothesis = currentHypothesis;
        return queryAndRelation.getFirst();
    }

    private <L> DefaultOneSEVPA<JSONSymbol> convertHypothesis(OneSEVPA<L, JSONSymbol> original) {
        LOGGER.info("Converting hypothesis");
        final Alphabet<JSONSymbol> internAlphabet = original.getInputAlphabet().getInternalAlphabet();
        final Alphabet<JSONSymbol> callAlphabet = original.getInputAlphabet().getCallAlphabet();
        final Alphabet<JSONSymbol> returnAlphabet = original.getInputAlphabet().getReturnAlphabet();

        final DefaultOneSEVPA<JSONSymbol> converted = new DefaultOneSEVPA<>(original.getInputAlphabet(), original.size());

        final Map<L, Location> originalToConvertedLocations = new LinkedHashMap<>();
        for (final L location : original.getLocations()) {
            final boolean accepting = original.isAcceptingLocation(location);
            final Location convertedLocation;
            if (location == original.getInitialLocation()) {
                convertedLocation = converted.addInitialLocation(accepting);
            }
            else {
                convertedLocation = converted.addLocation(accepting);
            }
            originalToConvertedLocations.put(location, convertedLocation);
        }

        for (final L originalLocation : original.getLocations()) {
            final Location convertedLocation = originalToConvertedLocations.get(originalLocation);
            for (final JSONSymbol internalSym : internAlphabet) {
                final L originalTarget = original.getInternalSuccessor(originalLocation, internalSym);
                final Location convertedTarget = originalToConvertedLocations.get(originalTarget);
                converted.setInternalSuccessor(convertedLocation, internalSym, convertedTarget);
            }

            for (final JSONSymbol callSym : callAlphabet) {
                for (final JSONSymbol returnSym : returnAlphabet) {
                    for (final L originalBeforeCall : original.getLocations()) {
                        final Location convertedBeforeCall = originalToConvertedLocations.get(originalBeforeCall);

                        final int originalStackSym = original.encodeStackSym(originalBeforeCall, callSym);
                        final int convertedStackSym = converted.encodeStackSym(convertedBeforeCall, callSym);

                        final L originalTarget = original.getReturnSuccessor(originalLocation, returnSym, originalStackSym);
                        final Location convertedTarget = originalToConvertedLocations.get(originalTarget);

                        converted.setReturnSuccessor(convertedLocation, returnSym, convertedStackSym, convertedTarget);
                    }
                }
            }
        }

        return converted;
    }

}
