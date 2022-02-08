package be.ac.umons.learningjson.oracles;

import java.util.Collection;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.learningjson.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.oca.automatoncountervalues.VCAFromDescription;

public class VCAJSONEquivalenceOracle extends AbstractJSONEquivalenceOracle<VCAFromDescription<?, JSONSymbol>>
        implements EquivalenceOracle.VCAEquivalenceOracle<JSONSymbol> {

    public VCAJSONEquivalenceOracle(int numberTests, int maxProperties, int maxItems, JSONSchema schema, Random random,
            boolean shuffleKeys) {
        super(numberTests, maxProperties, maxItems, schema, random, shuffleKeys);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(VCAFromDescription<?, JSONSymbol> hypo,
            Collection<? extends JSONSymbol> inputs) {
        return super.findCounterExample(hypo);
    }

}
