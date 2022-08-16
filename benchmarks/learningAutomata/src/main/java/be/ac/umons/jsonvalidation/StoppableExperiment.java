package be.ac.umons.jsonvalidation;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.util.Experiment;
import net.automatalib.words.Alphabet;

public class StoppableExperiment<A> extends Experiment<A> {

    private final LearningAlgorithm<? extends A, ?, ?> learningAlgorithm;

    public <I, D> StoppableExperiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
            EquivalenceOracle<? super A, I, D> equivalenceAlgorithm, Alphabet<I> inputs) {
        super(learningAlgorithm, equivalenceAlgorithm, inputs);
        this.learningAlgorithm = learningAlgorithm;
    }

    public A getCurrentHypothesis() {
        return learningAlgorithm.getHypothesisModel();
    }
    
}
