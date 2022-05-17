package be.ac.umons.learningjson;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultVPDAlphabet;

public class Automata {
    private static List<JSONSymbol> fromStringsToSymbols(String... symbols) {
        List<JSONSymbol> jsonSymbols = new ArrayList<>(symbols.length);
        for (String symbol : symbols) {
            jsonSymbols.add(JSONSymbol.toSymbol(symbol));
        }
        return jsonSymbols;
    }

    public static VPDAlphabet<JSONSymbol> constructAlphabet(String... symbols) {
        Alphabet<JSONSymbol> internalAlphabet = Alphabets.fromCollection(fromStringsToSymbols(symbols));
        Alphabet<JSONSymbol> callAlphabet = Alphabets.fromCollection(fromStringsToSymbols("{", "["));
        Alphabet<JSONSymbol> returnAlphabet = Alphabets.fromCollection(fromStringsToSymbols("}", "]"));
        return new DefaultVPDAlphabet<>(internalAlphabet, callAlphabet, returnAlphabet);
    }

    public static DefaultOneSEVPA<JSONSymbol> constructStraightforwardAutomaton() {
        VPDAlphabet<JSONSymbol> alphabet = constructAlphabet("k1", "k2", "k3", ",", "int", "str", "bool");
        DefaultOneSEVPA<JSONSymbol> automaton = new DefaultOneSEVPA<>(alphabet);

        Location q0 = automaton.addInitialLocation(false);
        Location q1 = automaton.addLocation(false);
        Location q2 = automaton.addLocation(false);
        Location q3 = automaton.addLocation(false);
        Location q4 = automaton.addLocation(false);
        Location q5 = automaton.addLocation(false);
        Location q6 = automaton.addLocation(true);

        automaton.setInternalSuccessor(q0, JSONSymbol.toSymbol("k1"), q1);

        automaton.setInternalSuccessor(q1, JSONSymbol.toSymbol("int"), q2);

        automaton.setInternalSuccessor(q2, JSONSymbol.commaSymbol, q3);

        automaton.setInternalSuccessor(q3, JSONSymbol.toSymbol("k2"), q4);

        automaton.setInternalSuccessor(q4, JSONSymbol.toSymbol("bool"), q5);

        automaton.setReturnSuccessor(q5, JSONSymbol.closingCurlyBraceSymbol,
                automaton.encodeStackSym(q0, JSONSymbol.openingCurlyBraceSymbol), q6);

        return automaton;
    }

    public static DefaultOneSEVPA<JSONSymbol> constructSmallTwoBranchesAutomaton() {
        VPDAlphabet<JSONSymbol> alphabet = constructAlphabet("k1", "k2", "k3", ",", "int", "str", "bool");
        DefaultOneSEVPA<JSONSymbol> automaton = new DefaultOneSEVPA<>(alphabet);

        Location q0 = automaton.addInitialLocation(false);
        Location q1 = automaton.addLocation(false);
        Location q2 = automaton.addLocation(false);
        Location q3 = automaton.addLocation(false);
        Location q4 = automaton.addLocation(false);
        Location q5 = automaton.addLocation(false);
        Location q6 = automaton.addLocation(true);
        Location q7 = automaton.addLocation(false);
        Location q8 = automaton.addLocation(false);
        Location q9 = automaton.addLocation(false);

        automaton.setInternalSuccessor(q0, JSONSymbol.toSymbol("k1"), q1);

        automaton.setInternalSuccessor(q1, JSONSymbol.toSymbol("int"), q2);
        automaton.setInternalSuccessor(q1, JSONSymbol.toSymbol("str"), q7);

        automaton.setInternalSuccessor(q2, JSONSymbol.commaSymbol, q3);

        automaton.setInternalSuccessor(q3, JSONSymbol.toSymbol("k2"), q4);

        automaton.setInternalSuccessor(q4, JSONSymbol.toSymbol("bool"), q5);

        automaton.setReturnSuccessor(q5, JSONSymbol.closingCurlyBraceSymbol,
                automaton.encodeStackSym(q0, JSONSymbol.openingCurlyBraceSymbol), q6);

        automaton.setInternalSuccessor(q7, JSONSymbol.commaSymbol, q8);

        automaton.setInternalSuccessor(q8, JSONSymbol.toSymbol("k2"), q9);

        automaton.setInternalSuccessor(q9, JSONSymbol.toSymbol("str"), q5);

        return automaton;
    }

