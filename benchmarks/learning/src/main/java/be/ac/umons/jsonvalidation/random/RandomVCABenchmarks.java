package be.ac.umons.jsonvalidation.random;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;
import java.util.Set;

import org.json.JSONObject;

import be.ac.umons.jsonlearning.random.JSONPartialEquivalenceOracle;
import be.ac.umons.jsonlearning.random.VCAJSONEquivalenceOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.VCABenchmarks;
import de.learnlib.api.oracle.EquivalenceOracle.RestrictedAutomatonEquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.VCAEquivalenceOracle;
import net.automatalib.words.VPDAlphabet;

public class RandomVCABenchmarks extends VCABenchmarks {

    private final Set<JSONObject> documentsToTest;

    public RandomVCABenchmarks(final Path pathToCSVFile, final Path pathToDotFiles, final Duration timeout,
            int maxProperties, int maxItems, final Set<JSONObject> documentsToTest)
            throws IOException {
        super(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems);
        this.documentsToTest = documentsToTest;
    }

    @Override
    protected RestrictedAutomatonEquivalenceOracle<JSONSymbol> getRestrictedAutomatonEquivalenceOracle(int nTests,
            boolean canGenerateInvalid, int maxProperties, int maxItems, JSONSchema schema, Random rand,
            boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet) {
        return new JSONPartialEquivalenceOracle(nTests, canGenerateInvalid, maxProperties, maxItems, schema, rand,
                shuffleKeys, alphabet);
    }

    @Override
    protected VCAEquivalenceOracle<JSONSymbol> getEquivalenceOracle(int nTests, boolean canGenerateInvalid,
            int maxDocumentDepth, int maxProperties, int maxItems, JSONSchema schema, Random rand, boolean shuffleKeys,
            VPDAlphabet<JSONSymbol> alphabet) {
        return new VCAJSONEquivalenceOracle(nTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems,
                schema, rand, shuffleKeys, alphabet, documentsToTest);
    }
}
