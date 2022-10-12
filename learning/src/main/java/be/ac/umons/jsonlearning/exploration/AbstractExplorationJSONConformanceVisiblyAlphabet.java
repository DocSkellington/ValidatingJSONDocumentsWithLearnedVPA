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

/**
 * Specialization of {@link AbstractExplorationJSONConformance} for automata
 * using a pushdown alphabet.
 * 
 * <p>
 * When performing an equivalence check, the following tests are performed, in
 * this order:
 * <ol>
 * <li>Is there a valid document that is rejected by the hypothesis?</li>
 * <li>Is there an invalid document that is accepted by the hypothesis?</li>
 * <li>Is there a gibberish word that is accepted by the hypothesis?</li>
 * <li>Is there a gibberish word using only internal symbols that is accepted by
 * the hypothesis?</li>
 * </ol>
 * </p>
 * 
 * @author Gaëtan Staquet
 */
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

        for (int i = 0; i < numberGibberish(); i++) {
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
