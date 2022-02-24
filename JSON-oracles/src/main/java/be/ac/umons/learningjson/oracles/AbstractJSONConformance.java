package be.ac.umons.learningjson.oracles;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.DefaultGeneratorInvalid;
import be.ac.umons.jsonschematools.DefaultGeneratorValid;
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
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public abstract class AbstractJSONConformance<A extends DeterministicAcceptorTS<?, JSONSymbol>> {
    private static final int MAX_NUMBER_SYMBOLS_GIBBERISH = 100;

    private final Generator generatorValid;
    private final Generator generatorInvalid;
    private final JSONSchema schema;
    private final Validator validator;
    private final int numberTests;
    private final boolean shuffleKeys;
    private final Random rand;
    private final Alphabet<JSONSymbol> alphabet;

    public AbstractJSONConformance(int numberTests, int maxProperties, int maxItems, JSONSchema schema, Random random,
            boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        this.numberTests = numberTests;
        this.schema = schema;
        this.generatorValid = new DefaultGeneratorValid(maxProperties, maxItems);
        this.generatorInvalid = new DefaultGeneratorInvalid(maxProperties, maxItems);
        this.validator = new DefaultValidator();
        this.shuffleKeys = shuffleKeys;
        this.rand = random;
        this.alphabet = alphabet;
    }

    protected DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypothesis, int maxTreeSize) {
        if (maxTreeSize == 0) {
            if (hypothesis.accepts(Word.epsilon())) {
                return new DefaultQuery<>(Word.epsilon(), false);
            } else {
                return null;
            }
        }

        for (int i = 0; i < numberTests; i++) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return null;
            }
            JSONObject document = generateDocument(generatorValid, maxTreeSize, rand);
            DefaultQuery<JSONSymbol, Boolean> query = checkDocument(hypothesis, document);
            if (query != null) {
                return query;
            }

            document = generateDocument(generatorInvalid, maxTreeSize, rand);
            query = checkDocument(hypothesis, document);
            if (query != null) {
                return query;
            }

            Word<JSONSymbol> word = generateGibberish(rand);
            query = checkWord(hypothesis, word);
            if (query != null) {
                return query;
            }
        }
        return null;
    }

    protected Word<JSONSymbol> generateGibberish(Random rand) {
        final int nSymbols = rand.nextInt(MAX_NUMBER_SYMBOLS_GIBBERISH) + 1;
        final int sizeAlphabet = alphabet.size();
        WordBuilder<JSONSymbol> wordBuilder = new WordBuilder<>();
        for (int i = 0; i < nSymbols; i++) {
            wordBuilder.add(alphabet.getSymbol(rand.nextInt(sizeAlphabet)));
        }
        return wordBuilder.toWord();
    }

    protected JSONObject generateDocument(Generator generator, int maxTreeSize, Random rand) {
        try {
            return (JSONObject) generator.generate(schema, maxTreeSize, rand);
        } catch (GeneratorException | JSONException | JSONSchemaException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    protected DefaultQuery<JSONSymbol, Boolean> checkDocument(A hypothesis, JSONObject document) {
        boolean correctForSchema;
        try {
            correctForSchema = validator.validate(schema, document);
        } catch (JSONSchemaException e) {
            e.printStackTrace(System.err);
            return null;
        }
        Word<JSONSymbol> word = WordConversion.fromJSONDocumentToJSONSymbolWord(document, shuffleKeys, rand);
        boolean correctForHypo = hypothesis.accepts(word);

        if (correctForSchema != correctForHypo) {
            return new DefaultQuery<>(word, correctForSchema);
        }
        return null;
    }

    protected DefaultQuery<JSONSymbol, Boolean> checkWord(A hypothesis, Word<JSONSymbol> word) {
        String string = WordConversion.fromJSONSymbolWordToString(word);
        if (!Utils.validWord(string)) {
            // Since constructing the JSON object might resolve the reason why the word is
            // invalid (such as missing quotes around a key), we handle this case explicitly
            if (hypothesis.accepts(word)) {
                return new DefaultQuery<>(word, false);
            }
        } else {
            JSONObject document = null;
            try {
                document = new JSONObject(string);
            }
            catch (JSONException e) {
                if (hypothesis.accepts(word)) {
                    return new DefaultQuery<>(word, false);
                }
            }

            if (document != null) {
                boolean correctForSchema;
                try {
                    correctForSchema = validator.validate(schema, document);
                } catch (JSONSchemaException e) {
                    e.printStackTrace(System.err);
                    return null;
                }
                boolean correctForHypo = hypothesis.accepts(word);

                if (correctForSchema != correctForHypo) {
                    return new DefaultQuery<>(word, correctForSchema);
                }
            }
        }
        return null;
    }
}
