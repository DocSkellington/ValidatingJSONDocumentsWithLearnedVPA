package be.ac.umons.jsonvalidation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.google.common.base.Stopwatch;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import be.ac.umons.jsonschematools.DefaultValidator;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import be.ac.umons.jsonschematools.Validator;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.WordConversion;
import be.ac.umons.jsonvalidation.validation.ValidationByAutomaton;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.Word;

public class Benchmarks {
    public static void main(String[] args) throws InterruptedException, IOException, JSONSchemaException {
        final Path pathToSchema = Paths.get(args[0]);
        final Path pathToVPA = Paths.get(args[1]);
        final Path pathToDocuments = Paths.get(args[2]);
        final int nExperiments = Integer.valueOf(args[3]);

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
        final LocalDateTime now = LocalDateTime.now();

        final String schemaName = pathToSchema.getFileName().toString();

        final JSONSchemaStore schemaStore = new JSONSchemaStore();
        JSONSchema schema;
        try {
            URL url = pathToSchema.toUri().toURL();
            schema = schemaStore.load(url.toURI());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        InputModelDeserializer<JSONSymbol, DefaultOneSEVPA<JSONSymbol>> parser = DOTParsers.oneSEVPA(JSONSymbol::toSymbol);
        DefaultOneSEVPA<JSONSymbol> vpa = parser.readModel(pathToVPA.toFile()).model;
        GraphDOT.write(vpa, System.out);

        System.out.println("Starting permutation automaton benchmarks");
        System.out.println("Schema name: " + schemaName + "; VPA Dot file: " + pathToVPA);
        System.out.println("Path to JSON documents: " + pathToDocuments);
        System.out.println("Number of experiments: " + nExperiments);
        System.out.println("Call alphabet: " + vpa.getInputAlphabet().getCallAlphabet());
        System.out.println("Return alphabet: " + vpa.getInputAlphabet().getReturnAlphabet());
        System.out.println("Internal alphabet: " + vpa.getInputAlphabet().getInternalAlphabet());

        Path pathToCSVFolder = Paths.get(System.getProperty("user.dir"), "Results", "Permutation");
        pathToCSVFolder.toFile().mkdirs();
        Path pathToCSVFile = pathToCSVFolder.resolve("" + schemaName + "-" + pathToVPA + "-" + nExperiments + "-" + dtf.format(now) + ".csv");
        Benchmarks benchmarks = new Benchmarks(pathToCSVFile);
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        benchmarks.runBenchmarks(schema, vpa, pathToDocuments, nExperiments);
        scanner.close();
    }

    protected final CSVPrinter csvPrinter;
    protected final int nColumns;

    private Benchmarks(final Path pathToCSVFile) throws IOException {
        this.csvPrinter = new CSVPrinter(new FileWriter(pathToCSVFile.toFile()), CSVFormat.DEFAULT);

        List<String> header = getHeader();
        this.nColumns = header.size();
        csvPrinter.print(header);
        csvPrinter.flush();
    }

    private List<String> getHeader() {
        // TODO: implement statistics
        // @formatter:off
        return Arrays.asList(
            "Type",
            "Length document",
            "Depth document",
            "Depth schema",
            "Automaton time (ms)",
            "Automaton output",
            "Validator time (ms)",
            "Validator output"
        );
        // @formatter:on
    }

    private void runBenchmarks(final JSONSchema schema, final DefaultOneSEVPA<JSONSymbol> vpa, final Path pathToDocuments, final int nExperiments) throws JSONException, IOException {
        for (int experimentId = 0 ; experimentId < nExperiments ; experimentId++) {
            System.out.println(experimentId);
            Stopwatch watch = Stopwatch.createStarted();
            ValidationByAutomaton automaton = new ValidationByAutomaton(vpa);
            watch.stop();
            
            csvPrinter.printRecords("PRECOMPUTATION", watch.elapsed().toMillis());

            Validator validator = new DefaultValidator();
            int documentId = 0;
            for (final File file : pathToDocuments.toFile().listFiles()) {
                if (file.isFile()) {
                    final JSONObject document = new JSONObject(new JSONTokener(new FileReader(file)));
                    runExperiment(automaton, validator, schema, document, documentId);
                    documentId++;
                }
            }
        }
    }

    private void runExperiment(final ValidationByAutomaton automaton, final Validator validator, final JSONSchema schema, final JSONObject document, final int documentId) throws IOException {
        Word<JSONSymbol> word = WordConversion.fromJSONDocumentToJSONSymbolWord(document, false, new Random());

        final boolean automatonOutput = automaton.accepts(word);

        boolean validatorOutput;
        boolean validatorError;
        try {
            validatorOutput = validator.validate(schema, document);
            validatorError = false;
        }
        catch (JSONSchemaException e) {
            validatorError = true;
            validatorOutput = false;
        }

        final List<Object> statistics = new LinkedList<>();
        statistics.add(documentId);
        statistics.add("AUTOMATON TIME");
        statistics.add(automatonOutput);
        if (validatorError) {
            statistics.add("Error");
            statistics.add("Error");
        }
        else {
            statistics.add("VALIDATOR TIME");
            statistics.add(validatorOutput);
        }

        csvPrinter.printRecord(statistics);
        csvPrinter.flush();
    }
}