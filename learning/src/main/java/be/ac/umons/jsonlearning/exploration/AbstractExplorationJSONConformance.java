/*
 * ValidatingJSONDocumentsWithLearnedVPA - Learning a visibly pushdown automaton
 * from a JSON schema, and using it to validate JSON documents.
 *
 * Copyright 2022 University of Mons, University of Antwerp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.ac.umons.jsonlearning.exploration;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONObject;

import be.ac.umons.jsonlearning.AbstractJSONConformance;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.generator.exploration.DefaultExplorationGenerator;
import be.ac.umons.jsonschematools.generator.exploration.ExplorationGenerator;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Base class for conformance testing-based equivalence oracles using an
 * exhaustive JSON document generator.
 * 
 * <p>
 * When performing an equivalence check, the following tests are performed, in
 * this order:
 * <ol>
 * <li>Is there a valid document that is rejected by the hypothesis?</li>
 * <li>Is there an invalid document that is accepted by the hypothesis?</li>
 * <li>Is there a gibberish word that is accepted by the hypothesis?</li>
 * </ol>
 * </p>
 * 
 * @param <A> Automaton type
 * @author Gaëtan Staquet
 */
abstract class AbstractExplorationJSONConformance<A extends DeterministicAcceptorTS<?, JSONSymbol>>
        extends AbstractJSONConformance<A> implements EquivalenceOracle<A, JSONSymbol, Boolean> {

    private final static LearnLogger LOGGER = LearnLogger.getLogger(AbstractExplorationJSONConformance.class);

    private final ExplorationGenerator generator;
    private Iterator<JSONObject> iteratorValidDocuments = null;
    private Iterator<JSONObject> iteratorInvalidDocuments = null;
    private int numberGeneratedInvalidDocuments = 0;

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

    private boolean continueInvalidGeneration() {
        if (numberTests() == -1) {
            return true;
        }
        return numberGeneratedInvalidDocuments++ < numberTests();
    }

    private boolean continueValidGeneration() {
        return true;
    }

    protected int numberGibberish() {
        if (numberTests() == -1) {
            return 10;
        } else {
            return numberTests();
        }
    }

    @Nullable
    protected DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypothesis) {
        while (iteratorValidDocuments.hasNext() && continueValidGeneration()) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return null;
            }

            JSONObject document = iteratorValidDocuments.next();
            DefaultQuery<JSONSymbol, Boolean> query = checkDocument(hypothesis, document);
            if (query != null) {
                return query;
            }
        }
        LOGGER.info("Valid documents exhausted");

        while (iteratorInvalidDocuments != null && iteratorInvalidDocuments.hasNext() && continueInvalidGeneration()) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return null;
            }

            JSONObject document = iteratorInvalidDocuments.next();
            DefaultQuery<JSONSymbol, Boolean> query = checkDocument(hypothesis, document);
            if (query != null) {
                return query;
            }
        }
        LOGGER.info("Invalid documents exhausted");

        for (int i = 0; i < numberGibberish(); i++) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return null;
            }

            Word<JSONSymbol> word = generateGibberish();
            DefaultQuery<JSONSymbol, Boolean> query = checkWord(hypothesis, word);
            if (query != null) {
                return query;
            }
        }
        LOGGER.info("Gibberish documents exhausted");

        return null;
    }

    protected void setMaximalDocumentDepth(int maxDocumentDepth) {
        this.iteratorValidDocuments = generator.createIterator(getSchema(), maxDocumentDepth, false);
        if (canGenerateInvalid()) {
            this.iteratorInvalidDocuments = generator.createIterator(getSchema(), maxDocumentDepth, true);
        }
    }
}
