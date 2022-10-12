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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.AbstractConstants;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.JSONSchemaStore;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.DefaultVPDAlphabet;

/**
 * A worst-case schema for the classical validator, and its corresponding
 * 1-SEVPA.
 * 
 * <p>
 * The idea of the schema is to create a conjunction of multiple disjunctions
 * S_i to S_l, with l a fixed parameter.
 * Each part of the conjunction removes one S_i, up until only S_l remains.
 * That is, while the schema is large, it can be reduced to only S_l.
 * In this version, each S_i is an object that contains the keys i to l, and
 * each value must be a string.
 * </p>
 * 
 * <p>
 * Since the classical validator implementation we rely on performs a special optimization for conjunctions, we instead negate a disjunction.
 * That is, instead of using "allOf", we use "not": { "anyOf": ... }.
 * </p>
 * 
 * <p>
 * The 1-SEVPA corresponding to the schema is pretty simple, as we only have to
 * consider S_l.
 * Thus, it can be generated directly without needing to learn it.
 * </p>
 * 
 * @author Gaëtan Staquet
 */
public class WorstCaseClassical {
    private WorstCaseClassical() {

    }

    /**
     * Constructs the schema for the given value for l.
     * 
     * @param store            The store that will hold the schema
     * @param numberOfElements The value of the parameter l
     * @return The schema
     * @throws JSONSchemaException
     */
    public static JSONSchema constructSchema(JSONSchemaStore store, int numberOfElements) throws JSONSchemaException {
        final JSONObject document = new JSONObject();
        document.put("not", constructNot(numberOfElements));
        document.put("type", "object");
        document.put("additionalProperties", constructEndObject());

        return store.loadFromJSONObject(document);
    }

    private static JSONObject constructNot(int numberOfElements) {
        final JSONArray inAnyOfInNot = new JSONArray();
        for (int i = 1; i <= numberOfElements; i++) {
            final JSONObject object = new JSONObject();
            object.put("not", constructAnyOf(i, numberOfElements));
            inAnyOfInNot.put(object);
        }

        final JSONObject anyOfInNot = new JSONObject();
        anyOfInNot.put("anyOf", inAnyOfInNot);
        return anyOfInNot;
    }

    private static JSONObject constructAnyOf(int startNumber, int endNumber) {
        final JSONArray anyOf = new JSONArray();
        for (int i = startNumber; i <= endNumber; i++) {
            final JSONObject properties = new JSONObject();
            final JSONArray requiredKeys = new JSONArray();
            for (int j = i; j <= endNumber; j++) {
                final String key = Integer.toString(j);
                properties.put(key, constructEndObject());
                requiredKeys.put(key);
            }

            final JSONObject inAnyOf = new JSONObject();
            inAnyOf.put("properties", properties);
            inAnyOf.put("required", requiredKeys);
            if (i > 1) {
                final JSONArray requiredOtherKeys = new JSONArray();
                for (int j = 1; j < i; j++) {
                    final String key = Integer.toString(j);
                    final JSONArray requiredArray = new JSONArray();
                    requiredArray.put(key);
                    final JSONObject required = new JSONObject();
                    required.put("required", requiredArray);
                    requiredOtherKeys.put(required);
                }

                final JSONObject anyOfOtherKeys = new JSONObject();
                anyOfOtherKeys.put("anyOf", requiredOtherKeys);

                inAnyOf.put("not", anyOfOtherKeys);
            }
            anyOf.put(inAnyOf);
        }

        final JSONObject object = new JSONObject();
        object.put("anyOf", anyOf);
        return object;
    }

    private static JSONObject constructEndObject() {
        JSONObject object = new JSONObject();
        object.put("type", "string");
        return object;
    }

