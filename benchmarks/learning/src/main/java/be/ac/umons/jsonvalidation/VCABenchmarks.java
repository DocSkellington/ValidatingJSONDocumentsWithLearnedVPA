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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import be.ac.umons.jsonlearning.JSONMembershipOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import de.learnlib.algorithms.lstar.vca.LStarVCA;
import de.learnlib.algorithms.lstar.vca.StratifiedObservationTableWithCounterValues;
import de.learnlib.algorithms.lstar.vca.VCAExperiment;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.cache.roca.ROCAHashCacheOracle;
import de.learnlib.filter.statistic.oracle.ROCACounterOracle;
import de.learnlib.filter.statistic.oracle.vca.VCACounterEQOracle;
import de.learnlib.oracle.equivalence.roca.RestrictedAutomatonCounterEQOracle;
import net.automatalib.automata.oca.VCA;
import net.automatalib.words.VPDAlphabet;

public abstract class VCABenchmarks extends ABenchmarks {

    public VCABenchmarks(Path pathToCSVFile, Path pathToDotFiles, Duration timeout, int maxProperties, int maxItems)
            throws IOException {
        super(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems);
    }

    @Override
    protected List<String> getHeader() {
        // @formatter:off
        return List.of(
            "Total time (ms)",
            "Membership queries",
            "Partial equivalence queries",
            "Equivalence queries",
            "Rounds",
            "|R|",
            "|S|",
            "Result alphabet size",
            "Result VCA size"
        );
        // @formatter:on
    }

    @Override
    protected void runExperiment(final Random rand, final JSONSchema schema, final String schemaName, final int nTests,
            final boolean canGenerateInvalid, final int maxDocumentDepth, final boolean shuffleKeys,
            final int currentId) throws InterruptedException, IOException, JSONSchemaException {
        final VPDAlphabet<JSONSymbol> alphabet = extractSymbolsFromSchema(schema);

        final MembershipOracle.ROCAMembershipOracle<JSONSymbol> sul = new JSONMembershipOracle(schema);
        final ROCAHashCacheOracle<JSONSymbol> sulCache = new ROCAHashCacheOracle<>(sul);
        final ROCACounterOracle<JSONSymbol> membershipOracle = new ROCACounterOracle<>(sulCache, "membership queries");

        final EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> partialEqOracle = getRestrictedAutomatonEquivalenceOracle(
                nTests, canGenerateInvalid, getMaxProperties(), getMaxItems(), schema, rand, shuffleKeys, alphabet);
        final RestrictedAutomatonCounterEQOracle<JSONSymbol> partialEquivalenceOracle = new RestrictedAutomatonCounterEQOracle<>(
                partialEqOracle, "partial equivalence queries");

        final EquivalenceOracle.VCAEquivalenceOracle<JSONSymbol> eqOracle = getEquivalenceOracle(nTests,
                canGenerateInvalid, maxDocumentDepth, getMaxProperties(), getMaxItems(), schema, rand, shuffleKeys,
                alphabet);
        final VCACounterEQOracle<JSONSymbol> equivalenceOracle = new VCACounterEQOracle<>(eqOracle,
                "equivalence queries");

        final LStarVCA<JSONSymbol> lstar_vca = new LStarVCA<>(membershipOracle, partialEquivalenceOracle, alphabet);
        final VCAExperiment<JSONSymbol> experiment = new VCAExperiment<>(lstar_vca, equivalenceOracle, alphabet);
        experiment.setLogModels(false);
        experiment.setProfile(true);

        ExperimentResults results = runExperiment(experiment);

        final List<Object> statistics = new LinkedList<>();
        if (results.finished) {
            final VCA<?, JSONSymbol> learntVCA = experiment.getFinalHypothesis();
            StratifiedObservationTableWithCounterValues<JSONSymbol, Boolean> table = lstar_vca.getObservationTable();

            statistics.add(results.timeInMillis);
            statistics.add(membershipOracle.getStatisticalData().getCount());
            statistics.add(partialEquivalenceOracle.getStatisticalData().getCount());
            statistics.add(equivalenceOracle.getStatisticalData().getCount());
            statistics.add(experiment.getRounds().getCount());
            statistics.add(table.numberOfShortPrefixRows());
            statistics.add(table.numberOfSuffixes());
            statistics.add(alphabet.size());
            statistics.add(learntVCA.size());

            writeModelToDot(learntVCA, schemaName, currentId);
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
            int nTests, boolean canGenerateInvalid, int maxProperties, int maxItems, JSONSchema schema, Random rand,
            boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet);

    protected abstract EquivalenceOracle.VCAEquivalenceOracle<JSONSymbol> getEquivalenceOracle(int nTests,
            boolean canGenerateInvalid, int maxDocumentDepth, int maxProperties, int maxItems, JSONSchema schema,
            Random rand, boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet);
}
