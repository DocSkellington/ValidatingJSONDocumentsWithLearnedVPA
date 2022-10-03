package be.ac.umons.jsonvalidation;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.generator.random.GeneratorException;

public class GenerateWorstCaseDocuments extends GenerateDocuments {

    private static final String SCHEMA_NAME = "WorstCase";

    private final int numberOfElements;

    public GenerateWorstCaseDocuments(Path pathToDocuments, int nDocuments, int maxDocumentDepth, int maxProperties,
            int maxItems, int numberOfElements) {
        super(null, null, null, pathToDocuments, nDocuments, maxDocumentDepth, maxProperties, maxItems);
        this.numberOfElements = numberOfElements;
    }

    @Override
    public void generate() throws IOException, JSONException, JSONSchemaException, GeneratorException {
        final Iterator<Set<Integer>> selectedKeysIterator = iteratorOverKeys();
        int id = 0;
        while (selectedKeysIterator.hasNext()) {
            final Set<Integer> selectedKeys = selectedKeysIterator.next();
            for (final boolean additional : List.of(false, true)) {
                final JSONObject document = createObject(selectedKeys, additional);
                writeDocument(document, pathToDocuments, id++);
            }
        }
    }

    private JSONObject createObject(final Set<Integer> selectedKeys, boolean additional) {
        final JSONObject object = new JSONObject();
        for (final int key : selectedKeys) {
            object.put(Integer.toString(key), AbstractConstants.stringConstant);
        }

        if (additional) {
            object.put(AbstractConstants.stringConstant, AbstractConstants.stringConstant);
        }

        return object;
    }

    private static void writeDocument(final JSONObject document, final Path pathToDocuments, final int id) throws IOException {
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(SCHEMA_NAME);
        fileNameBuilder.append(id);
        fileNameBuilder.append(".json");
        final Path documentPath = pathToDocuments.resolve(fileNameBuilder.toString());
        FileWriter writer = new FileWriter(documentPath.toFile());
        writer.write(document.toString(4));
        writer.close();
    }

    private Iterator<Set<Integer>> iteratorOverKeys() {
        return new IteratorOverKeys(numberOfElements);
    }

    private class IteratorOverKeys implements Iterator<Set<Integer>> {

        private final List<Integer> keys;
        private final BitSet selectedKeys;
        private boolean firstTime = true;

        public IteratorOverKeys(int numberOfElements) {
            this.keys = new ArrayList<>(numberOfElements);
            for (int i = 1; i <= numberOfElements; i++) {
                keys.add(i);
            }
            selectedKeys = new BitSet(numberOfElements);
        }

        @Override
        public boolean hasNext() {
            return selectedKeys.cardinality() != keys.size();
        }

        @Override
        public Set<Integer> next() {
            if (firstTime) {
                firstTime = false;
                return Collections.emptySet();
            }

            addOne();
            return selectedKeys.stream().mapToObj(i -> keys.get(i)).collect(Collectors.toSet());
        }

        private void addOne() {
            for (int i = 0; i < keys.size(); i++) {
                if (selectedKeys.get(i)) {
                    selectedKeys.set(i, false);
                } else {
                    selectedKeys.set(i, true);
                    return;
                }
            }
        }

    }
}
