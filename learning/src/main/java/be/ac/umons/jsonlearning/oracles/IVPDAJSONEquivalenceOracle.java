package be.ac.umons.jsonlearning.oracles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.WordConversion;
import be.ac.umons.jsonvalidation.validation.relation.KeyGraph;
import be.ac.umons.jsonvalidation.validation.relation.ReachabilityRelation;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.commons.util.Pair;
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
        final KeyGraph<L> keyGraph = KeyGraph.graphFor(hypo, true, true);
        if (keyGraph == null || keyGraph.isValid()) {
            return null;
        }
        assert keyGraph.getWitnessCycle() != null;
        assert hypo.accepts(keyGraph.getWitnessCycle());
        return new DefaultQuery<>(keyGraph.getWitnessCycle(), false);
    }

    default <L> Pair<DefaultQuery<JSONSymbol, Boolean>, ReachabilityRelation<L>> counterexampleAndRelationFromKeyGraph(OneSEVPA<L, JSONSymbol> hypo) {
        final ReachabilityRelation<L> relation = ReachabilityRelation.computeReachabilityRelation(hypo, true);
        final KeyGraph<L> keyGraph = new KeyGraph<>(hypo, relation, true);
        if (keyGraph == null || keyGraph.isValid()) {
            return null;
        }
        assert keyGraph.getWitnessCycle() != null;
        assert hypo.accepts(keyGraph.getWitnessCycle());
        return Pair.of(new DefaultQuery<>(keyGraph.getWitnessCycle(), false), relation);
    }

    default <L1, L2> Pair<DefaultQuery<JSONSymbol, Boolean>, ReachabilityRelation<L2>> counterexampleAndRelationFromKeyGraph(OneSEVPA<L1, JSONSymbol> previousHypothesis, ReachabilityRelation<L1> previousReachabilityRelation, OneSEVPA<L2, JSONSymbol> currentHypothesis) {
        final ReachabilityRelation<L2> relation = ReachabilityRelation.computeReachabilityRelation(previousHypothesis, previousReachabilityRelation, currentHypothesis, true);
        final KeyGraph<L2> keyGraph = new KeyGraph<>(currentHypothesis, relation, true);
        if (keyGraph == null || keyGraph.isValid()) {
            return null;
        }
        assert keyGraph.getWitnessCycle() != null;
        assert currentHypothesis.accepts(keyGraph.getWitnessCycle());
        return Pair.of(new DefaultQuery<>(keyGraph.getWitnessCycle(), false), relation);
    }

    JSONObject getOneValidDocument();
}
