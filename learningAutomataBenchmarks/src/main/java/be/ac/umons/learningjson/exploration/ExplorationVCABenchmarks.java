package be.ac.umons.learningjson.exploration;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.learningjson.VCABenchmarks;
import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.learningjson.oracles.exploration.JSONPartialEquivalenceOracle;
import be.ac.umons.learningjson.oracles.exploration.VCAJSONEquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.RestrictedAutomatonEquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.VCAEquivalenceOracle;
import net.automatalib.words.VPDAlphabet;

public class ExplorationVCABenchmarks extends VCABenchmarks {

    public ExplorationVCABenchmarks(final Path pathToCSVFile, final Duration timeout) throws IOException {
        super(pathToCSVFile, timeout);
    }

    @Override
    protected RestrictedAutomatonEquivalenceOracle<JSONSymbol> getRestrictedAutomatonEquivalenceOracle(int nTests,
            int maxProperties, int maxItems, JSONSchema schema, Random rand, boolean shuffleKeys,
            VPDAlphabet<JSONSymbol> alphabet) {
        return new JSONPartialEquivalenceOracle(nTests, MAX_PROPERTIES, MAX_ITEMS, schema, rand, shuffleKeys, alphabet);
    }

    @Override
    protected VCAEquivalenceOracle<JSONSymbol> getEquivalenceOracle(int nTests, int maxDocumentDepth, int maxProperties,
            int maxItems, JSONSchema schema, Random rand, boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet) {
        return new VCAJSONEquivalenceOracle(nTests, maxDocumentDepth, maxProperties, maxItems, schema, rand,
                shuffleKeys, alphabet);
    }
}
