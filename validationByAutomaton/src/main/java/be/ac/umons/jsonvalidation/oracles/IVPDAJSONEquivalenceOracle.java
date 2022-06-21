package be.ac.umons.jsonvalidation.oracles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.WordConversion;
import be.ac.umons.jsonvalidation.validation.relation.KeyGraph;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public interface IVPDAJSONEquivalenceOracle extends EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> {

    default <L> DefaultQuery<JSONSymbol, Boolean> counterexampleByLoopingOverInitial(OneSEVPA<L, JSONSymbol> hypo, Random random) {
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
            final int index = random.nextInt(loopingSymbols.size());
            builder.add(loopingSymbols.get(index));
        }
        Word<JSONSymbol> loopingWord = builder.toWord();

        if (loopingWord != null) {
            final JSONObject validDocument = getOneValidDocument();
            final Word<JSONSymbol> validWord = WordConversion.fromJSONDocumentToJSONSymbolWord(validDocument, false, random);

            final Word<JSONSymbol> totalWord = loopingWord.concat(validWord);
            if (hypo.accepts(totalWord)) {
                return new DefaultQuery<>(totalWord, false);
            }
        }
        return null;
    }

    default <L> DefaultQuery<JSONSymbol, Boolean> counterexampleFromKeyGraph(OneSEVPA<L, JSONSymbol> hypo) {
        KeyGraph<L> keyGraph = KeyGraph.graphFor(hypo, true);
        if (keyGraph == null || keyGraph.isValid()) {
            return null;
        }
        assert hypo.accepts(keyGraph.getWitnessCycle());
        return new DefaultQuery<>(keyGraph.getWitnessCycle(), false);
    }

    JSONObject getOneValidDocument();
}