    public static DefaultOneSEVPA<JSONSymbol> constructAutomaton(int numberOfElements) {
        final VPDAlphabet<JSONSymbol> alphabet = constructAlphabet(numberOfElements);
        final DefaultOneSEVPA<JSONSymbol> automaton = new DefaultOneSEVPA<>(alphabet);

        final List<Location> locations = new ArrayList<>();
        locations.add(automaton.addInitialLocation(false));
        locations.add(automaton.addLocation(false));
        locations.add(automaton.addLocation(false));
        locations.add(automaton.addLocation(true));
        locations.add(automaton.addLocation(false));
        locations.add(automaton.addLocation(false));
        locations.add(automaton.addLocation(false));
        locations.add(automaton.addLocation(false));
        final Location binLocation = locations.get(locations.size() - 1);

        final int stackSymQ0Curly = automaton.encodeStackSym(locations.get(0), JSONSymbol.openingCurlyBraceSymbol);
        automaton.setInternalSuccessor(locations.get(0), constructKeySymbol(numberOfElements), locations.get(1));

        automaton.setInternalSuccessor(locations.get(1), JSONSymbol.stringSymbol, locations.get(2));

        automaton.setReturnSuccessor(locations.get(2), JSONSymbol.closingCurlyBraceSymbol, stackSymQ0Curly,
                locations.get(3));
        automaton.setInternalSuccessor(locations.get(2), JSONSymbol.commaSymbol, locations.get(4));

        automaton.setInternalSuccessor(locations.get(4), constructAdditionalKeySymbol(), locations.get(5));

        automaton.setInternalSuccessor(locations.get(5), JSONSymbol.stringSymbol, locations.get(6));

        automaton.setReturnSuccessor(locations.get(6), JSONSymbol.closingCurlyBraceSymbol, stackSymQ0Curly,
                locations.get(3));

        for (final Location start : locations) {
            for (final JSONSymbol internalSymbol : alphabet.getInternalAlphabet()) {
                if (automaton.getInternalSuccessor(start, internalSymbol) == null) {
                    automaton.setInternalSuccessor(start, internalSymbol, binLocation);
                }
            }

            for (final JSONSymbol callSymbol : alphabet.getCallAlphabet()) {
                for (final Location beforeCall : locations) {
                    final int stackSymbol = automaton.encodeStackSym(beforeCall, callSymbol);
                    for (final JSONSymbol returnSymbol : alphabet.getReturnAlphabet()) {
                        if (automaton.getReturnSuccessor(start, returnSymbol, stackSymbol) == null) {
                            automaton.setReturnSuccessor(start, returnSymbol, stackSymbol, binLocation);
                        }
                    }
                }
            }
        }

        return automaton;
    }

    private static VPDAlphabet<JSONSymbol> constructAlphabet(int numberOfElements) {
        final Set<JSONSymbol> internalSymbols = new LinkedHashSet<>();
        final Set<JSONSymbol> callSymbols = new LinkedHashSet<>();
        final Set<JSONSymbol> returnSymbols = new LinkedHashSet<>();

        callSymbols.add(JSONSymbol.openingBracketSymbol);
        callSymbols.add(JSONSymbol.openingCurlyBraceSymbol);

        returnSymbols.add(JSONSymbol.closingBracketSymbol);
        returnSymbols.add(JSONSymbol.closingCurlyBraceSymbol);

        internalSymbols.add(JSONSymbol.commaSymbol);
        internalSymbols.add(JSONSymbol.trueSymbol);
        internalSymbols.add(JSONSymbol.falseSymbol);
        internalSymbols.add(JSONSymbol.nullSymbol);
        internalSymbols.add(JSONSymbol.stringSymbol);
        internalSymbols.add(JSONSymbol.integerSymbol);
        internalSymbols.add(JSONSymbol.numberSymbol);
        internalSymbols.add(JSONSymbol.enumSymbol);

        internalSymbols.add(constructAdditionalKeySymbol());
        // @formatter:off
        IntStream.range(1, numberOfElements + 1)
            .mapToObj(i -> constructKeySymbol(i))
            .forEach(key -> internalSymbols.add(key));
        // @formatter:on

        return new DefaultVPDAlphabet<>(internalSymbols, callSymbols, returnSymbols);
    }

    private static JSONSymbol constructAdditionalKeySymbol() {
        return JSONSymbol.toSymbol("\"" + AbstractConstants.stringConstant + "\":");
    }

    private static JSONSymbol constructKeySymbol(int keyNumber) {
        return JSONSymbol.toSymbol("\"" + keyNumber + "\":");
    }
}
