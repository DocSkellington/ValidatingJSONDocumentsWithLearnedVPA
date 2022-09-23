package be.ac.umons.jsonlearning.exploration;

import java.util.Collection;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;

import be.ac.umons.jsonlearning.IVPDAJSONEquivalenceOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.VPDAlphabet;

/**
 * Specialization of {@link AbstractExplorationJSONConformanceVisiblyAlphabet}
 * for VPDAs.
 * 
 * <p>
 * When performing an equivalence check, the following tests are performed, in
 * this order:
 * <ol>
 * <li>Is there a loop over the initial location reading an internal symbol? See
 * {@link IVPDAJSONEquivalenceOracle#counterexampleByLoopingOverInitial(OneSEVPA, Random)}</li>
 * <li>Is there a valid document that is rejected by the hypothesis?</li>
 * <li>Is there an invalid document that is accepted by the hypothesis?</li>
 * <li>Is there a gibberish word that is accepted by the hypothesis?</li>
 * <li>Is there a gibberish word using only internal symbols that is accepted by
 * the hypothesis?</li>
 * <li>Is the key graph valid? Note that this implementation does not reuse
 * previously computed relations.</li>
 * </ol>
 * </p>
 * 
 * @author Gaëtan Staquet
 */
public class VPDAJSONEquivalenceOracle
        extends AbstractExplorationJSONConformanceVisiblyAlphabet<OneSEVPA<?, JSONSymbol>>
        implements IVPDAJSONEquivalenceOracle {

    public VPDAJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            VPDAlphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys,
                alphabet);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(OneSEVPA<?, JSONSymbol> hypo,
            Collection<? extends JSONSymbol> inputs) {
        DefaultQuery<JSONSymbol, Boolean> query = counterexampleByLoopingOverInitial(hypo, getRandom());
        if (query != null) {
            return query;
        }

        query = super.findCounterExample(hypo, inputs);
        if (query != null) {
            return query;
        }

        query = counterexampleFromKeyGraph(hypo);
        return query;
    }

}
