package be.ac.umons.jsonvalidation;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.generator.exploration.DefaultExplorationGenerator;
import be.ac.umons.jsonschematools.generator.exploration.ExplorationGenerator;
import be.ac.umons.jsonschematools.generator.random.DefaultRandomGenerator;
import be.ac.umons.jsonschematools.generator.random.GeneratorException;
import be.ac.umons.jsonschematools.generator.random.RandomGenerator;

public class GenerateDocuments {
    enum GenerationType {
        EXPLORATION,
        RANDOM
    }

    private final JSONSchema schema;
    private final String schemaName;
    private final GenerationType generationType;
    private final Path pathToDocuments;
    private final int nDocuments;
    private final int maxDocumentDepth;
    private final int maxProperties;
    private final int maxItems;

    public GenerateDocuments(JSONSchema schema, String schemaName, GenerationType generationType, Path pathToDocuments, int nDocuments, int maxDocumentDepth, int maxProperties, int maxItems) {
        this.schema = schema;
        this.schemaName = schemaName;
        this.generationType = generationType;
        this.pathToDocuments = pathToDocuments;
        this.nDocuments = nDocuments;
        this.maxDocumentDepth = maxDocumentDepth;
        this.maxProperties = maxProperties;
        this.maxItems = maxItems;
    }

    public void generate() throws IOException, JSONException, JSONSchemaException, GeneratorException {
        final Map<Integer, Integer> sizeToNumber = new HashMap<>();
        for (boolean valid : List.of(true, false)) {
            final Iterator<JSONObject> iterator = createIterator(valid);

            for (int i = 0 ; i < nDocuments && iterator.hasNext() ; i++) {
                System.out.println(valid + " " + (i+1) + " / " + nDocuments);
                final JSONObject document = iterator.next();
                final int length = WordConversion.fromJSONDocumentToJSONSymbolWord(document, false, new Random()).length();
                sizeToNumber.put(length, sizeToNumber.getOrDefault(length, 0) + 1);
                writeDocument(document, pathToDocuments, schemaName, i, valid);
            }
        }

        final List<Integer> sizes = new ArrayList<>(sizeToNumber.keySet());
        Collections.sort(sizes);
        for (final int size : sizes) {
            System.out.println("Documents of length " + size + ": " + sizeToNumber.get(size));
        }
    }

    private Iterator<JSONObject> createIterator(boolean valid) {
        switch (generationType) {
            case EXPLORATION:
                return createExplorationIterator(valid);
            case RANDOM:
                return createRandomIterator(new Random(1), valid);
            default:
                return null;
        }
    }

    private Iterator<JSONObject> createExplorationIterator(boolean valid) {
        final ExplorationGenerator generator = new DefaultExplorationGenerator(maxProperties, maxItems);
        return generator.createIterator(schema, maxDocumentDepth, valid);
    }

    private Iterator<JSONObject> createRandomIterator(Random random, boolean valid) {
        final RandomGenerator generator = new DefaultRandomGenerator(maxProperties, maxItems);
        return generator.createIterator(schema, maxDocumentDepth, valid, random);
    }

    private static void writeDocument(final JSONObject document, final Path pathToDocuments, final String schemaName, final int id, final boolean valid) throws IOException {
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(schemaName);
        if (valid) {
            fileNameBuilder.append("-valid-");
        }
        else {
            fileNameBuilder.append("-invalid-");
        }
        fileNameBuilder.append(id);
        fileNameBuilder.append(".json");
        final Path documentPath = pathToDocuments.resolve(fileNameBuilder.toString());
        FileWriter writer = new FileWriter(documentPath.toFile());
        writer.write(document.toString(4));
        writer.close();
    }
}
