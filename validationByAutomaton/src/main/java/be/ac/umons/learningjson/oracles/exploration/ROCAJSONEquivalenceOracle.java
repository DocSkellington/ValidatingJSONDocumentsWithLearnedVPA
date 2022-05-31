package be.ac.umons.learningjson.oracles.exploration;

import java.util.Random;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.learningjson.JSONSymbol;
import de.learnlib.api.oracle.EquivalenceOracle;
import net.automatalib.automata.oca.automatoncountervalues.ROCAFromDescription;
import net.automatalib.words.Alphabet;

public class ROCAJSONEquivalenceOracle extends AbstractExplorationJSONConformance<ROCAFromDescription<?, JSONSymbol>>
        implements EquivalenceOracle.ROCAEquivalenceOracle<JSONSymbol> {
    public ROCAJSONEquivalenceOracle(int numberTests, int maxDocumentDepth, int maxProperties, int maxItems,
            JSONSchema schema, Random random, boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        super(numberTests, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys, alphabet);
    }

}
