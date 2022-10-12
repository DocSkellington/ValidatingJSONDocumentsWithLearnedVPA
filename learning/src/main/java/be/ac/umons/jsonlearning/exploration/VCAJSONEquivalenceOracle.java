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

import java.util.Random;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import net.automatalib.automata.oca.automatoncountervalues.VCAFromDescription;
import net.automatalib.words.VPDAlphabet;

/**
 * Specialization of {@link AbstractExplorationJSONConformanceVisiblyAlphabet}
 * for VCAs.
 * 
 * <p>
 * See {@link AbstractExplorationJSONConformanceVisiblyAlphabet} for the tests
 * performed during an equivalence query.
 * </p>
 * 
 * @author Gaëtan Staquet
 */
public class VCAJSONEquivalenceOracle
        extends AbstractExplorationJSONConformanceVisiblyAlphabet<VCAFromDescription<?, JSONSymbol>>
        implements EquivalenceOracle.VCAEquivalenceOracle<JSONSymbol> {

    public VCAJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            VPDAlphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys,
                alphabet);
    }
}
