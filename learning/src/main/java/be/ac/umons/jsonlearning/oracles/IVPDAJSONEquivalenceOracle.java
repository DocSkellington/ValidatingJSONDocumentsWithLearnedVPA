package be.ac.umons.jsonlearning.oracles;

import java.util.Random;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.validation.relation.KeyGraph;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.util.automata.vpda.OneSEVPAUtil;
import net.automatalib.words.Word;

public interface IVPDAJSONEquivalenceOracle extends EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> {

    default <L> DefaultQuery<JSONSymbol, Boolean> counterexampleByLoopingOverInitial(OneSEVPA<L, JSONSymbol> hypo, Random random) {
        JSONSymbol loopingSymbol = null;
        for (JSONSymbol internalSymbol : hypo.getInputAlphabet().getInternalAlphabet()) {
            final L target = hypo.getInternalSuccessor(hypo.getInitialLocation(), internalSymbol);
            if (target == hypo.getInitialLocation()) {
                loopingSymbol = internalSymbol;
                break;
            }
        }
        if (loopingSymbol == null) {
            return null;
        }

        final Word<JSONSymbol> loopingWord = Word.fromLetter(loopingSymbol);

        if (loopingWord != null) {
            final Word<JSONSymbol> acceptedWord = OneSEVPAUtil.findAcceptedWord(hypo, hypo.getInputAlphabet());
            final Word<JSONSymbol> totalWord = loopingWord.concat(acceptedWord);
            if (hypo.accepts(totalWord)) {
                return new DefaultQuery<>(totalWord, false);
            }
        }
        return null;
    }

    default <L> DefaultQuery<JSONSymbol, Boolean> counterexampleFromKeyGraph(OneSEVPA<L, JSONSymbol> hypo) {
        KeyGraph<L> keyGraph = KeyGraph.graphFor(hypo, true, true);
        if (keyGraph == null || keyGraph.isValid()) {
            return null;
        }
        assert keyGraph.getWitnessCycle() != null;
        assert hypo.accepts(keyGraph.getWitnessCycle());
        return new DefaultQuery<>(keyGraph.getWitnessCycle(), false);
    }
}
