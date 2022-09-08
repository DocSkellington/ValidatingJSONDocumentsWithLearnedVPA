package be.ac.umons.jsonlearning.random;

import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.Alphabet;

public abstract class AbstractJSONEquivalenceOracle<A extends DeterministicAcceptorTS<?, JSONSymbol>>
        extends AbstractRandomJSONConformance<A> {

    private final int maxDocumentDepth;

    public AbstractJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
        this.maxDocumentDepth = maxDocumentDepth;
    }

    protected @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypothesis) {
        for (int maxTreeSize = 0; maxTreeSize <= maxDocumentDepth; maxTreeSize++) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return null;
            }

            DefaultQuery<JSONSymbol, Boolean> counterexample = findCounterExample(hypothesis, maxTreeSize);
            if (counterexample != null) {
                return counterexample;
            }
        }
        return null;
    }
}
