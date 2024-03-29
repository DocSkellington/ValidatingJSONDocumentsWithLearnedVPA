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

package be.ac.umons.jsonlearning;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.validator.DefaultValidator;
import be.ac.umons.jsonschematools.validator.Validator;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.WordConversion;
import de.learnlib.api.oracle.SingleQueryOracle.SingleQueryOracleROCA;
import net.automatalib.words.Word;

/**
 * Membership oracle for JSON documents.
 * 
 * It checks whether a provided document is accepted by the JSON Schema.
 * 
 * @author Gaëtan Staquet
 */
public class JSONMembershipOracle implements SingleQueryOracleROCA<JSONSymbol> {

    private final JSONSchema schema;
    private final Validator validator;

    public JSONMembershipOracle(JSONSchema schema) {
        this.schema = schema;
        this.validator = new DefaultValidator();
    }

    @Override
    public Boolean answerQuery(Word<JSONSymbol> input) {
        String string = WordConversion.fromJSONSymbolWordToString(input);
        if (!Utils.validWord(string)) {
            return false;
        }
        string = Utils.escapeSymbolsForJSON(string);
        JSONObject json;
        try {
            json = new JSONObject(string);
        } catch (JSONException e) {
            return false;
        }

        final Word<JSONSymbol> wordFromDocument = WordConversion.fromJSONDocumentToJSONSymbolWord(json, false,
                new Random());
        if (!wordFromDocument.equals(input)) {
            return false;
        }

        try {
            return validator.validate(schema, json);
        } catch (JSONSchemaException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    @Override
    public Boolean answerQuery(Word<JSONSymbol> prefix, Word<JSONSymbol> suffix) {
        return answerQuery(prefix.concat(suffix));
    }
}
