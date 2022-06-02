package be.ac.umons.jsonvalidation.oracles.random;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.random.DefaultRandomGeneratorInvalid;
import be.ac.umons.jsonschematools.random.DefaultRandomGeneratorValid;
import be.ac.umons.jsonschematools.random.GeneratorException;
import be.ac.umons.jsonschematools.random.RandomGenerator;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.oracles.AbstractJSONConformance;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public abstract class AbstractRandomJSONConformance<A extends DeterministicAcceptorTS<?, JSONSymbol>>
        extends AbstractJSONConformance<A> {
    private final RandomGenerator generatorValid;
    private final RandomGenerator generatorInvalid;

    protected AbstractRandomJSONConformance(int numberTests, boolean canGenerateInvalid, int maxProperties,
            int maxItems, JSONSchema schema, Random random, boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
        this.generatorValid = new DefaultRandomGeneratorValid(maxProperties, maxItems);
        this.generatorInvalid = new DefaultRandomGeneratorInvalid(maxProperties, maxItems);
    }

    protected DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypothesis, int maxTreeSize) {
        if (maxTreeSize == 0) {
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
            JSONObject document = generateDocument(generatorValid, maxTreeSize);
            DefaultQuery<JSONSymbol, Boolean> query = checkDocument(hypothesis, document);
            if (query != null) {
                return query;
            }

            if (canGenerateInvalid()) {
                document = generateDocument(generatorInvalid, maxTreeSize);
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

    private JSONObject generateDocument(RandomGenerator generator, int maxTreeSize) {
        try {
            return (JSONObject) generator.generate(getSchema(), maxTreeSize, getRandom());
        } catch (GeneratorException | JSONException | JSONSchemaException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
}
