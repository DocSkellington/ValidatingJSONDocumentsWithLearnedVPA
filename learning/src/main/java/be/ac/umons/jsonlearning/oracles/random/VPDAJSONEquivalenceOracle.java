package be.ac.umons.jsonlearning.oracles.random;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONObject;

import be.ac.umons.jsonlearning.oracles.IVPDAJSONEquivalenceOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.generator.random.DefaultRandomGenerator;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;

public class VPDAJSONEquivalenceOracle extends AbstractJSONEquivalenceOracle<OneSEVPA<?, JSONSymbol>>
        implements IVPDAJSONEquivalenceOracle {

    private static final LearnLogger LOGGER = LearnLogger.getLogger(VPDAJSONEquivalenceOracle.class);

    public VPDAJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys,
                alphabet);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(OneSEVPA<?, JSONSymbol> hypo,
            Collection<? extends JSONSymbol> inputs) {
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

    @Override
    public JSONObject getOneValidDocument() {
        final Iterator<JSONObject> iterator = new DefaultRandomGenerator(getMaxProperties(), getMaxItems()).createIterator(getSchema());
        assert iterator.hasNext();
        return iterator.next();
    }

}
