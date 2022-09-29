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

public class WorstCaseClassical {
    private WorstCaseClassical() {

    }

    public static JSONSchema constructSchema(JSONSchemaStore store, int numberOfElements) throws JSONSchemaException {
        final JSONObject document = constructAllOf(numberOfElements);
        document.put("type", "object");
        document.put("additionalProperties", false);

        return store.loadFromJSONObject(document);
    }

    private static JSONObject constructAllOf(int numberOfElements) {
        final JSONArray allOf = new JSONArray();
        for (int i = 1; i <= numberOfElements; i++) {
            allOf.put(constructAnyOf(i, numberOfElements));
        }
        final JSONObject object = new JSONObject();
        object.put("allOf", allOf);
        return object;
    }

    private static JSONObject constructAnyOf(int startNumber, int endNumber) {
        final JSONArray anyOf = new JSONArray();
        for (int i = startNumber; i <= endNumber; i++) {
            final JSONObject properties = new JSONObject();
            for (int j = i; j <= endNumber; j++) {
                final JSONObject property = new JSONObject();
                property.put("type", "string");
                properties.put(Integer.toString(j), property);
            }

            final JSONObject inAnyOf = new JSONObject();
            inAnyOf.put("properties", properties);
            anyOf.put(inAnyOf);
        }
        final JSONObject object = new JSONObject();
        object.put("anyOf", anyOf);
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

        final int stackSymQ0Curly = automaton.encodeStackSym(locations.get(0), JSONSymbol.openingCurlyBraceSymbol);
        automaton.setInternalSuccessor(locations.get(0), constructKeySymbol(numberOfElements), locations.get(1));
        automaton.setReturnSuccessor(locations.get(0), JSONSymbol.closingCurlyBraceSymbol, stackSymQ0Curly, locations.get(3));
        automaton.setInternalSuccessor(locations.get(1), JSONSymbol.stringSymbol, locations.get(2));
        automaton.setReturnSuccessor(locations.get(2), JSONSymbol.closingCurlyBraceSymbol, stackSymQ0Curly, locations.get(3));

        for (final Location start : locations) {
            for (final JSONSymbol internalSymbol : alphabet.getInternalAlphabet()) {
                if (automaton.getInternalSuccessor(start, internalSymbol) == null) {
                    for (final Location target : locations) {
                        automaton.setInternalSuccessor(start, internalSymbol, target);
                    }
                }
            }

            for (final JSONSymbol callSymbol : alphabet.getCallAlphabet()) {
                for (final Location beforeCall : locations) {
                    final int stackSymbol = automaton.encodeStackSym(beforeCall, callSymbol);
                    for (final JSONSymbol returnSymbol : alphabet.getReturnAlphabet()) {
                        if (automaton.getReturnSuccessor(start, returnSymbol, stackSymbol) == null) {
                            for (final Location target : locations) {
                                automaton.setReturnSuccessor(start, returnSymbol, stackSymbol, target);
                            }
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

        internalSymbols.add(JSONSymbol.toSymbol("\"" + AbstractConstants.stringConstant + "\":"));
        // @formatter:off
        IntStream.range(1, numberOfElements + 1)
            .mapToObj(i -> constructKeySymbol(i))
            .forEach(key -> internalSymbols.add(key));
        // @formatter:on

        return new DefaultVPDAlphabet<>(internalSymbols, callSymbols, returnSymbols);
    }

    private static JSONSymbol constructKeySymbol(int keyNumber) {
        return JSONSymbol.toSymbol("\"" + keyNumber + "\":");
    }
}
