/*
 * ValidatingJSONDocumentsWithLearnedVPA - Learning a visibly pushdown automaton
 * from a JSON schema, and using it to validate JSON documents.
 *
 * Copyright 2022 University of Mons, University of Antwerp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.ac.umons.jsonvalidation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.testing.GcFinalization;

import net.automatalib.words.Word;

public class MemoryForDocuments {

    private final Path pathToDocuments;
    private final CSVPrinter memoryCSVPrinter;

    public MemoryForDocuments(Path pathToDocuments, Path pathToCSVFile) throws IOException {
        this.pathToDocuments = pathToDocuments;

        this.memoryCSVPrinter = new CSVPrinter(new FileWriter(pathToCSVFile.toFile()), CSVFormat.DEFAULT);
        memoryCSVPrinter.printRecord(getHeader());
        memoryCSVPrinter.flush();
    }

    private List<String> getHeader() {
        // @formatter:off
        return Arrays.asList(
            "Document ID",
            "Memory document",
            "Length document"
        );
        // @formatter:on
    }

    public void run() throws JSONException, IOException {
        File[] listFiles = pathToDocuments.toFile().listFiles();
        System.out.println(pathToDocuments);
        for (int i = 0; i < listFiles.length; i++) {
            final File file = listFiles[i];
            if (file.isFile()) {
                GcFinalization.awaitFullGc();
                final long memoryAtStart = ValidationBenchmarks.getMemoryUse();

                final JSONObject document = new JSONObject(new JSONTokener(new FileReader(file)));

                final long memoryAfterLoading = ValidationBenchmarks.getMemoryUse();

                final Word<JSONSymbol> word = WordConversion.fromJSONDocumentToJSONSymbolWord(document);

                final List<Object> values = new ArrayList<>();
                values.add(file.getName());
                values.add(memoryAfterLoading - memoryAtStart);
                values.add(word.length());

                memoryCSVPrinter.printRecord(values);
                memoryCSVPrinter.flush();
            }
        }
    }
}
