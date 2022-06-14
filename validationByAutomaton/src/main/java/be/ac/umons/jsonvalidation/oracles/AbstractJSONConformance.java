package be.ac.umons.jsonvalidation.oracles;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.DefaultValidator;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Validator;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.WordConversion;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public abstract class AbstractJSONConformance<A extends DeterministicAcceptorTS<?, JSONSymbol>> {
    protected static final int MAX_NUMBER_SYMBOLS_GIBBERISH = 5;

    private final JSONSchema schema;
    private final Validator validator;
    private final int numberTests;
    private final boolean shuffleKeys;
    private final Random rand;
    private final Alphabet<JSONSymbol> alphabet;
    private final boolean canGenerateInvalid;
    private final int maxProperties;
    private final int maxItems;

    protected AbstractJSONConformance(int numberTests, boolean canGenerateInvalid, int maxProperties, int maxItems,
            JSONSchema schema, Random random, boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        this.numberTests = numberTests;
        this.schema = schema;
        this.validator = new DefaultValidator();
        this.shuffleKeys = shuffleKeys;
        this.rand = random;
        this.alphabet = alphabet;
        this.canGenerateInvalid = canGenerateInvalid;
        this.maxProperties = maxProperties;
        this.maxItems = maxItems;
    }

    protected Alphabet<JSONSymbol> getAlphabet() {
        return alphabet;
    }

    protected boolean shouldShuffleKeys() {
        return shuffleKeys;
    }

    protected int numberTests() {
        return numberTests;
    }

    protected Random getRandom() {
        return rand;
    }

    public JSONSchema getSchema() {
        return schema;
    }

    public boolean canGenerateInvalid() {
        return canGenerateInvalid;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public int getMaxProperties() {
        return maxProperties;
    }

    protected Word<JSONSymbol> generateGibberish() {
        final int nSymbols = rand.nextInt(MAX_NUMBER_SYMBOLS_GIBBERISH) + 1;
        final int sizeAlphabet = alphabet.size();
        WordBuilder<JSONSymbol> wordBuilder = new WordBuilder<>();
        for (int i = 0; i < nSymbols; i++) {
            wordBuilder.add(alphabet.getSymbol(rand.nextInt(sizeAlphabet)));
        }
        return wordBuilder.toWord();
    }

    protected DefaultQuery<JSONSymbol, Boolean> checkDocument(A hypothesis, JSONObject document) {
        boolean correctForSchema;
        try {
            correctForSchema = validator.validate(schema, document);
        } catch (JSONSchemaException e) {
            e.printStackTrace(System.err);
            return null;
        }
        Word<JSONSymbol> word = WordConversion.fromJSONDocumentToJSONSymbolWord(document, shouldShuffleKeys(), rand);
        boolean correctForHypo = hypothesis.accepts(word);

        if (correctForSchema != correctForHypo) {
            return new DefaultQuery<>(word, correctForSchema);
        }
        return null;
    }

    protected DefaultQuery<JSONSymbol, Boolean> checkWord(A hypothesis, Word<JSONSymbol> word) {
        String string = WordConversion.fromJSONSymbolWordToString(word);
        string = Utils.escapeSymbolsForJSON(string);
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
            } catch (JSONException e) {
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
