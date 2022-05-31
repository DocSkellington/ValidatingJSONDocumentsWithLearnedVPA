package be.ac.umons.learningjson.random;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.learningjson.JSONSymbol;
import be.ac.umons.learningjson.ROCABenchmarks;
import be.ac.umons.learningjson.oracles.random.JSONPartialEquivalenceOracle;
import be.ac.umons.learningjson.oracles.random.ROCAJSONEquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.ROCAEquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.RestrictedAutomatonEquivalenceOracle;
import net.automatalib.words.Alphabet;

public class RandomROCABenchmarks extends ROCABenchmarks {

    public RandomROCABenchmarks(final Path pathToCSVFile, final Duration timeout) throws IOException {
        super(pathToCSVFile, timeout);
    }

    @Override
    protected RestrictedAutomatonEquivalenceOracle<JSONSymbol> getRestrictedAutomatonEquivalenceOracle(int nTests,
            int maxProperties, int maxItems, JSONSchema schema, Random rand, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet) {
        return new JSONPartialEquivalenceOracle(nTests, maxProperties, maxItems, schema, rand, shuffleKeys, alphabet);
    }

    @Override
    protected ROCAEquivalenceOracle<JSONSymbol> getEquivalenceOracle(int numberTests, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            Alphabet<JSONSymbol> alphabet) {
        return new ROCAJSONEquivalenceOracle(numberTests, maxDocumentDepth, maxProperties, maxItems, schema, random,
                shuffleKeys, alphabet);
    }
}
