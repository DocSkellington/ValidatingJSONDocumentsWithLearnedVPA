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

import static be.ac.umons.jsonvalidation.JSONSymbol.toSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Utility functions to convert a word of JSON symbols to a regular string, and
 * vice-versa.
 * 
 * @author Gaëtan Staquet
 */
public class WordConversion {
    public static Word<JSONSymbol> fromJSONDocumentToJSONSymbolWord(JSONObject document) {
        return fromJSONDocumentToJSONSymbolWord(document, false, new Random());
    }

    public static Word<JSONSymbol> fromJSONDocumentToJSONSymbolWord(JSONObject document, boolean shuffleKeys,
            Random rand) {
        WordBuilder<JSONSymbol> wordBuilder = new WordBuilder<>();
        wordBuilder.add(toSymbol("{"));
        fromJSONObjectToJSONWord(document, shuffleKeys, rand, wordBuilder);
        wordBuilder.add(toSymbol("}"));
        return wordBuilder.toWord();
    }

    private static void fromJSONObjectToJSONWord(JSONObject object, boolean shuffleKeys, Random rand,
            WordBuilder<JSONSymbol> wordBuilder) {
        List<String> keys = new ArrayList<>(object.keySet());
        if (shuffleKeys) {
            Collections.shuffle(keys, rand);
        }
        boolean first = true;
        for (String key : keys) {
            if (!first) {
                wordBuilder.add(JSONSymbol.commaSymbol);
            }
            first = false;
            wordBuilder.add(toSymbol("\"" + key + "\":"));

            Object o = object.get(key);
            if (o instanceof JSONObject) {
                wordBuilder.add(toSymbol("{"));
                fromJSONObjectToJSONWord((JSONObject) o, shuffleKeys, rand, wordBuilder);
                wordBuilder.add(toSymbol("}"));
            } else if (o instanceof JSONArray) {
                wordBuilder.add(toSymbol("["));
                fromJSONArrayToJSONWord((JSONArray) o, shuffleKeys, rand, wordBuilder);
                wordBuilder.add(toSymbol("]"));
            } else if (o instanceof Boolean) {
                wordBuilder.add(toSymbol(o.toString()));
            } else {
                if (Objects.equals(o, JSONObject.NULL)) {
                    wordBuilder.add(toSymbol(o.toString()));
                } else {
                    wordBuilder.add(toSymbol("\"" + o.toString() + "\""));
                }
            }
        }
    }

    private static void fromJSONArrayToJSONWord(JSONArray array, boolean shuffleKeys, Random rand,
            WordBuilder<JSONSymbol> wordBuilder) {
        boolean first = true;
        for (Object o : array) {
            if (!first) {
                wordBuilder.add(JSONSymbol.commaSymbol);
            }
            first = false;
            if (o instanceof JSONObject) {
                wordBuilder.add(toSymbol("{"));
                fromJSONObjectToJSONWord((JSONObject) o, shuffleKeys, rand, wordBuilder);
                wordBuilder.add(toSymbol("}"));
            } else if (o instanceof JSONArray) {
                wordBuilder.add(toSymbol("["));
                fromJSONArrayToJSONWord((JSONArray) o, shuffleKeys, rand, wordBuilder);
                wordBuilder.add(toSymbol("]"));
            } else if (o instanceof Boolean) {
                wordBuilder.add(toSymbol(o.toString()));
            } else if (Objects.equals(o, JSONObject.NULL)) {
                wordBuilder.add(toSymbol(o.toString()));
            } else {
                wordBuilder.add(toSymbol("\"" + o.toString() + "\""));
            }
        }
    }

    public static String fromJSONSymbolWordToString(Word<JSONSymbol> word) {
        StringBuilder stringBuilder = new StringBuilder();
        for (JSONSymbol symbol : word) {
            String string = symbol.toString();
            for (int i = 0; i < string.length(); i++) {
                stringBuilder.append(string.charAt(i));
            }
        }
        return stringBuilder.toString();
    }
}
