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

import java.io.IOException;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.util.AbstractExperiment;
import de.learnlib.util.Experiment;
import net.automatalib.words.Alphabet;

public class StoppableExperiment<A> extends AbstractExperiment<A> {

    public static final String LEARNING_PROFILE_KEY = "Learning";
    public static final String COUNTEREXAMPLE_PROFILE_KEY = "Searching for counterexample";

    private static final LearnLogger LOGGER = LearnLogger.getLogger(Experiment.class);
    private final ExperimentImpl<?, ?> impl;
    private final ABenchmarks benchmarks;
    private final String schemaName;
    private final int identifier;

    public <I, D> StoppableExperiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
            EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
            Alphabet<I> inputs, ABenchmarks benchmarks, String schemaName, int identifier) {
        this.benchmarks = benchmarks;
        this.schemaName = schemaName;
        this.identifier = identifier;
        this.impl = new ExperimentImpl<>(learningAlgorithm, equivalenceAlgorithm, inputs);
    }

    @Override
    protected A runInternal() {
        return impl.run();
    }

    private final class ExperimentImpl<I, D> {

        public final LearningAlgorithm<? extends A, I, D> learningAlgorithm;
        public final EquivalenceOracle<? super A, I, D> equivalenceAlgorithm;
        public final Alphabet<I> inputs;

        ExperimentImpl(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                Alphabet<I> inputs) {
            this.learningAlgorithm = learningAlgorithm;
            this.equivalenceAlgorithm = equivalenceAlgorithm;
            this.inputs = inputs;
        }

        public A run() {
            rounds.increment();
            LOGGER.logPhase("Starting round " + rounds.getCount());
            LOGGER.logPhase("Learning");

            profileStart(LEARNING_PROFILE_KEY);
            learningAlgorithm.startLearning();
            profileStop(LEARNING_PROFILE_KEY);

            while (true) {
                final A hyp = learningAlgorithm.getHypothesisModel();

                if (logModels) {
                    LOGGER.logModel(hyp);
                }
                try {
                    benchmarks.writeModelToDot(hyp, schemaName, identifier);
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }

                LOGGER.logPhase("Searching for counterexample");

                profileStart(COUNTEREXAMPLE_PROFILE_KEY);
                DefaultQuery<I, D> ce = equivalenceAlgorithm.findCounterExample(hyp, inputs);
                profileStop(COUNTEREXAMPLE_PROFILE_KEY);

                if (ce == null) {
                    return hyp;
                }

                LOGGER.logCounterexample(ce.getInput().toString());

                // next round ...
                rounds.increment();
                LOGGER.logPhase("Starting round " + rounds.getCount());
                LOGGER.logPhase("Learning");

                profileStart(LEARNING_PROFILE_KEY);
                final boolean refined = learningAlgorithm.refineHypothesis(ce);
                profileStop(LEARNING_PROFILE_KEY);

                assert refined;
            }
        }
    }

    public A getCurrentHypothesis() {
        return impl.learningAlgorithm.getHypothesisModel();
    }

}
