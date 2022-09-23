package be.ac.umons.jsonlearning.random;

import java.util.Iterator;
import java.util.Random;

import org.json.JSONObject;

import be.ac.umons.jsonlearning.AbstractJSONConformance;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.generator.random.DefaultRandomGenerator;
import be.ac.umons.jsonschematools.generator.random.RandomGenerator;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Base class for (partial) equivalence queries using a random generator.
 * 
 * <p>
 * It generates a fixed number of JSON documents (up to the fixed maximal depth)
 * and checks whether the hypothesis and the classical validator agrees on their
 * validity.
 * </p>
 * 
 * @author Gaëtan Staquet
 */
public abstract class AbstractRandomJSONConformance<A extends DeterministicAcceptorTS<?, JSONSymbol>>
        extends AbstractJSONConformance<A> implements EquivalenceOracle<A, JSONSymbol, Boolean> {
    private final RandomGenerator generator;
    private final Iterator<JSONObject> validIterator;
    private final Iterator<JSONObject> invalidIterator;

    protected AbstractRandomJSONConformance(int numberTests, boolean canGenerateInvalid, int maxProperties,
            int maxItems, JSONSchema schema, Random random, boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
        this.generator = new DefaultRandomGenerator(maxProperties, maxItems);
        this.validIterator = generator.createIterator(schema, -1, canGenerateInvalid, random);
        this.invalidIterator = generator.createIterator(schema, -1, canGenerateInvalid, random);
    }

    protected DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypothesis, int maxDocumentDepth) {
        if (maxDocumentDepth == 0) {
            if (hypothesis.accepts(Word.epsilon())) {
                return new DefaultQuery<>(Word.epsilon(), false);
            } else {
                return null;
            }
        }

        for (int i = 0; i < numberTests(); i++) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return null;
            }
            JSONObject document = validIterator.next();
            DefaultQuery<JSONSymbol, Boolean> query = checkDocument(hypothesis, document);
            if (query != null) {
                return query;
            }

            if (canGenerateInvalid()) {
                document = invalidIterator.next();
                query = checkDocument(hypothesis, document);
                if (query != null) {
                    return query;
                }
            }

            Word<JSONSymbol> word = generateGibberish();
            query = checkWord(hypothesis, word);
            if (query != null) {
                return query;
            }
        }
        return null;
    }
}
