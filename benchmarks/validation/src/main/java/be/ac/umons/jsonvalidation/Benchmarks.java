package be.ac.umons.jsonvalidation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONException;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import be.ac.umons.jsonschematools.generator.random.GeneratorException;
import de.learnlib.api.logging.LearnLogger;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.serialization.dot.GraphDOT;

public class Benchmarks {
    private static final LearnLogger LOGGER = LearnLogger.getLogger(Benchmarks.class);

    private enum Goal {
        GENERATE,
        VALIDATE,
        DEPTH
    }

    public static void main(String[] args) throws InterruptedException, IOException, JSONSchemaException, JSONException,
            GeneratorException, URISyntaxException {
        final Goal goal = Goal.valueOf(args[0].toUpperCase());

        switch (goal) {
            case DEPTH:
                System.out.println(getSchemaDepth(args));
                break;
            case GENERATE:
                getGenerateDocuments(args).generate();
                break;
            case VALIDATE:
                getValidationBenchmarks(args).runBenchmarks();
                break;
        }
    }

    private static int getSchemaDepth(String[] args)
            throws MalformedURLException, FileNotFoundException, JSONSchemaException, URISyntaxException {
        final Path pathToSchema = Paths.get(args[1]);
        final JSONSchema schema = loadSchema(pathToSchema, true);
        return schema.depth();
    }

    private static GenerateDocuments getGenerateDocuments(String[] args)
            throws IOException, JSONSchemaException, URISyntaxException {
        final Path pathToSchema = Paths.get(args[1]);
        final GenerateDocuments.GenerationType generationType = GenerateDocuments.GenerationType
                .valueOf(args[2].toUpperCase());
        final Path pathToDocuments = Paths.get(args[3]);
        final int nDocuments = Integer.valueOf(args[4]);
        final int maxDocumentDepth = Integer.valueOf(args[5]);
        final int maxProperties = Integer.valueOf(args[6]);
        final int maxItems = Integer.valueOf(args[7]);
        final boolean ignoreAdditionalProperties = Boolean.valueOf(args[8]);

        final JSONSchema schema;
        final String schemaName;
        if (pathToSchema.toString().equals("WorstCase")) { // TODO: document that "WorstCase" is a reserved schema name
            final Integer parameterSize = Integer.valueOf(args[9]);
            return new GenerateWorstCaseDocuments(pathToDocuments, nDocuments, maxDocumentDepth, maxProperties, maxItems, parameterSize);
        }
        else {
            if (!pathToDocuments.toFile().isDirectory()) {
                throw new IOException("The path to write the documents in must be a directory");
            }
            schema = loadSchema(pathToSchema, ignoreAdditionalProperties);
            schemaName = pathToSchema.getFileName().toString();
            return new GenerateDocuments(schema, schemaName, generationType, pathToDocuments, nDocuments, maxDocumentDepth, maxProperties, maxItems);
        }
    }

    private static ValidationBenchmarks getValidationBenchmarks(String[] args)
            throws JSONSchemaException, URISyntaxException, IOException {
        final Path pathToSchema = Paths.get(args[1]);
        final Path pathToDocuments = Paths.get(args[3]);
        final int nExperiments = Integer.valueOf(args[4]);

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
        final LocalDateTime now = LocalDateTime.now();

        final JSONSchema schema;
        final String schemaName;
        final String VPAName;
        final DefaultOneSEVPA<JSONSymbol> vpa;
        if (pathToSchema.toString().equals("WorstCase")) { // TODO: document that "WorstCase" is a reserved schema name
        final JSONSchemaStore schemaStore = new JSONSchemaStore(false);
            final int parameterSize = Integer.valueOf(args[2]);
            schema = WorstCaseClassical.constructSchema(schemaStore, parameterSize);
            schemaName = "WorstCase";
            VPAName = "HandWritten";
            vpa = WorstCaseClassical.constructAutomaton(parameterSize);
            System.out.println(schema);
            GraphDOT.write(vpa, System.out);
        }
        else {
            final Path pathToVPA = Paths.get(args[2]);
            if (!pathToDocuments.toFile().isDirectory()) {
                throw new IOException("The path to write the documents in must be a directory");
            }
            schema = loadSchema(pathToSchema, false);
            schemaName = pathToSchema.getFileName().toString();
            VPAName = pathToVPA.getFileName().toString();
            final InputModelDeserializer<JSONSymbol, DefaultOneSEVPA<JSONSymbol>> parser = DOTParsers
                    .oneSEVPA(JSONSymbol::toSymbol);
            vpa = parser.readModel(pathToVPA.toFile()).model;
        }

        LOGGER.info("Starting validation by automaton benchmarks");
        LOGGER.info("Schema name: " + schemaName + "; VPA Dot file: " + VPAName);
        LOGGER.info("Path to JSON documents: " + pathToDocuments);
        LOGGER.info("Number of experiments: " + nExperiments);
        LOGGER.info("Call alphabet: " + vpa.getInputAlphabet().getCallAlphabet());
        LOGGER.info("Return alphabet: " + vpa.getInputAlphabet().getReturnAlphabet());
        LOGGER.info("Internal alphabet: " + vpa.getInputAlphabet().getInternalAlphabet());

        final Path pathToCSVFolder = Paths.get(System.getProperty("user.dir"), "Results", "Validation");
        pathToCSVFolder.toFile().mkdirs();
        final Path pathToValidationCSVFile = pathToCSVFolder.resolve(
                "" + schemaName + "-" + VPAName + "-" + nExperiments + "-validation-" + dtf.format(now) + ".csv");
        final Path pathToPreprocessingCSVFile = pathToCSVFolder.resolve(
                "" + schemaName + "-" + VPAName + "-" + nExperiments + "-preprocessing-" + dtf.format(now) + ".csv");
        return new ValidationBenchmarks(pathToPreprocessingCSVFile, pathToValidationCSVFile, schema, vpa,
                pathToDocuments, nExperiments);
    }

    private static JSONSchema loadSchema(Path pathToSchema, boolean ignoreAdditionalProperties)
            throws MalformedURLException, FileNotFoundException, JSONSchemaException, URISyntaxException {
        final JSONSchemaStore schemaStore = new JSONSchemaStore(ignoreAdditionalProperties);
        URL url = pathToSchema.toUri().toURL();
        return schemaStore.load(url.toURI());
    }
}