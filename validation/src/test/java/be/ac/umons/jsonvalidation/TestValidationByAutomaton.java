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

import org.testng.Assert;
import org.testng.annotations.Test;

import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * @author Gaëtan Staquet
 */
public class TestValidationByAutomaton {

    @Test
    public void testStraightforwardAutomatonWithPermutation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructStraightforwardAutomaton();
        ValidationByAutomaton<Location> validationByAutomaton = new ValidationByAutomaton<>(automaton);

        Assert.assertFalse(validationByAutomaton.accepts(Word.epsilon()));

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));
    }

    @Test
    public void testSmallTwoBranchesAutomatonWithPermutation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructSmallTwoBranchesAutomaton();
        ValidationByAutomaton<Location> validationByAutomaton = new ValidationByAutomaton<>(automaton);

        Assert.assertFalse(validationByAutomaton.accepts(Word.epsilon()));

        // First branch

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        // Second branch

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        // Forbidden to mix the two branches
        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));
    }

    @Test
    public void testAutomatonWithOptionalKeysWithPermutation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithOptionalKeys();
        ValidationByAutomaton<Location> validationByAutomaton = new ValidationByAutomaton<>(automaton);

        Assert.assertFalse(validationByAutomaton.accepts(Word.epsilon()));

        // Nothing nested

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        // One level of nesting

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        // Two levels of nesting

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        // Three levels of nesting

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o1"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("o2"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));
    }

    @Test
    public void testAutomatonWithNestedObjectAndMultipleBranchesPermutation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithNestedObjectAndMultipleBranches();
        ValidationByAutomaton<Location> validationByAutomaton = new ValidationByAutomaton<>(automaton);

        Assert.assertFalse(validationByAutomaton.accepts(Word.epsilon()));

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k3"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k3"));
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));
    }

    @Test
    public void testAutomatonWithArraysPermutation() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithArrays();
        ValidationByAutomaton<Location> validationByAutomaton = new ValidationByAutomaton<>(automaton);

        Assert.assertFalse(validationByAutomaton.accepts(Word.epsilon()));

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        // Wrong order in the arrays

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        // Syntactically incorrect documents

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.integerSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.openingBracketSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.trueSymbol);
        builder.add(JSONSymbol.closingBracketSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k2"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));
    }

    @Test
    public void testEmptyObject() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonAcceptingEmptyObject();
        ValidationByAutomaton<Location> validationByAutomaton = new ValidationByAutomaton<>(automaton);

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));
    }

    @Test
    public void testAutomatonWithCycle() {
        DefaultOneSEVPA<JSONSymbol> automaton = Automata.constructAutomatonWithCycleReadingAKey();
        ValidationByAutomaton<Location> validationByAutomaton = new ValidationByAutomaton<>(automaton);

        WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertTrue(validationByAutomaton.accepts(builder.toWord()));

        builder.clear();
        builder.add(JSONSymbol.openingCurlyBraceSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.commaSymbol);
        builder.add(JSONSymbol.toSymbol("k1"));
        builder.add(JSONSymbol.stringSymbol);
        builder.add(JSONSymbol.closingCurlyBraceSymbol);
        Assert.assertFalse(validationByAutomaton.accepts(builder.toWord()));
    }
}