    public static DefaultOneSEVPA<JSONSymbol> constructAutomatonWithOptionalKeys() {
        VPDAlphabet<JSONSymbol> alphabet = constructAlphabet("k1", "k2", "o1", "o2", ",", "int", "str", "bool");
        DefaultOneSEVPA<JSONSymbol> automaton = new DefaultOneSEVPA<>(alphabet);

        Location q0 = automaton.addInitialLocation(false);
        Location q1 = automaton.addLocation(false);
        Location q2 = automaton.addLocation(false);
        Location q3 = automaton.addLocation(false);
        Location q4 = automaton.addLocation(false);
        Location q5 = automaton.addLocation(false);
        Location q6 = automaton.addLocation(false);
        Location q7 = automaton.addLocation(false);
        Location q8 = automaton.addLocation(false);
        Location q9 = automaton.addLocation(false);
        Location q10 = automaton.addLocation(false);
        Location q11 = automaton.addLocation(true);

        int q0StackSymbol = automaton.encodeStackSym(q0, JSONSymbol.openingCurlyBraceSymbol);
        automaton.setInternalSuccessor(q0, JSONSymbol.toSymbol("k1"), q1);
        automaton.setInternalSuccessor(q0, JSONSymbol.toSymbol("k2"), q5);

        automaton.setInternalSuccessor(q1, JSONSymbol.toSymbol("str"), q2);

        automaton.setReturnSuccessor(q2, JSONSymbol.closingCurlyBraceSymbol, q0StackSymbol, q11);
        automaton.setInternalSuccessor(q2, JSONSymbol.commaSymbol, q3);

        automaton.setInternalSuccessor(q3, JSONSymbol.toSymbol("o1"), q4);

        int q4StackSymbol = automaton.encodeStackSym(q4, JSONSymbol.openingCurlyBraceSymbol);

        automaton.setInternalSuccessor(q5, JSONSymbol.toSymbol("int"), q6);
        
        automaton.setReturnSuccessor(q6, JSONSymbol.closingCurlyBraceSymbol, q4StackSymbol, q7);
        automaton.setInternalSuccessor(q6, JSONSymbol.commaSymbol, q3);

        automaton.setReturnSuccessor(q7, JSONSymbol.closingCurlyBraceSymbol, q0StackSymbol, q11);
        automaton.setReturnSuccessor(q7, JSONSymbol.closingCurlyBraceSymbol, q4StackSymbol, q7);
        automaton.setInternalSuccessor(q7, JSONSymbol.commaSymbol, q8);

        automaton.setInternalSuccessor(q8, JSONSymbol.toSymbol("o2"), q9);

        automaton.setInternalSuccessor(q9, JSONSymbol.toSymbol("bool"), q10);

        automaton.setReturnSuccessor(q10, JSONSymbol.closingCurlyBraceSymbol, q0StackSymbol, q11);
        automaton.setReturnSuccessor(q10, JSONSymbol.closingCurlyBraceSymbol, q4StackSymbol, q7);

        return automaton;
    }

