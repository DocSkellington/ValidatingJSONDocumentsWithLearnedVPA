package be.ac.umons.jsonlearning.exploration;

import java.util.Random;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import net.automatalib.automata.oca.automatoncountervalues.ROCAFromDescription;
import net.automatalib.words.Alphabet;

/**
 * Specialization of {@link AbstractExplorationJSONConformance} for ROCAs.
 * 
 * <p>
 * See {@link AbstractExplorationJSONConformance} for the tests performed during
 * an equivalence query.
 * </p>
 * 
 * @author Gaëtan Staquet
 */
public class ROCAJSONEquivalenceOracle extends AbstractExplorationJSONConformance<ROCAFromDescription<?, JSONSymbol>>
        implements EquivalenceOracle.ROCAEquivalenceOracle<JSONSymbol> {
    public ROCAJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys,
                alphabet);
    }

}
