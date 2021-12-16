package be.ac.umons.vpdajson;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import be.ac.umons.jsonroca.JSONSymbol;
import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.util.statistics.SimpleProfiler;
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

    public void runBenchmarks(final Random rand, final JSONSchema schema, final String schemaName, final int nTests,
            final int nRepetitions, final boolean shuffleKeys) throws InterruptedException, IOException, JSONSchemaException {
        for (int i = 0; i < nRepetitions; i++) {
            System.out.println((i + 1) + "/" + nRepetitions);
            runExperiment(rand, schema, schemaName, nTests, shuffleKeys, i);
        }
    }

    protected abstract void runExperiment(final Random rand, final JSONSchema schema, final String schemaName, final int nTests, final boolean shuffleKeys, final int currentId)
            throws InterruptedException, IOException, JSONSchemaException;

    protected long getProfilerTime(String key) {
        Counter counter = SimpleProfiler.cumulated(key);
        if (counter == null) {
            return 0;
        } else {
            return counter.getCount();
        }
    }

    protected static VPDAlphabet<JSONSymbol> extractSymbolsFromSchema(final JSONSchema schema) throws JSONSchemaException {
        final Set<JSONSymbol> internalSymbols = new HashSet<>();
        final Set<JSONSymbol> callSymbols = new HashSet<>();
        final Set<JSONSymbol> returnSymbols = new HashSet<>();

        callSymbols.add(JSONSymbol.toSymbol("{"));
        callSymbols.add(JSONSymbol.toSymbol(":{"));
        callSymbols.add(JSONSymbol.toSymbol("["));
        callSymbols.add(JSONSymbol.toSymbol(":["));

        returnSymbols.add(JSONSymbol.toSymbol("}"));
        returnSymbols.add(JSONSymbol.toSymbol("]"));

        internalSymbols.add(JSONSymbol.toSymbol(","));
        internalSymbols.add(JSONSymbol.toSymbol(":"));
        internalSymbols.add(JSONSymbol.toSymbol("true"));
        internalSymbols.add(JSONSymbol.toSymbol("false"));
        internalSymbols.add(JSONSymbol.toSymbol("null"));
        internalSymbols.add(JSONSymbol.toSymbol("\"" + AbstractConstants.stringConstant + "\""));
        internalSymbols.add(JSONSymbol.toSymbol("\"" + AbstractConstants.integerConstant + "\""));
        internalSymbols.add(JSONSymbol.toSymbol("\"" + AbstractConstants.numberConstant + "\""));
        internalSymbols.add(JSONSymbol.toSymbol("\"" + AbstractConstants.enumConstant + "\""));

        schema.getAllKeysDefinedInSchema().
            stream().
            map(k -> "\"" + k + "\"").
            map(k -> JSONSymbol.toSymbol(k)).
            forEach(k -> internalSymbols.add(k));

        return new DefaultVPDAlphabet<>(internalSymbols, callSymbols, returnSymbols);
    }
}
