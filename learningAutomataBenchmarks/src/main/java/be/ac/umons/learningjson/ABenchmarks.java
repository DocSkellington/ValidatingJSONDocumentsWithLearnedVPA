package be.ac.umons.learningjson;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashSet;
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

import com.google.common.base.Stopwatch;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.util.AbstractExperiment;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public abstract class ABenchmarks {
    protected final static int MAX_PROPERTIES = 10;
    protected final static int MAX_ITEMS = 10;

    protected final CSVPrinter csvPrinter;
    protected final int nColumns;
    protected final Duration timeout;

    public ABenchmarks(final Path pathToCSVFile, final Duration timeout) throws IOException {
        csvPrinter = new CSVPrinter(new FileWriter(pathToCSVFile.toFile()), CSVFormat.DEFAULT);
        List<String> header = getHeader();
        this.nColumns = header.size();
        csvPrinter.printRecord(header);
        this.timeout = timeout;
        csvPrinter.flush();
    }

    protected abstract List<String> getHeader();

    public void runBenchmarks(final JSONSchema schema, final String schemaName, final int nTests,
            final int maxDocumentDepth, final int nRepetitions, final boolean shuffleKeys)
            throws InterruptedException, IOException, JSONSchemaException {
        for (int i = 0; i < nRepetitions; i++) {
            System.out.println((i + 1) + "/" + nRepetitions);
            runExperiment(new Random(i), schema, schemaName, nTests, maxDocumentDepth, shuffleKeys, i);
        }
    }

    protected abstract void runExperiment(final Random rand, final JSONSchema schema, final String schemaName,
            final int nTests, final int maxDocumentDepth, final boolean shuffleKeys, final int currentId)
            throws InterruptedException, IOException, JSONSchemaException;

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
        final Set<JSONSymbol> internalSymbols = new HashSet<>();
        final Set<JSONSymbol> callSymbols = new HashSet<>();
        final Set<JSONSymbol> returnSymbols = new HashSet<>();

        callSymbols.add(JSONSymbol.openingBracketSymbol);
        callSymbols.add(JSONSymbol.openingCurlyBraceSymbol);

        returnSymbols.add(JSONSymbol.closingBracketSymbol);
        returnSymbols.add(JSONSymbol.closingCurlyBraceSymbol);

        internalSymbols.add(JSONSymbol.commaSymbol);
        internalSymbols.add(JSONSymbol.toSymbol("true"));
        internalSymbols.add(JSONSymbol.toSymbol("false"));
        internalSymbols.add(JSONSymbol.toSymbol("null"));
        internalSymbols.add(JSONSymbol.toSymbol("\"" + AbstractConstants.stringConstant + "\""));
        internalSymbols.add(JSONSymbol.toSymbol("\"" + AbstractConstants.integerConstant + "\""));
        internalSymbols.add(JSONSymbol.toSymbol("\"" + AbstractConstants.numberConstant + "\""));
        internalSymbols.add(JSONSymbol.toSymbol("\"" + AbstractConstants.enumConstant + "\""));

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
