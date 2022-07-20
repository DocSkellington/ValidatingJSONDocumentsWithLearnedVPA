package be.ac.umons.jsonvalidation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.base.Stopwatch;
import com.google.common.testing.GcFinalization;

import be.ac.umons.jsonschematools.DefaultValidator;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Validator;
import be.ac.umons.jsonvalidation.validation.ValidationByAutomaton;
import be.ac.umons.jsonvalidation.validation.ValidationState;
import be.ac.umons.jsonvalidation.validation.relation.DotWriter;
import be.ac.umons.jsonvalidation.validation.relation.KeyGraph;
import be.ac.umons.jsonvalidation.validation.relation.ReachabilityRelation;
import de.learnlib.api.logging.LearnLogger;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

public class ValidationBenchmarks {
    private static final LearnLogger LOGGER = LearnLogger.getLogger(ValidationBenchmarks.class);

    private final CSVPrinter preprocessingCSVPrinter;
    private final CSVPrinter validationCSVPrinter;
    private final int nPreprocessingColumns;
    private final int nValidationColumns;
    private final JSONSchema schema;
    private final DefaultOneSEVPA<JSONSymbol> vpa;
    private final Path pathToDocuments;
    private final int nExperiments;

    public ValidationBenchmarks(final Path pathToPreprocessingCSVFile, final Path pathToValidationCSVFile, final JSONSchema schema, final DefaultOneSEVPA<JSONSymbol> vpa, final Path pathToDocuments, final int nExperiments) throws IOException {
        this.preprocessingCSVPrinter = new CSVPrinter(new FileWriter(pathToPreprocessingCSVFile.toFile()), CSVFormat.DEFAULT);
        this.validationCSVPrinter = new CSVPrinter(new FileWriter(pathToValidationCSVFile.toFile()), CSVFormat.DEFAULT);
        this.schema = schema;
        this.vpa = vpa;
        this.pathToDocuments = pathToDocuments;
        this.nExperiments = nExperiments;

        final List<String> preprocessingHeader = getPreprocessingHeader();
        this.nPreprocessingColumns = preprocessingHeader.size();
        preprocessingCSVPrinter.printRecord(preprocessingHeader);
        preprocessingCSVPrinter.flush();;

        final List<String> validationHeader = getValidationHeader();
        this.nValidationColumns = validationHeader.size();
        validationCSVPrinter.printRecord(validationHeader);
        validationCSVPrinter.flush();
    }

    private List<String> getPreprocessingHeader() {
        // @formatter:off
        return List.of(
            "Success",
            "Relation time",
            "Relation memory",
            "Graph time",
            "Graph memory",
            "Automaton time",
            "Automaton memory",
            "Size relation",
            "Size graph"
        );
        // @formatter:on
    }

    private List<String> getValidationHeader() {
        // @formatter:off
        return List.of(
            "Document ID",
            "Length document",
            "Depth document",
            "Automaton time (ms)",
            "Automaton memory",
            "Automaton output",
            "Validator time (ms)",
            "Validator memory",
            "Validator output"
        );
        // @formatter:on
    }

    public void runBenchmarks() throws JSONException, IOException, JSONSchemaException {
        for (int experimentId = 0 ; experimentId < nExperiments ; experimentId++) {
            LOGGER.info((experimentId + 1) + " / " + nExperiments);

            ValidationByAutomaton<Location> automaton = constructAutomaton(vpa);
            if (automaton == null) {
                return;
            }

            Validator validator = new DefaultValidator();
            File[] listFiles = pathToDocuments.toFile().listFiles();
            for (int i = 0 ; i < listFiles.length ; i++) {
                LOGGER.info("File " + (i+1) + " / " + listFiles.length);
                final File file = listFiles[i];
                if (file.isFile()) {
                    final JSONObject document = new JSONObject(new JSONTokener(new FileReader(file)));
                    runExperiment(automaton, validator, schema, document, file.getName());
                }
            }
        }
    }

