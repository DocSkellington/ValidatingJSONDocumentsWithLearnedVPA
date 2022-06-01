package be.ac.umons.learningjson.oracles.exploration;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.exploration.DefaultExplorationGenerator;
import be.ac.umons.jsonschematools.exploration.ExplorationGenerator;
import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.learningjson.oracles.AbstractJSONConformance;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

abstract class AbstractExplorationJSONConformance<A extends DeterministicAcceptorTS<?, JSONSymbol>>
        extends AbstractJSONConformance<A> implements EquivalenceOracle<A, JSONSymbol, Boolean> {

    private final ExplorationGenerator generator;
    private Iterator<JSONObject> iteratorDocuments;
    private int numberGeneratedDocuments = 0;

    protected AbstractExplorationJSONConformance(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
        this.generator = new DefaultExplorationGenerator(maxProperties, maxItems);
        setMaximalDocumentDepth(maxDocumentDepth);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypo,
            Collection<? extends JSONSymbol> inputs) {
        return findCounterExample(hypo);
    }

    private boolean continueGeneration() {
        if (numberTests() == -1) {
            return true;
        }
        return numberGeneratedDocuments++ < numberTests();
    }

    protected DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypothesis) {
        while (iteratorDocuments.hasNext() && continueGeneration()) {
            JSONObject document = iteratorDocuments.next();
            System.out.println(document);
            DefaultQuery<JSONSymbol, Boolean> query = checkDocument(hypothesis, document);
            if (query != null) {
                return query;
            }
        }

        for (int i = 0; i < numberTests(); i++) {
            Word<JSONSymbol> word = generateGibberish();
            DefaultQuery<JSONSymbol, Boolean> query = checkWord(hypothesis, word);
            if (query != null) {
                return query;
            }
        }

        return null;
    }

    protected void setMaximalDocumentDepth(int maxDocumentDepth) {
        this.iteratorDocuments = generator.createIterator(getSchema(), maxDocumentDepth, canGenerateInvalid());
    }
}
