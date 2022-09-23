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
package be.ac.umons.jsonvalidation;

import static be.ac.umons.jsonvalidation.JSONSymbol.toSymbol;

import java.util.Arrays;
import java.util.Random;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.automatalib.words.Word;

/**
 * @author Gaëtan Staquet
 */
public class WordConversionTests {
    private static void assertEqualsOr(Object actual, Object... expected) {
        Assert.assertTrue(Arrays.asList(expected).contains(actual));
    }

    @Test
    public void fromStringToSymbols() {
        String base = "{\"string\": \"hello\"}";
        Word<JSONSymbol> result = WordConversion.fromJSONDocumentToJSONSymbolWord(new JSONObject(base), false,
                new Random());
        Word<JSONSymbol> target = Word.fromSymbols(toSymbol("{"), toSymbol("\"string\":"), toSymbol("\"hello\""),
                toSymbol("}"));
        Assert.assertEquals(result, target);

        base = "{\"pro\":{\"i\":\"\\\\I\"},\"other\":\"\\\\D\"}";
        result = WordConversion.fromJSONDocumentToJSONSymbolWord(new JSONObject(base), false, new Random());
        Word<JSONSymbol> target1 = target = JSONSymbol.toWord("{", "\"pro\":", "{", "\"i\":", "\"\\I\"", "}", ",",
                "\"other\":", "\"\\D\"", "}");
        Word<JSONSymbol> target2 = target = JSONSymbol.toWord("{", "\"other\":", "\"\\D\"", ",", "\"pro\":", "{",
                "\"i\":", "\"\\I\"", "}", "}");
        assertEqualsOr(result, target1, target2);

        // We ignore some spaces
        base = "{ \"pro\" : { \"i\" : \"\\\\I\" } , \"other\" : \"\\\\D\" }";
        result = WordConversion.fromJSONDocumentToJSONSymbolWord(new JSONObject(base), false, new Random());
        assertEqualsOr(result, target1, target2);

        // Arrays and booleans
        base = "{\"arrays\": [\"\\\\I\", \"\\\\I\", \"\\\\I\"], \"boolean\": false, \"other\": true}";
        result = WordConversion.fromJSONDocumentToJSONSymbolWord(new JSONObject(base), false, new Random());
        target1 = JSONSymbol.toWord("{", "\"arrays\":", "[", "\"\\I\"", ",", "\"\\I\"", ",", "\"\\I\"", "]", ",",
                "\"boolean\":", "false", ",", "\"other\":", "true", "}");
        target2 = JSONSymbol.toWord("{", "\"arrays\":", "[", "\"\\I\"", ",", "\"\\I\"", ",", "\"\\I\"", "]", ",",
                "\"other\":", "true", ",", "\"boolean\":", "false", "}");
        Word<JSONSymbol> target3 = JSONSymbol.toWord("{", "\"boolean\":", "false", ",", "\"arrays\":", "[", "\"\\I\"",
                ",", "\"\\I\"", ",", "\"\\I\"", "]", ",", "\"other\":", "true", "}");
        Word<JSONSymbol> target4 = JSONSymbol.toWord("{", "\"other\":", "true", ",", "\"arrays\":", "[", "\"\\I\"", ",",
                "\"\\I\"", ",", "\"\\I\"", "]", ",", "\"boolean\":", "false", "}");
        Word<JSONSymbol> target5 = JSONSymbol.toWord("{", "\"boolean\":", "false", ",", "\"other\":", "true", ",",
                "\"arrays\":", "[", "\"\\I\"", ",", "\"\\I\"", ",", "\"\\I\"", "]", "}");
        Word<JSONSymbol> target6 = JSONSymbol.toWord("{", "\"other\":", "true", ",", "\"boolean\":", "false", ",",
                "\"arrays\":", "[", "\"\\I\"", ",", "\"\\I\"", ",", "\"\\I\"", "]", "}");
        assertEqualsOr(result, target1, target2, target3, target4, target5, target6);

        // Empty array
        base = "{\"array\": [], \"other\": false}";
        result = WordConversion.fromJSONDocumentToJSONSymbolWord(new JSONObject(base), false, new Random());
        target1 = JSONSymbol.toWord("{", "\"array\":", "[", "]", ",", "\"other\":", "false", "}");
        target2 = JSONSymbol.toWord("{", "\"other\":", "false", ",", "\"array\":", "[", "]", "}");
        assertEqualsOr(result, target1, target2);

        // Empty object
        base = "{\"obj\": {}}";
        result = WordConversion.fromJSONDocumentToJSONSymbolWord(new JSONObject(base), false, new Random());
        target = JSONSymbol.toWord("{", "\"obj\":", "{", "}", "}");
        Assert.assertEquals(result, target);

        // Closing two objects in a row
        base = "{\"obj\": {\"obj\": {}}}";
        result = WordConversion.fromJSONDocumentToJSONSymbolWord(new JSONObject(base), false, new Random());
        target = JSONSymbol.toWord("{", "\"obj\":", "{", "\"obj\":", "{", "}", "}", "}");
        Assert.assertEquals(result, target);
    }

    @Test
    public void fromSymbolToCharacter() {
        Word<JSONSymbol> base = Word.fromSymbols(toSymbol("h"), toSymbol('e'), toSymbol('l'), toSymbol('l'),
                toSymbol('o'));
        String result = WordConversion.fromJSONSymbolWordToString(base);
        String target = "hello";
        Assert.assertEquals(result, target);

        base = Word.fromSymbols(toSymbol('"'), toSymbol('p'), toSymbol('r'), toSymbol('o'), toSymbol('"'),
                toSymbol(':'), toSymbol('{'), toSymbol('"'), toSymbol('i'), toSymbol("\":"), toSymbol('1'),
                toSymbol('}'));
        result = WordConversion.fromJSONSymbolWordToString(base);
        target = "\"pro\":{\"i\":1}";
        Assert.assertEquals(result, target);
    }
}
