package be.ac.umons.learningjson.oracles;

import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.learningjson.JSONSymbol;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.Alphabet;

public abstract class AbstractJSONEquivalenceOracle<A extends DeterministicAcceptorTS<?, JSONSymbol>>
        extends AbstractJSONConformance<A> {

    public AbstractJSONEquivalenceOracle(int numberTests, int maxProperties, int maxItems, JSONSchema schema,
            Random random, boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        super(numberTests, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
    }

    protected @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypothesis) {
        for (int maxTreeSize = 0; maxTreeSize <= 100; maxTreeSize++) {
            DefaultQuery<JSONSymbol, Boolean> counterexample = findCounterExample(hypothesis, maxTreeSize);
            if (counterexample != null) {
                return counterexample;
            }
        }
        return null;
    }
}
