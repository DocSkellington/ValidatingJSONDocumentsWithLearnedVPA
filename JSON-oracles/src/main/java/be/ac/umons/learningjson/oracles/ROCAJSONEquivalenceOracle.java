package be.ac.umons.learningjson.oracles;

import java.util.Collection;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.learningjson.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.oca.automatoncountervalues.ROCAFromDescription;

public class ROCAJSONEquivalenceOracle extends AbstractJSONEquivalenceOracle<ROCAFromDescription<?, JSONSymbol>>
        implements EquivalenceOracle.ROCAEquivalenceOracle<JSONSymbol> {

    public ROCAJSONEquivalenceOracle(int numberTests, int maxProperties, int maxItems, JSONSchema schema, Random random,
            boolean shuffleKeys) {
        super(numberTests, maxProperties, maxItems, schema, random, shuffleKeys);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(ROCAFromDescription<?, JSONSymbol> hypo,
            Collection<? extends JSONSymbol> inputs) {
        return super.findCounterExample(hypo);
    }

}
