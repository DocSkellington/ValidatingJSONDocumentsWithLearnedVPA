package be.ac.umons.learningjson;

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
import be.ac.umons.learningjson.exploration.ExplorationROCABenchmarks;
import be.ac.umons.learningjson.exploration.ExplorationVCABenchmarks;
import be.ac.umons.learningjson.exploration.ExplorationVPDABenchmarks;
import be.ac.umons.learningjson.random.RandomROCABenchmarks;
import be.ac.umons.learningjson.random.RandomVCABenchmarks;
import be.ac.umons.learningjson.random.RandomVPDABenchmarks;

public class Benchmarks {
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
        final AutomatonType automatonType = AutomatonType.valueOf(args[0]);
        final EquivalenceType equivalenceType = EquivalenceType.valueOf(args[1]);
        final int timeLimit = Integer.valueOf(args[2]);

        final Duration timeout = Duration.ofSeconds(timeLimit);
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
        final LocalDateTime now = LocalDateTime.now();

        final Path filePath = Paths.get(args[3]);
        final String schemaName = filePath.getFileName().toString();
        final int nTests = Integer.valueOf(args[4]);
        final int maxDocumentDepth = Integer.valueOf(args[5]);
        final int nRepetitions = Integer.valueOf(args[6]);
        final boolean shuffleKeys = Boolean.valueOf(args[7]);

        final JSONSchemaStore schemaStore = new JSONSchemaStore();
        final JSONSchema schema;
        try {
            URL url = filePath.toUri().toURL();
            schema = schemaStore.load(url.toURI());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Starting JSON benchmarks with the following parameters:");
        System.out.println("Schema name: " + schemaName + "; schema contents: " + schema);
        System.out.println("Number of tests for equivalence: " + nTests);
        System.out.println("Number of repetitions: " + nRepetitions);

        Path pathToCSVFolder = Paths.get(System.getProperty("user.dir"), "Results", "JSON");
        pathToCSVFolder.toFile().mkdirs();
        Path pathToCSVFile = pathToCSVFolder.resolve("" + timeLimit + "s-" + schemaName + "-" + automatonType + "-" + nTests + "-" + nRepetitions + "-" + shuffleKeys + "-" + dtf.format(now) + ".csv");
        ABenchmarks benchmarks;
        if (automatonType == AutomatonType.VPA) {
            if (equivalenceType == EquivalenceType.RANDOM) {
                benchmarks = new RandomVPDABenchmarks(pathToCSVFile, timeout);
            }
            else {
                benchmarks = new ExplorationVPDABenchmarks(pathToCSVFile, timeout);
            }
        }
        else if (automatonType == AutomatonType.VCA) {
            if (equivalenceType == EquivalenceType.RANDOM) {
                benchmarks = new RandomVCABenchmarks(pathToCSVFile, timeout);
            }
            else {
                benchmarks = new ExplorationVCABenchmarks(pathToCSVFile, timeout);
            }
        }
        else {
            if (equivalenceType == EquivalenceType.RANDOM) {
                benchmarks = new RandomROCABenchmarks(pathToCSVFile, timeout);
            }
            else {
                benchmarks = new ExplorationROCABenchmarks(pathToCSVFile, timeout);
            }
        }
        benchmarks.runBenchmarks(schema, schemaName, nTests, maxDocumentDepth, nRepetitions, shuffleKeys);
    }
}