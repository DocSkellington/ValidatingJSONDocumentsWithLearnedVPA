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
package be.ac.umons.vpdajson.oracles;

import static be.ac.umons.jsonroca.JSONSymbol.toSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import be.ac.umons.jsonroca.JSONSymbol;
import be.ac.umons.vpdajson.algorithm.GrowingVPDAlphabet;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.VPDAlphabet.SymbolType;

/**
 * Utility functions to convert a character word (or a string) to a JSONSymbol
 * word, and vice-versa.
 * 
 * @author Gaëtan Staquet
 */
public class WordConversion {
    private static boolean addSymbol(GrowingVPDAlphabet<JSONSymbol> alphabet, String symbol, WordBuilder<JSONSymbol> wordBuilder) {
        SymbolType type = SymbolType.INTERNAL;
        if (symbol.contains("{") || symbol.contains("[")) {
            type = SymbolType.CALL;
        }
        if (symbol.contains("}") || symbol.contains("]")) {
            type = SymbolType.CALL;
        }
        JSONSymbol sym = toSymbol(symbol);
        boolean newSymbol = alphabet.addNewSymbol(sym, type);
        wordBuilder.add(sym);
        assert !newSymbol : symbol;
        return newSymbol;
    }

    public static Pair<Word<JSONSymbol>, Boolean> fromJSONDocumentToJSONSymbolWord(GrowingVPDAlphabet<JSONSymbol> alphabet, JSONObject document, boolean shuffleKeys, Random rand) {
        WordBuilder<JSONSymbol> wordBuilder = new WordBuilder<>();
        addSymbol(alphabet, "{", wordBuilder);
        boolean newSymbol = fromJSONObjectToJSONWord(alphabet, document, shuffleKeys, rand, wordBuilder);
        addSymbol(alphabet, "}", wordBuilder);
        return Pair.of(wordBuilder.toWord(), newSymbol);
    }

    public static Pair<Word<JSONSymbol>, Boolean> fromJSONDocumentToJSONSymbolWord(GrowingVPDAlphabet<JSONSymbol> alphabet, JSONObject document, boolean shuffleKeys) {
        return fromJSONDocumentToJSONSymbolWord(alphabet, document, shuffleKeys, new Random());
    }

    public static Pair<Word<JSONSymbol>, Boolean> fromJSONDocumentToJSONSymbolWord(GrowingVPDAlphabet<JSONSymbol> alphabet, JSONObject document) {
        return fromJSONDocumentToJSONSymbolWord(alphabet, document, true);
    }

    private static boolean fromJSONObjectToJSONWord(GrowingVPDAlphabet<JSONSymbol> alphabet, JSONObject object, boolean shuffleKeys, Random rand, WordBuilder<JSONSymbol> wordBuilder) {
        List<String> keys = new ArrayList<>(object.keySet());
        if (shuffleKeys) {
            Collections.shuffle(keys, rand);
        }
        boolean first =  true;
        boolean newSymbol = false;
        for (String key : keys) {
            if (!first) {
                newSymbol = addSymbol(alphabet, ",", wordBuilder) || newSymbol;
            }
            first = false;
            newSymbol = addSymbol(alphabet, "\"" + key + "\"", wordBuilder) || newSymbol;

            Object o = object.get(key);
            if (o instanceof JSONObject) {
                newSymbol = addSymbol(alphabet, ":{", wordBuilder) || newSymbol;
                newSymbol = fromJSONObjectToJSONWord(alphabet, (JSONObject) o, shuffleKeys, rand, wordBuilder) || newSymbol;
                newSymbol = addSymbol(alphabet, "}", wordBuilder) || newSymbol;
            }
            else if (o instanceof JSONArray) {
                newSymbol = addSymbol(alphabet, ":[", wordBuilder) || newSymbol;
                newSymbol = fromJSONArrayToJSONWord(alphabet, (JSONArray) o, shuffleKeys, rand, wordBuilder) || newSymbol;
                newSymbol = addSymbol(alphabet, "]", wordBuilder) || newSymbol;
            }
            else if (o instanceof Boolean) {
                newSymbol = addSymbol(alphabet, ":", wordBuilder) || newSymbol;
                newSymbol = addSymbol(alphabet, o.toString(), wordBuilder) || newSymbol;
            }
            else {
                newSymbol = addSymbol(alphabet, ":", wordBuilder) || newSymbol;
                newSymbol = addSymbol(alphabet, "\"" + o.toString() + "\"", wordBuilder) || newSymbol;
            }
        }

        return newSymbol;
    }

    private static boolean fromJSONArrayToJSONWord(GrowingVPDAlphabet<JSONSymbol> alphabet, JSONArray array, boolean shuffleKeys, Random rand, WordBuilder<JSONSymbol> wordBuilder) {
        boolean first = true;
        boolean newSymbol = false;
        for (Object o : array) {
            if (!first) {
                newSymbol = addSymbol(alphabet, ",", wordBuilder) || newSymbol;
            }
            first = false;
            if (o instanceof JSONObject) {
                newSymbol = addSymbol(alphabet, "{", wordBuilder) || newSymbol;
                newSymbol = fromJSONObjectToJSONWord(alphabet, (JSONObject) o, shuffleKeys, rand, wordBuilder) || newSymbol;
                newSymbol = addSymbol(alphabet, "}", wordBuilder) || newSymbol;
            }
            else if (o instanceof JSONArray) {
                newSymbol = addSymbol(alphabet, "[", wordBuilder) || newSymbol;
                newSymbol = fromJSONArrayToJSONWord(alphabet, (JSONArray) o, shuffleKeys, rand, wordBuilder) || newSymbol;
                newSymbol = addSymbol(alphabet, "]", wordBuilder) || newSymbol;
            }
            else if (o instanceof Boolean) {
                newSymbol = addSymbol(alphabet, o.toString(), wordBuilder) || newSymbol;
            }
            else {
                newSymbol = addSymbol(alphabet, "\"" + o.toString() + "\"", wordBuilder) || newSymbol;
            }
        }

        return newSymbol;
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
