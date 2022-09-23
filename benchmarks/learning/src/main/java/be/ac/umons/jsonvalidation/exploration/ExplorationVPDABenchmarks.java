package be.ac.umons.jsonvalidation.exploration;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;

import be.ac.umons.jsonlearning.exploration.VPDAJSONEquivalenceOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.VPDABenchmarks;
import de.learnlib.api.oracle.EquivalenceOracle;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.VPDAlphabet;

/**
 * Benchmarks based on JSON documents and Schemas.
 * 
 * @author Gaëtan Staquet
 */
public class ExplorationVPDABenchmarks extends VPDABenchmarks {

    public ExplorationVPDABenchmarks(final Path pathToCSVFile, final Path pathToDotFiles, final Duration timeout,
            int maxProperties, int maxItems)
            throws IOException {
        super(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems);
    }

    @Override
    protected EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> getEquivalenceOracle(int numberTests,
            boolean canGenerateInvalid, int maxDocumentDepth, int maxProperties, int maxItems, JSONSchema schema,
            Random random, boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet) {
        return new VPDAJSONEquivalenceOracle(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems,
                schema, random, shuffleKeys, alphabet);
    }
}