    public static DefaultOneSEVPA<JSONSymbol> constructAutomatonWithTwoKeysOnSameTransition() {
        VPDAlphabet<JSONSymbol> alphabet = constructAlphabet("k1", "k2", "o1", ",", "int", "str");
        DefaultOneSEVPA<JSONSymbol> automaton = new DefaultOneSEVPA<>(alphabet);

        Location q0 = automaton.addInitialLocation(false);
        Location q1 = automaton.addLocation(false);
        Location q2 = automaton.addLocation(false);
        Location q3 = automaton.addLocation(false);
        Location q4 = automaton.addLocation(false);
        Location q5 = automaton.addLocation(false);
        Location q6 = automaton.addLocation(true);

        int q0StackSymbol = automaton.encodeStackSym(q0, JSONSymbol.openingCurlyBraceSymbol);
        int q4StackSymbol = automaton.encodeStackSym(q4, JSONSymbol.openingCurlyBraceSymbol);

        automaton.setInternalSuccessor(q0, JSONSymbol.toSymbol("k1"), q1);
        automaton.setInternalSuccessor(q0, JSONSymbol.toSymbol("k2"), q1);

        automaton.setInternalSuccessor(q1, JSONSymbol.toSymbol("int"), q2);
        automaton.setInternalSuccessor(q1, JSONSymbol.toSymbol("str"), q2);

        automaton.setInternalSuccessor(q2, JSONSymbol.commaSymbol, q3);
        automaton.setReturnSuccessor(q2, JSONSymbol.closingCurlyBraceSymbol, q4StackSymbol, q5);

        automaton.setInternalSuccessor(q3, JSONSymbol.toSymbol("o1"), q4);

        automaton.setReturnSuccessor(q5, JSONSymbol.closingCurlyBraceSymbol, q0StackSymbol, q6);
        automaton.setReturnSuccessor(q5, JSONSymbol.closingCurlyBraceSymbol, q4StackSymbol, q5);

        return automaton;
    }

    public static DefaultOneSEVPA<JSONSymbol> constructAutomatonWithNestedObjectAndMultipleBranches() {
        VPDAlphabet<JSONSymbol> alphabet = constructAlphabet("k1", "k2", "k3", ",", "int", "str", "bool");
        DefaultOneSEVPA<JSONSymbol> automaton = new DefaultOneSEVPA<>(alphabet);
        JSONSymbol k1Symbol = JSONSymbol.toSymbol("k1"), k2Symbol = JSONSymbol.toSymbol("k2"), k3Symbol = JSONSymbol.toSymbol("k3");
        JSONSymbol intSymbol = JSONSymbol.toSymbol("int"), boolSymbol = JSONSymbol.toSymbol("bool"), strSymbol = JSONSymbol.toSymbol("str");

        List<Location> locations = new ArrayList<>();

        locations.add(automaton.addInitialLocation(false));
        for (int i = 1 ; i <= 17 ; i++) {
            locations.add(automaton.addLocation(false));
        }
        locations.add(automaton.addLocation(true));

        int q0StackSymbol = automaton.encodeStackSym(locations.get(0), JSONSymbol.openingCurlyBraceSymbol);
        int q4StackSymbol = automaton.encodeStackSym(locations.get(4), JSONSymbol.openingCurlyBraceSymbol);
        int q7StackSymbol = automaton.encodeStackSym(locations.get(7), JSONSymbol.openingCurlyBraceSymbol);
        int q10StackSymbol = automaton.encodeStackSym(locations.get(10), JSONSymbol.openingCurlyBraceSymbol);

        automaton.setInternalSuccessor(locations.get(0), k1Symbol, locations.get(1));
        automaton.setInternalSuccessor(locations.get(0), k2Symbol, locations.get(11));

        automaton.setInternalSuccessor(locations.get(1), intSymbol, locations.get(2));
        automaton.setInternalSuccessor(locations.get(1), strSymbol, locations.get(5));
        automaton.setInternalSuccessor(locations.get(1), boolSymbol, locations.get(8));

        automaton.setInternalSuccessor(locations.get(2), JSONSymbol.commaSymbol, locations.get(3));

        automaton.setInternalSuccessor(locations.get(3), k2Symbol, locations.get(4));

        automaton.setInternalSuccessor(locations.get(5), JSONSymbol.commaSymbol, locations.get(6));

        automaton.setInternalSuccessor(locations.get(6), k2Symbol, locations.get(7));

        automaton.setInternalSuccessor(locations.get(8), JSONSymbol.commaSymbol, locations.get(9));

        automaton.setInternalSuccessor(locations.get(9), k2Symbol, locations.get(10));

        automaton.setInternalSuccessor(locations.get(11), strSymbol, locations.get(12));
        automaton.setInternalSuccessor(locations.get(11), intSymbol, locations.get(14));

        automaton.setReturnSuccessor(locations.get(12), JSONSymbol.closingCurlyBraceSymbol, q4StackSymbol, locations.get(13));

        automaton.setReturnSuccessor(locations.get(13), JSONSymbol.closingCurlyBraceSymbol, q0StackSymbol, locations.get(18));

        automaton.setReturnSuccessor(locations.get(14), JSONSymbol.closingCurlyBraceSymbol, q7StackSymbol, locations.get(15));
        automaton.setReturnSuccessor(locations.get(14), JSONSymbol.closingCurlyBraceSymbol, q10StackSymbol, locations.get(13));

        automaton.setInternalSuccessor(locations.get(15), JSONSymbol.commaSymbol, locations.get(16));

        automaton.setInternalSuccessor(locations.get(16), k3Symbol, locations.get(17));

        automaton.setInternalSuccessor(locations.get(17), intSymbol, locations.get(13));

        return automaton;
    }

