package be.ac.umons.jsonvalidation.oracles.exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.exploration.DefaultExplorationGenerator;
import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.WordConversion;
import be.ac.umons.jsonvalidation.validation.relation.KeyGraph;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class VPDAJSONEquivalenceOracle
        extends AbstractExplorationJSONConformanceVisiblyAlphabet<OneSEVPA<?, JSONSymbol>> {

    public VPDAJSONEquivalenceOracle(int numberTests, boolean canGenerateInvalid, int maxDocumentDepth,
            int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys,
            VPDAlphabet<JSONSymbol> alphabet) {
        super(numberTests, canGenerateInvalid, maxDocumentDepth, maxProperties, maxItems, schema, random, shuffleKeys,
                alphabet);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(OneSEVPA<?, JSONSymbol> hypo,
            Collection<? extends JSONSymbol> inputs) {
        final Word<JSONSymbol> loopingWord = loopOverInitial(hypo);
        if (loopingWord != null) {
            final Iterator<JSONObject> iterator = new DefaultExplorationGenerator(getMaxProperties(), getMaxItems()).createIterator(getSchema());
            assert iterator.hasNext();
            final JSONObject validDocument = iterator.next();
            final Word<JSONSymbol> validWord = WordConversion.fromJSONDocumentToJSONSymbolWord(validDocument, false, getRandom());

            final Word<JSONSymbol> totalWord = loopingWord.concat(validWord);
            if (hypo.accepts(totalWord)) {
                return new DefaultQuery<>(totalWord, false);
            }
        }

        final Word<JSONSymbol> fromCycle = counterexampleFromKeyGraph(hypo);
        if (fromCycle != null) {
            return new DefaultQuery<>(fromCycle, false);
        }

        return super.findCounterExample(hypo, inputs);
    }

    private <L> Word<JSONSymbol> loopOverInitial(OneSEVPA<L, JSONSymbol> hypo) {
        final List<JSONSymbol> loopingSymbols = new ArrayList<>();
        for (JSONSymbol internalSymbol : hypo.getInputAlphabet().getInternalAlphabet()) {
            final L target = hypo.getInternalSuccessor(hypo.getInitialLocation(), internalSymbol);
            if (target == hypo.getInitialLocation()) {
                loopingSymbols.add(internalSymbol);
            }
        }
        if (loopingSymbols.isEmpty()) {
            return null;
        }

        final WordBuilder<JSONSymbol> builder = new WordBuilder<>();
        for (int i = 0 ; i < 1 ; i++) {
            final int index = getRandom().nextInt(loopingSymbols.size());
            builder.add(loopingSymbols.get(index));
        }
        return builder.toWord();
    }

    private <L> Word<JSONSymbol> counterexampleFromKeyGraph(OneSEVPA<L, JSONSymbol> hypo) {
        KeyGraph<L> keyGraph = KeyGraph.graphFor(hypo);
        if (keyGraph.isValid()) {
            return null;
        }
        return keyGraph.getWitnessCycle();
    }
}
