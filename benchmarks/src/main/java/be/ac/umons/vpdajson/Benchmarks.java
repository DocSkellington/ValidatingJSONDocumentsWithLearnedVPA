package be.ac.umons.vpdajson;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;

public class Benchmarks {
    public static void main(String[] args) throws InterruptedException, IOException, JSONSchemaException {
        final String type = args[0];
        final int timeLimit = Integer.valueOf(args[1]);

        final Duration timeout = Duration.ofSeconds(timeLimit);
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
        final LocalDateTime now = LocalDateTime.now();

        final Random rand = new Random();
        final Path filePath = Paths.get(args[2]);
        final String schemaName = filePath.getFileName().toString();
        int nTests = 1000;
        int nRepetitions = 1000;
        boolean shuffleKeys = true;
        if (args.length >= 3) {
            nTests = Integer.valueOf(args[3]);
            if (args.length >= 4) {
                nRepetitions = Integer.valueOf(args[4]);
                if (args.length >= 5) {
                    shuffleKeys = Boolean.valueOf(args[5]);
                }
            }
        }

        JSONSchemaStore schemaStore = new JSONSchemaStore();
        JSONSchema schema;
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
        Path pathToCSVFile = pathToCSVFolder.resolve("" + timeLimit + "s-" + schemaName + "-" + type + "-" + nTests + "-" + nRepetitions + "-" + shuffleKeys + "-" + dtf.format(now) + ".csv");
        ABenchmarks benchmarks;
        if (type.toLowerCase().equals("vpda")) {
            benchmarks = new VPDABenchmarks(pathToCSVFile, timeout);
        }
        else if (type.toLowerCase().equals("vca")) {
            benchmarks = new VCABenchmarks(pathToCSVFile, timeout);
        }
        else if (type.toLowerCase().equals("roca")) {
            benchmarks = new ROCABenchmarks(pathToCSVFile, timeout);
        }
        else {
            throw new Error("Invalid automaton type " + type);
        }
        benchmarks.runBenchmarks(rand, schema, schemaName, nTests, nRepetitions, shuffleKeys);
    }
}