    public static DefaultOneSEVPA<JSONSymbol> constructAutomatonWithArrays() {
        VPDAlphabet<JSONSymbol> alphabet = constructAlphabet("k1", "k2", ",", "int", "str", "bool");
        DefaultOneSEVPA<JSONSymbol> automaton = new DefaultOneSEVPA<>(alphabet);
        JSONSymbol k1Symbol = JSONSymbol.toSymbol("k1"), k2Symbol = JSONSymbol.toSymbol("k2");
        JSONSymbol intSymbol = JSONSymbol.toSymbol("int"), boolSymbol = JSONSymbol.toSymbol("bool"), strSymbol = JSONSymbol.toSymbol("str");

        List<Location> locations = new ArrayList<>();

        locations.add(automaton.addInitialLocation(false));
        for (int i = 1 ; i <= 15 ; i++) {
            locations.add(automaton.addLocation(false));
        }
        locations.add(automaton.addLocation(true));

        int q0StackSymbol = automaton.encodeStackSym(locations.get(0), JSONSymbol.openingCurlyBraceSymbol);
        int q1StackSymbol = automaton.encodeStackSym(locations.get(1), JSONSymbol.openingBracketSymbol);
        int q3StackSymbol = automaton.encodeStackSym(locations.get(3), JSONSymbol.openingCurlyBraceSymbol);
        int q7StackSymbol = automaton.encodeStackSym(locations.get(7), JSONSymbol.openingBracketSymbol);

        automaton.setInternalSuccessor(locations.get(0), k1Symbol, locations.get(1));
        automaton.setInternalSuccessor(locations.get(0), intSymbol, locations.get(2));
        automaton.setInternalSuccessor(locations.get(0), k2Symbol, locations.get(4));
        automaton.setInternalSuccessor(locations.get(0), boolSymbol, locations.get(10));

        automaton.setInternalSuccessor(locations.get(2), JSONSymbol.commaSymbol, locations.get(3));

        automaton.setInternalSuccessor(locations.get(4), strSymbol, locations.get(5));

        automaton.setReturnSuccessor(locations.get(5), JSONSymbol.closingCurlyBraceSymbol, q3StackSymbol, locations.get(6));

        automaton.setInternalSuccessor(locations.get(6), JSONSymbol.commaSymbol, locations.get(7));

        automaton.setReturnSuccessor(locations.get(10), JSONSymbol.closingBracketSymbol, q7StackSymbol, locations.get(11));

        automaton.setInternalSuccessor(locations.get(11), JSONSymbol.commaSymbol, locations.get(12));

        automaton.setInternalSuccessor(locations.get(12), boolSymbol, locations.get(8));

        automaton.setReturnSuccessor(locations.get(8), JSONSymbol.closingBracketSymbol, q1StackSymbol, locations.get(9));

        automaton.setInternalSuccessor(locations.get(9), JSONSymbol.commaSymbol, locations.get(13));

        automaton.setInternalSuccessor(locations.get(13), k2Symbol, locations.get(14));

        automaton.setInternalSuccessor(locations.get(14), strSymbol, locations.get(15));

        automaton.setReturnSuccessor(locations.get(15), JSONSymbol.closingCurlyBraceSymbol, q0StackSymbol, locations.get(16));

        return automaton;
    }
}
