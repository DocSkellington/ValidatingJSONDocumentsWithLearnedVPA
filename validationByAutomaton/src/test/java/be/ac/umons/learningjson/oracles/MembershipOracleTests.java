/* Copyright (C) 2021 – University of Mons, University Antwerpen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ac.umons.learningjson.oracles;

import static be.ac.umons.learningjson.JSONSymbol.toSymbol;
import static be.ac.umons.learningjson.JSONSymbol.toWord;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.words.Word;


/**
 * @author Gaëtan Staquet
 */
public class MembershipOracleTests {
    private JSONMembershipOracle createOracle(URL schemaURL) throws FileNotFoundException, JSONSchemaException, URISyntaxException {
        JSONSchemaStore schemaStore = new JSONSchemaStore();
        JSONSchema schema = schemaStore.load(schemaURL.toURI());
        return new JSONMembershipOracle(schema);
    }

    @Test
    public void testEscape() {
        String document = "{\"object\":{\"str1\": \"\\S\", \"in\\t\": \\I}, \"\\D\": \\D}";
        String escaped = Utils.escapeSymbolsForJSON(document);
        Assert.assertEquals(escaped, "{\"object\":{\"str1\": \"\\\\S\", \"in\\t\": \\\\I}, \"\\\\D\": \\\\D}");
    }

    @Test
    public void testSimpleStringSchema() throws URISyntaxException, FileNotFoundException, JSONSchemaException {
        URL schemaURL = getClass().getResource("/singleString.json");
        JSONMembershipOracle oracle = createOracle(schemaURL);

        Word<JSONSymbol> word = Word.epsilon();
        Assert.assertFalse(oracle.answerQuery(word));

        word = Word.fromSymbols(toSymbol("{"), toSymbol("}"));
        Assert.assertFalse(oracle.answerQuery(word));

        word = toWord("{", "\"string\":", "\"\\S\"", "}");
        Assert.assertTrue(oracle.answerQuery(word));
    }

    @Test
    public void testNumbersSchema() throws URISyntaxException, FileNotFoundException, JSONSchemaException {
        URL schemaURL = getClass().getResource("/numbers.json");
        JSONMembershipOracle oracle = createOracle(schemaURL);
        
        Word<JSONSymbol> word = toWord("{", "\"integer\":", "\"\\I\"", "}");
        Assert.assertFalse(oracle.answerQuery(word));

        word = toWord("{", "\"integer\":", "\"\\I\"", ",", "\"double\":", "\"\\D\"", "}");
        Assert.assertTrue(oracle.answerQuery(word));
    }
}
