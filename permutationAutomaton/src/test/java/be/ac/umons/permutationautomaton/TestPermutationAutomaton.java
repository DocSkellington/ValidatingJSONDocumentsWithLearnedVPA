package be.ac.umons.permutationautomaton;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.learningjson.JSONSymbol;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class TestPermutationAutomaton {
    
    @Test
    public void testStraightforwardAutomatonWithPermutation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructStraightforwardAutomaton();
        PermutationAutomaton permutationAutomaton = new PermutationAutomaton(automaton);

        Assert.assertFalse(permutationAutomaton.accepts(Word.epsilon()));

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));
    }
    
    @Test
    public void testSmallTwoBranchesAutomatonWithPermutation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
        PermutationAutomaton permutationAutomaton = new PermutationAutomaton(automaton);

        Assert.assertFalse(permutationAutomaton.accepts(Word.epsilon()));

        // First branch

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        // Second branch

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        // Forbidden to mix the two branches
        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));
    }

    @Test
    public void testAutomatonWithOptionalKeysWithPermutation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeys();
        PermutationAutomaton permutationAutomaton = new PermutationAutomaton(automaton);

        Assert.assertFalse(permutationAutomaton.accepts(Word.epsilon()));

        // Nothing nested

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        // One level of nesting

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        // Two levels of nesting

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        // Three levels of nesting

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));
    }

    @Test
    public void testAutomatonWithNestedObjectAndMultipleBranchesPermutation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithNestedObjectAndMultipleBranches();
        PermutationAutomaton permutationAutomaton = new PermutationAutomaton(automaton);

        Assert.assertFalse(permutationAutomaton.accepts(Word.epsilon()));

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k3"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k3"));
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));
    }

    @Test
    public void testAutomatonWithArraysPermutation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithArrays();
        PermutationAutomaton permutationAutomaton = new PermutationAutomaton(automaton);

        Assert.assertFalse(permutationAutomaton.accepts(Word.epsilon()));

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(permutationAutomaton.accepts(builder.toWord()));

        // Wrong order in the arrays

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        // Syntactically incorrect documents

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("int"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("bool"));
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.toSymbol("str"));
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(permutationAutomaton.accepts(builder.toWord()));
    }
}
