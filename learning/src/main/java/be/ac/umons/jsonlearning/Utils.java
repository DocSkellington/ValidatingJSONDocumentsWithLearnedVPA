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

import net.automatalib.words.Word;

/**
 * Utility functions for oracles.
 * 
 * @author Gaëtan Staquet
 */
public class Utils {
    private Utils() {
    }

    /**
     * Converts an Automatalib's Word into a Java String.
     * 
     * @param word The word
     * @return The string constructed from the word.
     */
    public static String wordToString(final Word<Character> word) {
        StringBuilder builder = new StringBuilder(word.size());
        for (Character character : word) {
            builder.append(character);
        }
        return builder.toString();
    }

    /**
     * Counts the number of unmatched { and [ in a String.
     * 
     * @param word The string
     * @return The number of unmatched { and [
     */
    public static int countUnmatched(final String word) {
        int numberUnmatchedOpen = 0;
        boolean inString = false;
        boolean previousWasEscape = false;
        for (int i = 0; i < word.length(); i++) {
            char character = word.charAt(i);
            if (!previousWasEscape && (character == '"' || character == '\'')) {
                inString = !inString;
            }

            if (!inString && (character == '{' || character == '[')) {
                numberUnmatchedOpen++;
            } else if (!inString && (character == '}' || character == ']')) {
                numberUnmatchedOpen--;
            }
            previousWasEscape = (character == '\\');
        }

        return numberUnmatchedOpen;
    }

    /**
     * Tests whether the provided string encodes a valid JSON document.
     * 
     * That is, the string must begin by {@code {} and end by {@code \}}.
     * 
     * @param word
     * @return
     */
    public static boolean validWord(final String word) {
        if (word.isEmpty() || word.charAt(0) != '{' || word.charAt(word.length() - 1) != '}') {
            return false;
        }

        int numberUnmatchedOpen = 0;
        boolean firstObject = true;
        boolean inString = false;
        boolean previousWasEscape = false;
        boolean previousWasComma = false;
        for (int i = 0; i < word.length(); i++) {
            char character = word.charAt(i);
            if (numberUnmatchedOpen == 0 && !firstObject) {
                return false;
            }
            if (!previousWasEscape && (character == '"' || character == '\'')) {
                inString = !inString;
            }
            if (!inString) {
                if (character == '{' || character == '[') {
                    if (numberUnmatchedOpen == 0) {
                        firstObject = false;
                    }
                    numberUnmatchedOpen++;
                    previousWasComma = false;
                } else if (character == '}' || character == ']') {
                    if (numberUnmatchedOpen == 0) {
                        return false;
                    }
                    if (previousWasComma) {
                        return false;
                    }
                    numberUnmatchedOpen--;
                    previousWasComma = false;
                } else if (character == ',') {
                    if (previousWasComma) {
                        return false;
                    }
                    previousWasComma = true;
                } else {
                    previousWasComma = false;
                }
            }

            previousWasEscape = (character == '\\');
        }

        return true;
    }

    /**
     * We escape the "\S", "\E", "\I", and "\D" symbols in the document (to avoid
     * errors from JSONObject).
     * 
     * That means we replace every \\([SIDE]) by \\\\$1.
     * 
     * @param string The string to escape
     * @return The escaped string
     */
    public static String escapeSymbolsForJSON(String string) {
        // We need to escape each \ in the Java code.
        return string.replaceAll("\\\\([SIDE])", "\\\\\\\\$1");
    }
}
