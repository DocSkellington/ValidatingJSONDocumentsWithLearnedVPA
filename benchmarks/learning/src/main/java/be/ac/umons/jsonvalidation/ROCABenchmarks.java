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
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import be.ac.umons.jsonlearning.JSONCounterValueOracle;
import be.ac.umons.jsonlearning.JSONMembershipOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import de.learnlib.algorithms.lstar.roca.LStarROCA;
import de.learnlib.algorithms.lstar.roca.ObservationTableWithCounterValuesROCA;
import de.learnlib.algorithms.lstar.roca.ROCAExperiment;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.cache.roca.CounterValueHashCacheOracle;
import de.learnlib.filter.cache.roca.ROCAHashCacheOracle;
import de.learnlib.filter.statistic.oracle.CounterValueCounterOracle;
import de.learnlib.filter.statistic.oracle.ROCACounterOracle;
import de.learnlib.filter.statistic.oracle.roca.ROCACounterEQOracle;
import de.learnlib.oracle.equivalence.roca.RestrictedAutomatonCounterEQOracle;
import net.automatalib.automata.oca.ROCA;
import net.automatalib.words.Alphabet;

public abstract class ROCABenchmarks extends ABenchmarks {

    public ROCABenchmarks(Path pathToCSVFile, Path pathToDotFiles, Duration timeout, int maxProperties, int maxItems)
            throws IOException {
        super(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems);
    }

    @Override
    protected List<String> getHeader() {
        // @formatter:off
        return Arrays.asList(
            "Total time (ms)",
            "ROCA counterexample time (ms)",
            "DFA counterexample time (ms)",
            "Learning DFA time (ms)",
            "Table time (ms)",
            "Finding descriptions (ms)",
            "Membership queries",
            "Counter value queries",
            "Partial equivalence queries",
            "Equivalence queries",
            "Rounds",
            "|R|",
            "|S|",
            "Result alphabet size",
            "Result ROCA size"
        );
        // @formatter:on
    }

    @Override
    protected void runExperiment(final Random rand, final JSONSchema schema, final String schemaName, final int nTests,
            final boolean canGenerateInvalid, final int maxDocumentDepth, final boolean shuffleKeys,
            final int currentId) throws InterruptedException, IOException, JSONSchemaException {
        final Alphabet<JSONSymbol> alphabet = extractSymbolsFromSchema(schema);

        final MembershipOracle.ROCAMembershipOracle<JSONSymbol> sul = new JSONMembershipOracle(schema);
        final ROCAHashCacheOracle<JSONSymbol> sulCache = new ROCAHashCacheOracle<>(sul);
        final ROCACounterOracle<JSONSymbol> membershipOracle = new ROCACounterOracle<>(sulCache, "membership queries");

        final MembershipOracle.CounterValueOracle<JSONSymbol> counterValue = new JSONCounterValueOracle();
        final CounterValueHashCacheOracle<JSONSymbol> counterValueCache = new CounterValueHashCacheOracle<>(
                counterValue);
        final CounterValueCounterOracle<JSONSymbol> counterValueOracle = new CounterValueCounterOracle<>(
                counterValueCache, "counter value queries");

        final EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> partialEqOracle = getRestrictedAutomatonEquivalenceOracle(
                nTests, canGenerateInvalid, getMaxProperties(), getMaxItems(), schema, rand, shuffleKeys, alphabet);
        final RestrictedAutomatonCounterEQOracle<JSONSymbol> partialEquivalenceOracle = new RestrictedAutomatonCounterEQOracle<>(
                partialEqOracle, "partial equivalence queries");

        final EquivalenceOracle.ROCAEquivalenceOracle<JSONSymbol> eqOracle = getEquivalenceOracle(nTests,
                canGenerateInvalid, maxDocumentDepth, getMaxProperties(), getMaxItems(), schema, rand, shuffleKeys,
                alphabet);
        final ROCACounterEQOracle<JSONSymbol> equivalenceOracle = new ROCACounterEQOracle<>(eqOracle,
                "equivalence queries");

        final LStarROCA<JSONSymbol> lstar_roca = new LStarROCA<>(membershipOracle, counterValueOracle,
                partialEquivalenceOracle, alphabet);
        final ROCAExperiment<JSONSymbol> experiment = new ROCAExperiment<>(lstar_roca, equivalenceOracle, alphabet);
        experiment.setLogModels(false);
        experiment.setProfile(true);

        final ExperimentResults results = runExperiment(experiment);

        final List<Object> statistics = new LinkedList<>();
        if (results.finished) {
            final ROCA<?, JSONSymbol> learntROCA = experiment.getFinalHypothesis();
            ObservationTableWithCounterValuesROCA<JSONSymbol> table = lstar_roca.getObservationTable();

            statistics.add(results.timeInMillis);
            statistics.add(getProfilerTime(ROCAExperiment.COUNTEREXAMPLE_PROFILE_KEY));
            statistics.add(getProfilerTime(LStarROCA.COUNTEREXAMPLE_DFA_PROFILE_KEY));
            statistics.add(getProfilerTime(ROCAExperiment.LEARNING_ROCA_PROFILE_KEY));
            statistics.add(getProfilerTime(LStarROCA.CLOSED_TABLE_PROFILE_KEY));
            statistics.add(getProfilerTime(LStarROCA.FINDING_PERIODIC_DESCRIPTIONS));
            statistics.add(membershipOracle.getStatisticalData().getCount());
            statistics.add(counterValueOracle.getStatisticalData().getCount());
            statistics.add(partialEquivalenceOracle.getStatisticalData().getCount());
            statistics.add(equivalenceOracle.getStatisticalData().getCount());
            statistics.add(experiment.getRounds().getCount());
            statistics.add(table.numberOfShortPrefixRows());
            statistics.add(table.numberOfSuffixes());
            statistics.add(alphabet.size());
            statistics.add(learntROCA.size());

            writeModelToDot(learntROCA, schemaName, currentId);
        } else if (results.error) {
            for (int i = statistics.size(); i < nColumns; i++) {
                statistics.add("Error");
            }
        } else {
            for (int i = statistics.size(); i < nColumns; i++) {
                statistics.add("Timeout");
            }
        }

        csvPrinter.printRecord(statistics);
        csvPrinter.flush();
    }

    protected abstract EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> getRestrictedAutomatonEquivalenceOracle(
            int numberTests, boolean canGenerateInvalid, int maxProperties, int maxItems, JSONSchema schema,
            Random random, boolean shuffleKeys, Alphabet<JSONSymbol> alphabet);

    protected abstract EquivalenceOracle.ROCAEquivalenceOracle<JSONSymbol> getEquivalenceOracle(int numberTests,
            boolean canGenerateInvalid, int maxDocumentDepth, int maxProperties, int maxItems, JSONSchema schema,
            Random random, boolean shuffleKeys, Alphabet<JSONSymbol> alphabet);
}
