package be.ac.umons.jsonvalidation;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.google.common.base.Stopwatch;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.util.AbstractExperiment;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public abstract class ABenchmarks {
    private static final LearnLogger LOGGER = LearnLogger.getLogger(ABenchmarks.class);

    protected final CSVPrinter csvPrinter;
    protected final int nColumns;
    protected final Duration timeout;
    private final int maxProperties;
    private final int maxItems;

    public ABenchmarks(final Path pathToCSVFile, final Duration timeout, int maxProperties, int maxItems)
            throws IOException {
        this.maxProperties = maxProperties;
        this.maxItems = maxItems;
        csvPrinter = new CSVPrinter(new FileWriter(pathToCSVFile.toFile()), CSVFormat.DEFAULT);
        List<String> header = getHeader();
        this.nColumns = header.size();
        csvPrinter.printRecord(header);
        this.timeout = timeout;
        csvPrinter.flush();
    }

    public int getMaxProperties() {
        return maxProperties;
    }

    public int getMaxItems() {
        return maxItems;
    }

    protected abstract List<String> getHeader();

    public void runBenchmarks(final JSONSchema schema, final String schemaName, final int nTests,
            final boolean canGenerateInvalid, final int maxDocumentDepth, final int nRepetitions,
            final boolean shuffleKeys) throws InterruptedException, IOException, JSONSchemaException {
        for (int i = 0; i < nRepetitions; i++) {
            LOGGER.info((i + 1) + "/" + nRepetitions);
            runExperiment(new Random(i), schema, schemaName, nTests, canGenerateInvalid, maxDocumentDepth, shuffleKeys,
                    i);
        }
    }

    protected abstract void runExperiment(final Random rand, final JSONSchema schema, final String schemaName,
            final int nTests, final boolean canGenerateInvalid, final int maxDocumentDepth, final boolean shuffleKeys,
            final int currentId) throws InterruptedException, IOException, JSONSchemaException;

    protected long getProfilerTime(String key) {
        Counter counter = SimpleProfiler.cumulated(key);
        if (counter == null) {
            return 0;
        } else {
            return counter.getCount();
        }
    }

    protected static VPDAlphabet<JSONSymbol> extractSymbolsFromSchema(final JSONSchema schema)
            throws JSONSchemaException {
        final Set<JSONSymbol> internalSymbols = new LinkedHashSet<>();
        final Set<JSONSymbol> callSymbols = new LinkedHashSet<>();
        final Set<JSONSymbol> returnSymbols = new LinkedHashSet<>();

        callSymbols.add(JSONSymbol.openingBracketSymbol);
        callSymbols.add(JSONSymbol.openingCurlyBraceSymbol);

        returnSymbols.add(JSONSymbol.closingBracketSymbol);
        returnSymbols.add(JSONSymbol.closingCurlyBraceSymbol);

        internalSymbols.add(JSONSymbol.commaSymbol);
        internalSymbols.add(JSONSymbol.trueSymbol);
        internalSymbols.add(JSONSymbol.falseSymbol);
        internalSymbols.add(JSONSymbol.nullSymbol);
        internalSymbols.add(JSONSymbol.stringSymbol);
        internalSymbols.add(JSONSymbol.integerSymbol);
        internalSymbols.add(JSONSymbol.numberSymbol);
        internalSymbols.add(JSONSymbol.enumSymbol);

        internalSymbols.add(JSONSymbol.toSymbol("\"" + AbstractConstants.stringConstant + "\":"));
        schema.getAllKeysDefinedInSchema().stream().map(k -> "\"" + k + "\":").map(k -> JSONSymbol.toSymbol(k))
                .forEach(k -> internalSymbols.add(k));

        return new DefaultVPDAlphabet<>(internalSymbols, callSymbols, returnSymbols);
    }

    protected void writeModelToDot(Graph<?, ?> automaton, String schemaName, int currentId, String modelType)
            throws IOException {
        Path pathToDOTFolder = Paths.get(System.getProperty("user.dir"), "Results", "JSON", "Dot", modelType);
        pathToDOTFolder.toFile().mkdirs();
        Path pathToDotFile = pathToDOTFolder.resolve(schemaName + "-" + String.valueOf(currentId) + ".dot");
        FileWriter writer = new FileWriter(pathToDotFile.toFile());
        GraphDOT.write(automaton, writer);
    }

    protected void writeModelToDot(GraphViewable automaton, String schemaName, int currentId, String modelType)
            throws IOException {
        Path pathToDOTFolder = Paths.get(System.getProperty("user.dir"), "Results", "JSON", "Dot", modelType);
        pathToDOTFolder.toFile().mkdirs();
        Path pathToDotFile = pathToDOTFolder.resolve(schemaName + "-" + String.valueOf(currentId) + ".dot");
        FileWriter writer = new FileWriter(pathToDotFile.toFile());
        GraphDOT.write(automaton, writer);
    }

    protected <A> ExperimentResults runExperiment(AbstractExperiment<A> experiment) throws InterruptedException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        SimpleProfiler.reset();

        final Future<Void> handler = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                experiment.run();
                return null;
            }
        });

        boolean finished;
        boolean error = false;
        Stopwatch watch = Stopwatch.createStarted();
        try {
            handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            finished = true;
        } catch (TimeoutException e) {
            handler.cancel(true);
            finished = false;
        } catch (ExecutionException e) {
            e.printStackTrace(System.err);
            handler.cancel(true);
            error = true;
            finished = false;
        }
        watch.stop();
        executor.shutdownNow();

        return new ExperimentResults(finished, error, watch.elapsed().toMillis());
    }
}
