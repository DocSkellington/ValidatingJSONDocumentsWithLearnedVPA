package be.ac.umons.jsonlearning;

import java.util.Random;

import javax.annotation.Nullable;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.graph.KeyGraph;
import be.ac.umons.jsonvalidation.graph.OnAcceptingPathRelation;
import be.ac.umons.jsonvalidation.graph.ReachabilityRelation;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.util.automata.vpda.OneSEVPAUtil;
import net.automatalib.words.Word;

/**
 * The common interface for equivalence oracles for VPDAs.
 * 
 * @author Gaëtan Staquet
 */
public interface IVPDAJSONEquivalenceOracle extends EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> {

    /**
     * If there is a loop over the initial location reading an internal symbol, we
     * can construct a counterexample by concatenating the symbol with a word that
     * is accepted by the hypothesis.
     * 
     * <p>
     * Since the resulting word starts by a symbol that is not {, it must actually
     * be rejected.
     * </p>
     * 
     * @param <L>    Location type
     * @param hypo   The hypothesis
     * @param random The random generator
     * @return A counterexample, or null
     */
    @Nullable
    default <L> DefaultQuery<JSONSymbol, Boolean> counterexampleByLoopingOverInitial(final OneSEVPA<L, JSONSymbol> hypo,
            final Random random) {
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

    /**
     * Constructs the key graph for the hypothesis and, if it is invalid, provides a
     * counterexample.
     * 
     * @see KeyGraph#getWitnessInvalid()
     * @param <L>  Location type
     * @param hypo The hypothesis
     * @return A counterexample, or null
     */
    @Nullable
    default <L> DefaultQuery<JSONSymbol, Boolean> counterexampleFromKeyGraph(OneSEVPA<L, JSONSymbol> hypo) {
        final KeyGraph<L> keyGraph = KeyGraph.graphFor(hypo, true);
        if (keyGraph == null || keyGraph.isValid()) {
            return null;
        }
        assert keyGraph.getWitnessInvalid() != null;
        assert hypo.accepts(keyGraph.getWitnessInvalid());
        return new DefaultQuery<>(keyGraph.getWitnessInvalid(), false);
    }

    /**
     * Constructs the key graph for the hypothesis and, if it is invalid, provides a
     * counterexample. In all cases, also returns the computed relations.
     * 
     * @see KeyGraph#getWitnessInvalid()
     * @param <L>  Location type
     * @param hypo The hypothesis
     * @return The relations, alongside a counterexample. The counterexample may be
     *         null.
     */
    default <L> CounterexampleWithRelations<L> counterexampleAndRelationFromKeyGraph(OneSEVPA<L, JSONSymbol> hypo) {
        final ReachabilityRelation<L> reachabilityRelation = ReachabilityRelation.computeReachabilityRelation(hypo,
                true);
        final OnAcceptingPathRelation<L> onAcceptingPathRelation = OnAcceptingPathRelation.computeRelation(hypo,
                reachabilityRelation, true);

        final KeyGraph<L> keyGraph = new KeyGraph<>(hypo, reachabilityRelation, onAcceptingPathRelation, true);
        if (keyGraph == null || keyGraph.isValid()) {
            return null;
        }
        assert keyGraph.getWitnessInvalid() != null;
        assert hypo.accepts(keyGraph.getWitnessInvalid());
        return new CounterexampleWithRelations<>(new DefaultQuery<>(keyGraph.getWitnessInvalid(), false),
                reachabilityRelation, onAcceptingPathRelation);
    }

    /**
     * Constructs the key graph for the hypothesis by reusing information computed
     * at the previous equivalence query and, if it is invalid, provides a
     * counterexample. In all cases, also returns the computed relations.
     * 
     * @param <L1>                            Previous hypothesis location type
     * @param <L2>                            Current hypothesis location type
     * @param previousHypothesis              The previous hypothesis
     * @param previousReachabilityRelation    The reachability relation for the
     *                                        previous hypothesis
     * @param previousOnAcceptingPathRelation The Z_A relation for the previous
     *                                        hypothesis
     * @param currentHypothesis               The current hypothesis
     * @return The new relations, alongside a counterexample. The counterexample may
     *         be null.
     */
    default <L1, L2> CounterexampleWithRelations<L2> counterexampleAndRelationFromKeyGraph(
            OneSEVPA<L1, JSONSymbol> previousHypothesis, ReachabilityRelation<L1> previousReachabilityRelation,
            OnAcceptingPathRelation<L1> previousOnAcceptingPathRelation, OneSEVPA<L2, JSONSymbol> currentHypothesis) {
        final ReachabilityRelation<L2> reachabilityRelation = ReachabilityRelation
                .computeReachabilityRelation(previousHypothesis, previousReachabilityRelation, currentHypothesis, true);
        final OnAcceptingPathRelation<L2> onAcceptingPathRelation = OnAcceptingPathRelation.computeRelation(
                previousHypothesis, previousOnAcceptingPathRelation, currentHypothesis, reachabilityRelation, true);

        final KeyGraph<L2> keyGraph = new KeyGraph<>(currentHypothesis, reachabilityRelation, onAcceptingPathRelation,
                true);
        if (keyGraph == null || keyGraph.isValid()) {
            return null;
        }
        assert keyGraph.getWitnessInvalid() != null;
        assert currentHypothesis.accepts(keyGraph.getWitnessInvalid());
        return new CounterexampleWithRelations<>(new DefaultQuery<>(keyGraph.getWitnessInvalid(), false),
                reachabilityRelation, onAcceptingPathRelation);
    }

    /**
     * The {@link ReachabilityRelation} and {@link OnAcceptingPathRelation} computed
     * to construct a key graph, and a counterexample.
     * 
     * If the key graph is valid, the counterexample is null.
     * 
     * @author Gaëtan Staquet
     */
    class CounterexampleWithRelations<L> {
        public final DefaultQuery<JSONSymbol, Boolean> counterexample;
        public final ReachabilityRelation<L> reachabilityRelation;
        public final OnAcceptingPathRelation<L> onAcceptingPathRelation;

        public CounterexampleWithRelations(DefaultQuery<JSONSymbol, Boolean> counterexample,
                ReachabilityRelation<L> reachabilityRelation, OnAcceptingPathRelation<L> onAcceptingPathRelation) {
            this.counterexample = counterexample;
            this.reachabilityRelation = reachabilityRelation;
            this.onAcceptingPathRelation = onAcceptingPathRelation;
        }
    }

}
