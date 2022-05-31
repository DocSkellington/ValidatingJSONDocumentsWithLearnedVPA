package be.ac.umons.learningjson.oracles.exploration;

import java.util.Random;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.learningjson.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import net.automatalib.automata.oca.automatoncountervalues.VCAFromDescription;
import net.automatalib.words.VPDAlphabet;

public class VCAJSONEquivalenceOracle extends AbstractExplorationJSONConformanceVisiblyAlphabet<VCAFromDescription<?, JSONSymbol>> implements EquivalenceOracle.VCAEquivalenceOracle<JSONSymbol> {

    public VCAJSONEquivalenceOracle(int numberTests, int maxDocumentDepth, int maxProperties, int maxItems, JSONSchema schema, Random random,
            boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet) {
        super(numberTests, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
    }
}
