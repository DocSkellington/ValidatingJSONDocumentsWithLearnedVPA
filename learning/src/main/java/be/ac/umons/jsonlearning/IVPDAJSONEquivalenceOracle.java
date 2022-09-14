package be.ac.umons.jsonlearning;

import java.util.Random;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.graph.KeyGraph;
import be.ac.umons.jsonvalidation.graph.ReachabilityRelation;
import be.ac.umons.jsonvalidation.graph.OnAcceptingPathRelation;
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
        final Word<JSONSymbol> acceptedWord = OneSEVPAUtil.findAcceptedWord(hypo, hypo.getInputAlphabet());

        if (acceptedWord != null) {
            final Word<JSONSymbol> totalWord = loopingWord.concat(acceptedWord);
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

    default <L> CounterexampleWithRelations<L> counterexampleAndRelationFromKeyGraph(OneSEVPA<L, JSONSymbol> hypo) {
        final ReachabilityRelation<L> reachabilityRelation = ReachabilityRelation.computeReachabilityRelation(hypo, true);
        final OnAcceptingPathRelation<L> witnessRelation = OnAcceptingPathRelation.computeWitnessRelation(hypo, reachabilityRelation, true);

        final KeyGraph<L> keyGraph = new KeyGraph<>(hypo, reachabilityRelation, witnessRelation, true);
        if (keyGraph == null || keyGraph.isValid()) {
            return null;
        }
        assert keyGraph.getWitnessCycle() != null;
        assert hypo.accepts(keyGraph.getWitnessCycle());
        return new CounterexampleWithRelations<>(new DefaultQuery<>(keyGraph.getWitnessCycle(), false), reachabilityRelation, witnessRelation);
    }

    default <L1, L2> CounterexampleWithRelations<L2> counterexampleAndRelationFromKeyGraph(OneSEVPA<L1, JSONSymbol> previousHypothesis, ReachabilityRelation<L1> previousReachabilityRelation, OnAcceptingPathRelation<L1> previousWitnessRelation, OneSEVPA<L2, JSONSymbol> currentHypothesis) {
        final ReachabilityRelation<L2> reachabilityRelation = ReachabilityRelation.computeReachabilityRelation(previousHypothesis, previousReachabilityRelation, currentHypothesis, true);
        final OnAcceptingPathRelation<L2> witnessRelation = OnAcceptingPathRelation.computeWitnessRelation(previousHypothesis, previousWitnessRelation, currentHypothesis, reachabilityRelation, true);

        final KeyGraph<L2> keyGraph = new KeyGraph<>(currentHypothesis, reachabilityRelation, witnessRelation, true);
        if (keyGraph == null || keyGraph.isValid()) {
            return null;
        }
        assert keyGraph.getWitnessCycle() != null;
        assert currentHypothesis.accepts(keyGraph.getWitnessCycle());
        return new CounterexampleWithRelations<>(new DefaultQuery<>(keyGraph.getWitnessCycle(), false), reachabilityRelation, witnessRelation);
    }

    class CounterexampleWithRelations<L> {
        public final DefaultQuery<JSONSymbol, Boolean> counterexample;
        public final ReachabilityRelation<L> reachabilityRelation;
        public final OnAcceptingPathRelation<L> witnessRelation;

        public CounterexampleWithRelations(DefaultQuery<JSONSymbol, Boolean> counterexample, ReachabilityRelation<L> reachabilityRelation, OnAcceptingPathRelation<L> witnessRelation) {
            this.counterexample = counterexample;
            this.reachabilityRelation = reachabilityRelation;
            this.witnessRelation = witnessRelation;
        }
    }

}
