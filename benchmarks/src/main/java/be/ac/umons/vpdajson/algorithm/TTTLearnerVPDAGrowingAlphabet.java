package be.ac.umons.vpdajson.algorithm;

import be.ac.umons.jsonroca.JSONSymbol;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.HypLoc;
import de.learnlib.algorithms.ttt.vpda.TTTLearnerVPDA;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.vpda.State;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

public class TTTLearnerVPDAGrowingAlphabet extends TTTLearnerVPDA<JSONSymbol> {

    public TTTLearnerVPDAGrowingAlphabet(GrowingVPDAlphabet<JSONSymbol> alphabet,
            MembershipOracle<JSONSymbol, Boolean> oracle, AcexAnalyzer analyzer) {
        super((VPDAlphabet<JSONSymbol>) alphabet, oracle, analyzer);
    }

    @Override
    protected boolean computeHypothesisOutput(Word<JSONSymbol> word) {
        State<HypLoc<JSONSymbol>> curr = hypothesis.getInitialState();
        for (JSONSymbol sym : word) {
            curr = getAnySuccessor(curr, sym);
            if (curr == null) {
                return false;
            }
        }
        return hypothesis.isAccepting(curr);
    }
}
