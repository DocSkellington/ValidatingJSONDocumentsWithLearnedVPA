package be.ac.umons.jsonvalidation;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import be.ac.umons.jsonvalidation.exploration.ExplorationROCABenchmarks;
import be.ac.umons.jsonvalidation.exploration.ExplorationVCABenchmarks;
import be.ac.umons.jsonvalidation.exploration.ExplorationVPDABenchmarks;
import be.ac.umons.jsonvalidation.random.RandomROCABenchmarks;
import be.ac.umons.jsonvalidation.random.RandomVCABenchmarks;
import be.ac.umons.jsonvalidation.random.RandomVPDABenchmarks;
import de.learnlib.api.logging.LearnLogger;

public class Benchmarks {
    private static final LearnLogger LOGGER = LearnLogger.getLogger(Benchmarks.class);

    private enum AutomatonType {
        VCA,
        ROCA,
        VPA
    }

    private enum EquivalenceType {
        RANDOM,
        EXPLORATION
    }

    public static void main(String[] args) throws InterruptedException, IOException, JSONSchemaException {
        final AutomatonType automatonType = AutomatonType.valueOf(args[0].toUpperCase());
        final EquivalenceType equivalenceType = EquivalenceType.valueOf(args[1].toUpperCase());
        final int timeLimit = Integer.valueOf(args[2]);

        final Duration timeout = Duration.ofSeconds(timeLimit);
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
        final LocalDateTime now = LocalDateTime.now();

        final Path filePath = Paths.get(args[3]);
        final String schemaName = filePath.getFileName().toString();
        final int nTests = Integer.valueOf(args[4]);
        final boolean canGenerateInvalid = Boolean.valueOf(args[5]);
        final int maxDocumentDepth = Integer.valueOf(args[6]);
        final int nRepetitions = Integer.valueOf(args[7]);
        final boolean shuffleKeys = Boolean.valueOf(args[8]);
        final int maxProperties = Integer.valueOf(args[9]);
        final int maxItems = Integer.valueOf(args[10]);
        final boolean ignoreAdditionalProperties = Boolean.valueOf(args[11]);

        final JSONSchemaStore schemaStore = new JSONSchemaStore(ignoreAdditionalProperties);
        final JSONSchema schema;
        try {
            URL url = filePath.toUri().toURL();
            schema = schemaStore.load(url.toURI());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        LOGGER.info("Starting JSON benchmarks with the following parameters:");
        LOGGER.info("Schema name: " + schemaName);
        LOGGER.info("Number of tests for equivalence: " + nTests);
        LOGGER.info("Can generate invalid documents: " + canGenerateInvalid);
        LOGGER.info("Max depth of generated document: " + maxDocumentDepth);
        LOGGER.info("maximum number of properties in an object: " + maxProperties);
        LOGGER.info("maximum number of items in an array: " + maxItems);
        LOGGER.info("Are true additional properties ignored: " + ignoreAdditionalProperties);
        LOGGER.info("Number of repetitions: " + nRepetitions);

        Path pathToCSVFolder = Paths.get(System.getProperty("user.dir"), "Results", "JSON");
        pathToCSVFolder.toFile().mkdirs();
        Path pathToCSVFile = pathToCSVFolder.resolve("" + timeLimit + "s-" + schemaName + "-" + automatonType + "-"
                + nTests + "-" + nRepetitions + "-" + shuffleKeys + "-" + dtf.format(now) + ".csv");
        ABenchmarks benchmarks;
        if (automatonType == AutomatonType.VPA) {
            if (equivalenceType == EquivalenceType.RANDOM) {
                benchmarks = new RandomVPDABenchmarks(pathToCSVFile, timeout, maxProperties, maxItems);
            } else {
                benchmarks = new ExplorationVPDABenchmarks(pathToCSVFile, timeout, maxProperties, maxItems);
            }
        } else if (automatonType == AutomatonType.VCA) {
            if (equivalenceType == EquivalenceType.RANDOM) {
                benchmarks = new RandomVCABenchmarks(pathToCSVFile, timeout, maxProperties, maxItems);
            } else {
                benchmarks = new ExplorationVCABenchmarks(pathToCSVFile, timeout, maxProperties, maxItems);
            }
        } else {
            if (equivalenceType == EquivalenceType.RANDOM) {
                benchmarks = new RandomROCABenchmarks(pathToCSVFile, timeout, maxProperties, maxItems);
            } else {
                benchmarks = new ExplorationROCABenchmarks(pathToCSVFile, timeout, maxProperties, maxItems);
            }
        }
        benchmarks.runBenchmarks(schema, schemaName, nTests, canGenerateInvalid, maxDocumentDepth, nRepetitions,
                shuffleKeys);
    }
}