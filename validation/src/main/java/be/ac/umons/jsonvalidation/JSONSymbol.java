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

import java.util.List;
import java.util.Objects;

import be.ac.umons.jsonschematools.AbstractConstants;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.abstractimpl.AbstractSymbol;
import net.automatalib.words.impl.Alphabets;

/**
 * A symbol for JSON documents.
 * 
 * @author Gaëtan Staquet
 */
public class JSONSymbol extends AbstractSymbol<JSONSymbol> {

    public static final JSONSymbol commaSymbol = JSONSymbol.toSymbol(",");
    public static final JSONSymbol openingCurlyBraceSymbol = JSONSymbol.toSymbol("{");
    public static final JSONSymbol closingCurlyBraceSymbol = JSONSymbol.toSymbol("}");
    public static final JSONSymbol openingBracketSymbol = JSONSymbol.toSymbol("[");
    public static final JSONSymbol closingBracketSymbol = JSONSymbol.toSymbol("]");
    public static final JSONSymbol nullSymbol = JSONSymbol.toSymbol("null");
    public static final JSONSymbol integerSymbol = JSONSymbol.toSymbol("\"" + AbstractConstants.integerConstant + "\"");
    public static final JSONSymbol numberSymbol = JSONSymbol.toSymbol("\"" + AbstractConstants.numberConstant + "\"");
    public static final JSONSymbol stringSymbol = JSONSymbol.toSymbol("\"" + AbstractConstants.stringConstant + "\"");
    public static final JSONSymbol enumSymbol = JSONSymbol.toSymbol("\"" + AbstractConstants.enumConstant + "\"");
    public static final JSONSymbol trueSymbol = JSONSymbol.toSymbol("true");
    public static final JSONSymbol falseSymbol = JSONSymbol.toSymbol("false");
    public static final Alphabet<JSONSymbol> primitiveValuesAlphabet;

    static {
        // @formatter:off
        final List<JSONSymbol> primitiveValuesSymbols = List.of(
            JSONSymbol.nullSymbol,
            JSONSymbol.integerSymbol,
            JSONSymbol.numberSymbol,
            JSONSymbol.stringSymbol,
            JSONSymbol.enumSymbol,
            JSONSymbol.trueSymbol,
            JSONSymbol.falseSymbol
        );
        // @formatter:on
        primitiveValuesAlphabet = Alphabets.fromList(primitiveValuesSymbols);
    }

    private final String actualSymbols;

    private JSONSymbol(String actualSymbols) {
        this.actualSymbols = actualSymbols;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof JSONSymbol)) {
            return false;
        }
        JSONSymbol o = (JSONSymbol) obj;
        return Objects.equals(actualSymbols, o.actualSymbols);
    }

    @Override
    public int compareTo(JSONSymbol other) {
        return actualSymbols.compareTo(other.actualSymbols);
    }

    public JSONSymbol callToReturn() {
        if (Objects.equals(this, openingCurlyBraceSymbol)) {
            return closingCurlyBraceSymbol;
        }
        else if (Objects.equals(this, openingBracketSymbol)) {
            return closingBracketSymbol;
        }
        else {
            return null;
        }
    }

    public static JSONSymbol toSymbol(String string) {
        return new JSONSymbol(string);
    }

    public static JSONSymbol toSymbol(Character character) {
        return new JSONSymbol(Character.toString(character));
    }

    public static Word<JSONSymbol> toWord(String... symbols) {
        WordBuilder<JSONSymbol> wordBuilder = new WordBuilder<>(symbols.length);
        for (String symbol : symbols) {
            wordBuilder.add(toSymbol(symbol));
        }
        return wordBuilder.toWord();
    }

    @Override
    public String toString() {
        return actualSymbols;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actualSymbols);
    }

}
