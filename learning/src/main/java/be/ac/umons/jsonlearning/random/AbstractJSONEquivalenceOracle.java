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

package be.ac.umons.jsonlearning.random;

import java.util.Collection;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.Alphabet;

/**
 * Base class for (full) equivalence queries using a random JSON documents
 * generator.
 * 
 * <p>
 * When performing an equivalence query, multiple partial equivalence queries
 * (see {@link AbstractRandomJSONConformance}) are performed, one per document
 * depth between 0 and the maximal depth.
 * </p>
 * 
 * <p>
 * It is possible to provide a collection of documents to explicitly test.
 * These documents are tested before the randomly generated ones.
 * </p>
 * 
 * @author Gaëtan Staquet
 */
public abstract class AbstractJSONEquivalenceOracle<A extends DeterministicAcceptorTS<?, JSONSymbol>>
        extends AbstractRandomJSONConformance<A> {

    private final int maxDocumentDepth;
    private final Collection<JSONObject> documentsToTest;

    public AbstractJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet, Collection<JSONObject> documentsToTest) {
        super(numberTests, canGenerateInvalid, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
        this.maxDocumentDepth = maxDocumentDepth;
        this.documentsToTest = documentsToTest;
    }

    protected @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(A hypothesis) {
        for (JSONObject document : documentsToTest) {
            DefaultQuery<JSONSymbol, Boolean> query = checkDocument(hypothesis, document);
            if (query != null) {
                return query;
            }
        }

        for (int maxDepth = 0; maxDepth <= maxDocumentDepth; maxDepth++) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return null;
            }

            DefaultQuery<JSONSymbol, Boolean> counterexample = findCounterExample(hypothesis, maxDepth);
            if (counterexample != null) {
                return counterexample;
            }
        }
        return null;
    }
}
