package be.ac.umons.jsonlearning.exploration;

import java.util.Random;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
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
public class JSONPartialEquivalenceOracle extends AbstractExplorationJSONConformance<DFA<?, JSONSymbol>>
        implements EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> {

    public JSONPartialEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxProperties, int maxItems,
            JSONSchema schema, Random random, boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, 0, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
    }

    @Override
    public void setCounterLimit(int counterLimit) {
        setMaximalDocumentDepth(counterLimit);
    }
}
