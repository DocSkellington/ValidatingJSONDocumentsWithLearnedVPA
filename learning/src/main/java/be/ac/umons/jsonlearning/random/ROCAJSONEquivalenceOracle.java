package be.ac.umons.jsonlearning.random;

import java.util.Collection;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.oca.automatoncountervalues.ROCAFromDescription;
import net.automatalib.words.Alphabet;

public class ROCAJSONEquivalenceOracle extends AbstractJSONEquivalenceOracle<ROCAFromDescription<?, JSONSymbol>>
        implements EquivalenceOracle.ROCAEquivalenceOracle<JSONSymbol> {

    public ROCAJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys,
                alphabet);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(ROCAFromDescription<?, JSONSymbol> hypo,
            Collection<? extends JSONSymbol> inputs) {
        return super.findCounterExample(hypo);
    }

}
