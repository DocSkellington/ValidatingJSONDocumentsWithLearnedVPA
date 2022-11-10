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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import be.ac.umons.jsonschematools.AbstractConstants;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.abstractimpl.AbstractSymbol;
import net.automatalib.words.impl.Alphabets;

/**
 * An abstracted symbol used in JSON documents learning and validation.
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
    /**
     * Contains the symbols for enum, false, integer, null, number, string, and
     * true.
     */
    public static final Alphabet<JSONSymbol> primitiveValuesAlphabet;

    static {
        // @formatter:off
        final List<JSONSymbol> primitiveValuesSymbols = Arrays.asList(
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

    private final String actualSymbol;

    private JSONSymbol(String actualSymbol) {
        this.actualSymbol = actualSymbol;
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
        return Objects.equals(actualSymbol, o.actualSymbol);
    }

    @Override
    public int compareTo(JSONSymbol other) {
        return actualSymbol.compareTo(other.actualSymbol);
    }

    /**
     * If the current symbol is a call, returns the corresponding return symbol.
     * 
     * More explicitly, if the current symbol is { (resp. [), returns } (resp. ]).
     * 
     * @return Null, or the symbols }, ]
     */
    @Nullable
    public JSONSymbol callToReturn() {
        if (Objects.equals(this, openingCurlyBraceSymbol)) {
            return closingCurlyBraceSymbol;
        } else if (Objects.equals(this, openingBracketSymbol)) {
            return closingBracketSymbol;
        } else {
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
        return actualSymbol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actualSymbol);
    }

}
