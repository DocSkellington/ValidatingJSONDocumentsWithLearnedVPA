package be.ac.umons.jsonvalidation.oracles.exploration;

import java.util.Collection;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

abstract class AbstractExplorationJSONConformanceVisiblyAlphabet<A extends DeterministicAcceptorTS<?, JSONSymbol>>
        extends AbstractExplorationJSONConformance<A> {

    private final VPDAlphabet<JSONSymbol> alphabet;

    protected AbstractExplorationJSONConformanceVisiblyAlphabet(int numberTests, boolean canGenerateInvalid,
            int maxDocumentDepth, int maxProperties, int maxItems, JSONSchema schema, Random random,
            boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys,
                alphabet);

        this.alphabet = alphabet;
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypo,
            Collection<? extends JSONSymbol> inputs) {
        DefaultQuery<JSONSymbol, Boolean> query = super.findCounterExample(hypo);
        if (query != null) {
            return query;
        }

        System.out.println("INTERNAL GIBBERISH");
        for (int i = 0; i < numberTests(); i++) {
            Word<JSONSymbol> word = generateGibberishInternalSymbols();
            query = checkWord(hypo, word);
            if (query != null) {
                return query;
            }
        }
        return null;
    }

    protected Word<JSONSymbol> generateGibberishInternalSymbols() {
        final int nSymbols = getRandom().nextInt(MAX_NUMBER_SYMBOLS_GIBBERISH) + 1;
        final Alphabet<JSONSymbol> internalAlphabet = alphabet.getInternalAlphabet();
        final int sizeAlphabet = internalAlphabet.size();
        WordBuilder<JSONSymbol> wordBuilder = new WordBuilder<>();
        for (int i = 0; i < nSymbols; i++) {
            wordBuilder.add(internalAlphabet.getSymbol(getRandom().nextInt(sizeAlphabet)));
        }
        return wordBuilder.toWord();
    }
}
