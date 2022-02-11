package be.ac.umons.learningjson.oracles;

import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.DefaultGenerator;
import be.ac.umons.jsonschematools.DefaultValidator;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Validator;
import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.learningjson.WordConversion;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.Word;

public abstract class AbstractJSONEquivalenceOracle<A extends DeterministicAcceptorTS<?, JSONSymbol>> {
    private final Generator generator;
    private final JSONSchema schema;
    private final Validator validator;
    private final int numberTests;
    private final boolean shuffleKeys;
    private final Random rand;

    public AbstractJSONEquivalenceOracle(int numberTests, int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys) {
        this.numberTests = numberTests;
        this.schema = schema;
        this.generator = new DefaultGenerator(maxProperties, maxItems);
        this.validator = new DefaultValidator();
        this.shuffleKeys = shuffleKeys;
        this.rand = random;
    }

    protected @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypo) {
        for (int maxTreeSize = 1 ; maxTreeSize <= 100 ; maxTreeSize++) {
            for (int i = 0 ; i < numberTests ; i++) {
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                boolean correctForSchema;
                JSONObject document = null;
                try {
                    document = (JSONObject) generator.generate(schema, maxTreeSize, rand);
                    correctForSchema = validator.validate(schema, document);
                } catch (GeneratorException | JSONException | JSONSchemaException e) {
                    e.printStackTrace();
                    return null;
                }

                Word<JSONSymbol> word = WordConversion.fromJSONDocumentToJSONSymbolWord(document, shuffleKeys, rand);
                boolean correctForHypo = hypo.accepts(word);

                if (correctForSchema != correctForHypo) {
                    return new DefaultQuery<>(word, correctForSchema);
                }
            }
        }
        return null;
    }
}
