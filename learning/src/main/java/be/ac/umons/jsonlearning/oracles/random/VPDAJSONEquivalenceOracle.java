package be.ac.umons.jsonlearning.oracles.random;

import java.util.Collection;
import java.util.Random;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONObject;

import be.ac.umons.jsonlearning.oracles.IVPDAJSONEquivalenceOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;

public class VPDAJSONEquivalenceOracle extends AbstractJSONEquivalenceOracle<OneSEVPA<?, JSONSymbol>>
        implements IVPDAJSONEquivalenceOracle {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(VPDAJSONEquivalenceOracle.class);

    private final Set<JSONObject> documentsToTest;

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

        LOGGER.info("Creating graph");
        query = counterexampleFromKeyGraph(hypo);
        return query;
    }

}
