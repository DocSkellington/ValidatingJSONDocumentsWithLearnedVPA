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

public class VPDAJSONEquivalenceOracle
        extends AbstractExplorationJSONConformanceVisiblyAlphabet<OneSEVPA<?, JSONSymbol>> implements IVPDAJSONEquivalenceOracle {

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
