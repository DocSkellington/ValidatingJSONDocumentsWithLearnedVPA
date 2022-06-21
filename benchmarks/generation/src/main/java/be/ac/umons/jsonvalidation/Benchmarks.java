package be.ac.umons.jsonvalidation;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONObject;

import com.google.common.base.Stopwatch;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import be.ac.umons.jsonschematools.exploration.DefaultExplorationGenerator;
import be.ac.umons.jsonschematools.random.DefaultRandomGenerator;
import de.learnlib.api.logging.LearnLogger;

public class Benchmarks {
    private static final LearnLogger LOGGER = LearnLogger.getLogger(Benchmarks.class);

    private enum EquivalenceType {
        RANDOM,
        EXPLORATION
    }


    public static void main(String[] args) throws InterruptedException, IOException, JSONSchemaException {
        final EquivalenceType equivalenceType = EquivalenceType.valueOf(args[0].toUpperCase());

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
        final LocalDateTime now = LocalDateTime.now();

        final Path schemaPath = Paths.get(args[1]);
        final String schemaName = schemaPath.getFileName().toString();

        final boolean canGenerateInvalid = Boolean.valueOf(args[2]);
        final int maxDocumentDepth = Integer.valueOf(args[3]);
        final int maxProperties = Integer.valueOf(args[4]);
        final int maxItems = Integer.valueOf(args[5]);
        final boolean ignoreAdditionalProperties = Boolean.valueOf(args[6]);
        final int numberDocuments = Integer.valueOf(args[7]);

        final JSONSchemaStore schemaStore = new JSONSchemaStore(ignoreAdditionalProperties);
        final JSONSchema schema;
        try {
            URL url = schemaPath.toUri().toURL();
            schema = schemaStore.load(url.toURI());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        LOGGER.info("Starting JSON benchmarks with the following parameters:");
        LOGGER.info("Schema name: " + schemaName);
        LOGGER.info("Can generate invalid documents: " + canGenerateInvalid);
        LOGGER.info("Max depth of generated document: " + maxDocumentDepth);
        LOGGER.info("maximum number of properties in an object: " + maxProperties);
        LOGGER.info("maximum number of items in an array: " + maxItems);
        LOGGER.info("Are true additional properties ignored: " + ignoreAdditionalProperties);

        final Path pathToCSVFolder = Paths.get(System.getProperty("user.dir"), "Results", "Generation");
        pathToCSVFolder.toFile().mkdirs();
        final Path pathToCSVFile = pathToCSVFolder.resolve("" + schemaName + "-" + canGenerateInvalid + "-" + maxDocumentDepth + "-" + maxProperties + "-" + maxItems + "-" + ignoreAdditionalProperties + "-" + dtf.format(now) + ".csv");
        
        new Benchmarks(pathToCSVFile, equivalenceType, schema, maxDocumentDepth, canGenerateInvalid, maxProperties, maxItems, numberDocuments).run();
    }

    private final EquivalenceType equivalenceType;
    private final JSONSchema schema;
    private final int maxDocumentDepth;
    private final boolean canGenerateInvalid;
    private final int maxProperties;
    private final int maxItems;
    private final Iterator<JSONObject> iterator;
    private final int maxDocuments;
    private int nDocuments;
        
    private final CSVPrinter csvPrinter;
    private final int nColumns;

    private Benchmarks(Path pathToCSVFile, EquivalenceType equivalenceType, JSONSchema schema, int maxDocumentDepth, boolean canGenerateInvalid, int maxPropertiesObject, int maxItemsArray, int maxDocuments) throws IOException {
        this.equivalenceType = equivalenceType;
        this.schema = schema;
        this.maxDocumentDepth = maxDocumentDepth;
        this.canGenerateInvalid = canGenerateInvalid;
        this.maxProperties = maxPropertiesObject;
        this.maxItems = maxItemsArray;
        this.iterator = getIterator();
        this.maxDocuments = maxDocuments;

        this.csvPrinter = new CSVPrinter(new FileWriter(pathToCSVFile.toFile()), CSVFormat.DEFAULT);
        List<String> header = getHeader();
        this.nColumns = header.size();
        csvPrinter.printRecord(header);
        csvPrinter.flush();
    }

    private List<String> getHeader() {
        // @formatter:off
        return List.of(
            "Number",
            "Time (ms)"
        );
        // @formatter:on
    }

    private Iterator<JSONObject> getIterator() {
        switch (equivalenceType) {
            case EXPLORATION:
                return new DefaultExplorationGenerator(maxProperties, maxItems).createIterator(schema, maxDocumentDepth, canGenerateInvalid);
            case RANDOM:
                return new DefaultRandomGenerator(maxProperties, maxItems).createIterator(schema, maxDocumentDepth, canGenerateInvalid, new Random(1));
            default:
                return null;
        }
    }

    private boolean keepGenerating() {
        if (!iterator.hasNext()) {
            return false;
        }
        return nDocuments < maxDocuments || maxDocuments == -1;
    }

    private void run() throws IOException {
        Stopwatch watch = Stopwatch.createStarted();
        while (keepGenerating()) {
            iterator.next();
            nDocuments++;
        }
        watch.stop();
        
        List<Object> results = new ArrayList<>(nColumns);
        results.add(nDocuments);
        results.add(watch.elapsed().toMillis());
        csvPrinter.printRecord(results);
        csvPrinter.flush();
    }
}
