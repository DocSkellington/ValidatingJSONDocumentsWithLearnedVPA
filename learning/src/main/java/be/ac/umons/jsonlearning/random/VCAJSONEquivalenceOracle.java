package be.ac.umons.jsonlearning.random;

import java.util.Collection;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.oca.automatoncountervalues.VCAFromDescription;
import net.automatalib.words.Alphabet;

/**
 * Specialization of {@link AbstractJSONEquivalenceOracle} for VPDAs.
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
 * @author Gaëtan Staquet
 */
public class VCAJSONEquivalenceOracle extends AbstractJSONEquivalenceOracle<VCAFromDescription<?, JSONSymbol>>
        implements EquivalenceOracle.VCAEquivalenceOracle<JSONSymbol> {

    public VCAJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet, Collection<JSONObject> documentsToTest) {
        super(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys,
                alphabet, documentsToTest);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(VCAFromDescription<?, JSONSymbol> hypo,
            Collection<? extends JSONSymbol> inputs) {
        return super.findCounterExample(hypo);
    }

}
