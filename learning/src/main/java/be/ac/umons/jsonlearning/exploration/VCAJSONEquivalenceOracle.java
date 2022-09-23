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
