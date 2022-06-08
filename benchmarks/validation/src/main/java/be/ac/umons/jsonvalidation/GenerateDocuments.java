package be.ac.umons.jsonvalidation;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.exploration.DefaultExplorationGenerator;
import be.ac.umons.jsonschematools.exploration.ExplorationGenerator;
import be.ac.umons.jsonschematools.random.DefaultRandomGenerator;
import be.ac.umons.jsonschematools.random.GeneratorException;
import be.ac.umons.jsonschematools.random.RandomGenerator;

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
    private final boolean canGenerateInvalid;
    private final int maxDocumentDepth;
    private final int maxProperties;
    private final int maxItems;

    public GenerateDocuments(JSONSchema schema, String schemaName, GenerationType generationType, Path pathToDocuments, int nDocuments, boolean canGenerateInvalid, int maxDocumentDepth, int maxProperties, int maxItems) {
        this.schema = schema;
        this.schemaName = schemaName;
        this.generationType = generationType;
        this.pathToDocuments = pathToDocuments;
        this.nDocuments = nDocuments;
        this.canGenerateInvalid = canGenerateInvalid;
        this.maxDocumentDepth = maxDocumentDepth;
        this.maxProperties = maxProperties;
        this.maxItems = maxItems;
    }

    public void generate() throws IOException, JSONException, JSONSchemaException, GeneratorException {
        Iterator<JSONObject> iterator;
        switch (generationType) {
            case EXPLORATION:
                iterator = createExplorationIterator();
                break;
            case RANDOM:
                iterator = createRandomIterator(new Random(1));
                break;
            default:
                return;
        }

        for (int i = 0 ; i < nDocuments && iterator.hasNext() ; i++) {
            final JSONObject document = iterator.next();
            writeDocument(document, pathToDocuments, schemaName, i);
        }
    }

    private Iterator<JSONObject> createExplorationIterator() {
        final ExplorationGenerator generator = new DefaultExplorationGenerator(maxProperties, maxItems);
        return generator.createIterator(schema, maxDocumentDepth, canGenerateInvalid);
    }

    private Iterator<JSONObject> createRandomIterator(Random random) {
        final RandomGenerator generator = new DefaultRandomGenerator(maxProperties, maxItems);
        return generator.createIterator(schema, maxDocumentDepth, canGenerateInvalid, random);
    }

    private static void writeDocument(final JSONObject document, final Path pathToDocuments, final String schemaName, final int id) throws IOException {
        final Path documentPath = pathToDocuments.resolve(schemaName + "" + id + ".json");
        FileWriter writer = new FileWriter(documentPath.toFile());
        writer.write(document.toString(4));
        writer.close();
    }
}