    private ValidationByAutomaton<Location> constructAutomaton(DefaultOneSEVPA<JSONSymbol> vpa) throws IOException {
        GcFinalization.awaitFullGc();
        final long memoryAtStart = getMemoryUse();
        final Stopwatch watch = Stopwatch.createStarted();

        final ReachabilityRelation<Location> reachabilityRelation = ReachabilityRelation.computeReachabilityRelation(vpa, false);

        final long timeRelations = watch.stop().elapsed().toMillis();
        final long memoryForRelations = getMemoryUse() - memoryAtStart;

        watch.reset().start();
        final KeyGraph<Location> graph = new KeyGraph<>(vpa, reachabilityRelation);

        final long timeGraph = watch.stop().elapsed().toMillis();
        final long memoryForGraph = getMemoryUse() - memoryForRelations;

        watch.reset().start();
        final ValidationByAutomaton<Location> automaton = new ValidationByAutomaton<>(vpa, graph);

        final long timeAutomaton = watch.stop().elapsed().toMillis();
        final long memoryForAutomaton = getMemoryUse() - memoryForGraph;

        final StringBuilder builder = new StringBuilder();
        DotWriter.write(graph, builder);
        LOGGER.logModel(builder);

        final List<Object> statistics = new ArrayList<>(nPreprocessingColumns);
        if (!graph.isValid()) {
            LOGGER.error("The automaton can not be used for our algorithm");
            
            statistics.add(false);
        }
        else {
            statistics.add(true);
        }
        statistics.add(timeRelations);
        statistics.add(memoryForRelations);
        statistics.add(timeGraph);
        statistics.add(memoryForGraph);
        statistics.add(timeAutomaton);
        statistics.add(memoryForAutomaton);
        statistics.add(reachabilityRelation.size());
        statistics.add(graph.size());
        
        preprocessingCSVPrinter.printRecord(statistics);
        preprocessingCSVPrinter.flush();

        if (graph.isValid()) {
            return automaton;
        }
        else {
            return null;
        }
    }

    private void runExperiment(final ValidationByAutomaton<Location> automaton, final Validator validator, final JSONSchema schema, final JSONObject document, final String documentName) throws IOException, JSONSchemaException {
        final Word<JSONSymbol> word = WordConversion.fromJSONDocumentToJSONSymbolWord(document, false, new Random());
        assert word.length() != 0;

        GcFinalization.awaitFullGc();
        final Stopwatch watch = Stopwatch.createStarted();
        final Pair<Boolean, Long> automatonResult = runValidationByAutomaton(automaton, word);
        final long automatonTime = watch.stop().elapsed().toMillis();
        final boolean automatonOutput = automatonResult.getFirst();
        final long automatonMemory = automatonResult.getSecond();

        GcFinalization.awaitFullGc();
        boolean validatorOutput;
        boolean validatorError;
        watch.reset().start();
        try {
            validatorOutput = validator.validate(schema, document);
            validatorError = false;
        }
        catch (JSONSchemaException e) {
            validatorOutput = false;
            validatorError = true;
        }
        final long validatorTime = watch.stop().elapsed().toMillis();

        final List<Object> statistics = new ArrayList<>(nValidationColumns);

        statistics.add(documentName);

        statistics.add(word.length());
        statistics.add(depthDocument(word));

        statistics.add(automatonTime);
        statistics.add(automatonMemory);
        statistics.add(automatonOutput);

        if (validatorError) {
            statistics.add(validatorTime);
            statistics.add(validator.getMaxMemoryUsed());
            statistics.add("Error");
        }
        else {
            statistics.add(validatorTime);
            statistics.add(validator.getMaxMemoryUsed());
            statistics.add(validatorOutput);
        }

        validationCSVPrinter.printRecord(statistics);
        validationCSVPrinter.flush();
    }

    private Pair<Boolean, Long> runValidationByAutomaton(final ValidationByAutomaton<Location> automaton, final Word<JSONSymbol> word) {
        final long memoryStart = getMemoryUse();

        long maxMemory = memoryStart;
        if (word.isEmpty() || !word.getSymbol(0).equals(JSONSymbol.openingCurlyBraceSymbol)) {
            return Pair.of(false, 0L);
        }
        JSONSymbol currentSymbol = word.getSymbol(0);
        ValidationState<Location> validationState = automaton.getInitialState();
        for (int i = 1 ; i < word.size() ; i++) {
            final JSONSymbol nextSymbol = word.getSymbol(i);

            validationState = automaton.getSuccessor(validationState, currentSymbol, nextSymbol);

            currentSymbol = nextSymbol;

            maxMemory = Math.max(maxMemory, getMemoryUse());
        }

        validationState = automaton.getSuccessor(validationState, currentSymbol, null);
        maxMemory = Math.max(maxMemory, getMemoryUse());

        return Pair.of(automaton.isAccepting(validationState), maxMemory - memoryStart);
    }

    private int depthDocument(final Word<JSONSymbol> word) {
        int depth = 0;
        int maxDepth = 0;
        for (JSONSymbol symbol : word) {
            if (symbol.equals(JSONSymbol.openingCurlyBraceSymbol) || symbol.equals(JSONSymbol.openingBracketSymbol)) {
                depth++;
                maxDepth = Math.max(maxDepth, depth);
            }
            else if (symbol.equals(JSONSymbol.closingCurlyBraceSymbol) || symbol.equals(JSONSymbol.closingBracketSymbol)) {
                depth--;
                assert depth >= 0;
            }
        }
        return maxDepth;
    }

    private long getMemoryUse() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
    }
}
