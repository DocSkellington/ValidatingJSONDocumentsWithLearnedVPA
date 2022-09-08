package be.ac.umons.jsonvalidation.random;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;

import be.ac.umons.jsonlearning.random.JSONPartialEquivalenceOracle;
import be.ac.umons.jsonlearning.random.ROCAJSONEquivalenceOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.ROCABenchmarks;
import de.learnlib.api.oracle.EquivalenceOracle.ROCAEquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.RestrictedAutomatonEquivalenceOracle;
import net.automatalib.words.Alphabet;

public class RandomROCABenchmarks extends ROCABenchmarks {

    public RandomROCABenchmarks(final Path pathToCSVFile, final Path pathToDotFiles, final Duration timeout, int maxProperties, int maxItems)
            throws IOException {
        super(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems);
    }

    @Override
    protected RestrictedAutomatonEquivalenceOracle<JSONSymbol> getRestrictedAutomatonEquivalenceOracle(int nTests,
            boolean canGenerateInvalid, int maxProperties, int maxItems, JSONSchema schema, Random rand,
            boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        return new JSONPartialEquivalenceOracle(nTests, canGenerateInvalid, maxProperties, maxItems, schema, rand,
                shuffleKeys, alphabet);
    }

    @Override
    protected ROCAEquivalenceOracle<JSONSymbol> getEquivalenceOracle(int numberTests, boolean canGenerateInvalid,
            int maxDocumentDepth, int maxProperties, int maxItems, JSONSchema schema, Random random,
            boolean shuffleKeys, Alphabet<JSONSymbol> alphabet) {
        return new ROCAJSONEquivalenceOracle(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems,
                schema, random,
                shuffleKeys, alphabet);
    }
}
