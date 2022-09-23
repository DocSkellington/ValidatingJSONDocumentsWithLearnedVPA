package be.ac.umons.jsonlearning.random;

import java.util.Collection;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;

/**
 * Partial equivalence oracle for JSON documents.
 * 
 * It tests whether the provided ROCA accepts the same sets of JSON documents
 * than the JSON Schema, up to a tree depth that depends on the counter limit.
 * 
 * @author Gaëtan Staquet
 */
public class JSONPartialEquivalenceOracle extends AbstractRandomJSONConformance<DFA<?, JSONSymbol>>
        implements EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> {

    private int counterLimit = 0;

    public JSONPartialEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxProperties, int maxItems,
            JSONSchema schema, Random random, boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(DFA<?, JSONSymbol> hypothesis,
            Collection<? extends JSONSymbol> inputs) {
        return findCounterExample(hypothesis, counterLimit);
    }

    @Override
    public void setCounterLimit(int counterLimit) {
        this.counterLimit = counterLimit;
    }
}
