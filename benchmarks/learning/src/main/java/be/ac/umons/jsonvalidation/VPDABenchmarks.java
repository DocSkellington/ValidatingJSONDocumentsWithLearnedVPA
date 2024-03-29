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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;

import be.ac.umons.jsonlearning.JSONMembershipOracle;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonvalidation.graph.OnAcceptingPathRelation;
import be.ac.umons.jsonvalidation.graph.ReachabilityRelation;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.vpda.TTTLearnerVPDA;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.statistic.oracle.CounterEQOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.automata.vpda.OneSEVPAGraphView.SevpaViewEdge;
import net.automatalib.graphs.Graph;
import net.automatalib.util.automata.vpda.OneSEVPAUtil;
import net.automatalib.words.VPDAlphabet;

public abstract class VPDABenchmarks extends ABenchmarks {
    private static final LearnLogger LOGGER = LearnLogger.getLogger(VPDABenchmarks.class);

    public VPDABenchmarks(Path pathToCSVFile, Path pathToDotFiles, Duration timeout, int maxProperties, int maxItems)
            throws IOException {
        super(pathToCSVFile, pathToDotFiles, timeout, maxProperties, maxItems);
    }

    @Override
    protected List<String> getHeader() {
        // @formatter:off
        return Arrays.asList(
            "Total time (ms)",
            "Membership queries",
            "Equivalence queries",
            "Rounds",
            "Bin state",
            "alphabet size",
            "VPDA size",
            "Internal transitions",
            "Return transitions",
            "Call transitions",
            "Diameter"
        );
        // @formatter:on
    }

    @Override
    protected void runExperiment(final Random rand, final JSONSchema schema, final String schemaName, final int nTests,
            final boolean canGenerateInvalid, final int maxDocumentDepth, final boolean shuffleKeys,
            final int currentId) throws InterruptedException, IOException, JSONSchemaException {
        final VPDAlphabet<JSONSymbol> alphabet = extractSymbolsFromSchema(schema);

        final MembershipOracle<JSONSymbol, Boolean> sul = new JSONMembershipOracle(schema);
        final CounterOracle<JSONSymbol, Boolean> membershipOracle = new CounterOracle<>(sul, "membership queries");

        final EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> eqOracle = getEquivalenceOracle(nTests,
                canGenerateInvalid, maxDocumentDepth, getMaxProperties(), getMaxItems(), schema, rand, shuffleKeys,
                alphabet);
        final CounterEQOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> equivalenceOracle = new CounterEQOracle<>(
                eqOracle, "equivalence queries");

        final TTTLearnerVPDA<JSONSymbol> learner = new TTTLearnerVPDA<>(alphabet, membershipOracle,
                AcexAnalyzers.LINEAR_FWD);
        final StoppableExperiment<OneSEVPA<?, JSONSymbol>> experiment = new StoppableExperiment<>(learner,
                equivalenceOracle, alphabet, this, schemaName, currentId);
        experiment.setLogModels(false);
        experiment.setProfile(true);

        final ExperimentResults results = runExperiment(experiment);

        final List<Object> statistics = new LinkedList<>();
        if (results.error) {
            for (int i = statistics.size(); i < nColumns; i++) {
                statistics.add("Error");
            }
            csvPrinter.printRecord(statistics);
            csvPrinter.flush();
            return;
        }

        final OneSEVPA<?, JSONSymbol> hypothesis;
        if (results.finished) {
            hypothesis = experiment.getFinalHypothesis();
            statistics.add(results.timeInMillis);
        } else { // Timeout
            hypothesis = experiment.getCurrentHypothesis();
            statistics.add("Timeout");
        }
        LOGGER.info("VPDA learned with " + hypothesis.size() + " states");
        final DefaultOneSEVPA<JSONSymbol> vpda = removeBinState(hypothesis);
        LOGGER.info("Bin state removed");
        assert hypothesis.size() - vpda.size() <= 1;
        assert OneSEVPAUtil.testEquivalence(vpda, hypothesis, alphabet);

        statistics.add(membershipOracle.getStatisticalData().getCount());
        statistics.add(equivalenceOracle.getStatisticalData().getCount());
        statistics.add(experiment.getRounds().getCount());
        statistics.add(vpda.size() != hypothesis.size());
        statistics.add(alphabet.size());
        statistics.add(vpda.size());
        statistics.add(numberOfInternalTransitions(vpda));
        statistics.add(numberOfReturnTransitions(vpda));
        statistics.add(numberOfCallTransitions(vpda));
        statistics.add(computeDiameter(vpda));

        writeModelToDot(hypothesis, schemaName, currentId);

        csvPrinter.printRecord(statistics);
        csvPrinter.flush();
    }

    private <L> int computeDiameter(OneSEVPA<L, JSONSymbol> vpda) {
        final Graph<L, SevpaViewEdge<L, JSONSymbol>> graph = vpda.graphView();
        final List<L> nodes = new ArrayList<>(graph.getNodes());
        final List<List<Integer>> distances = floydWarshall(graph, nodes);

        return distances.stream()
                .map(list -> list.stream()
                        .max(Comparator.naturalOrder())
                        .orElseThrow(NoSuchElementException::new))
                .filter(value -> value != Integer.MAX_VALUE)
                .max(Comparator.naturalOrder())
                .orElseThrow(NoSuchElementException::new);
    }

    private <L> List<List<Integer>> floydWarshall(Graph<L, SevpaViewEdge<L, JSONSymbol>> graph, List<L> nodes) {
        final List<List<Integer>> distances = new ArrayList<>(graph.size());

        for (L source : nodes) {
            final List<Integer> dist = new ArrayList<>(nodes.size());
            final Collection<L> adjacent = graph.getAdjacentTargets(source);
            for (L target : nodes) {
                if (source == target) {
                    dist.add(0);
                } else if (adjacent.contains(target)) {
                    dist.add(1);
                } else {
                    dist.add(Integer.MAX_VALUE);
                }
            }
            distances.add(dist);
        }

        for (int k = 0; k < nodes.size(); k++) {
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = 0; j < nodes.size(); j++) {
                    final int sumByK;
                    if (distances.get(i).get(k) == Integer.MAX_VALUE || distances.get(k).get(j) == Integer.MAX_VALUE) {
                        sumByK = Integer.MAX_VALUE;
                    } else {
                        sumByK = distances.get(i).get(k) + distances.get(k).get(j);
                    }
                    if (distances.get(i).get(j) > sumByK) {
                        distances.get(i).set(j, sumByK);
                    }
                }
            }
        }

        return distances;
    }

    private <L> DefaultOneSEVPA<JSONSymbol> removeBinState(OneSEVPA<L, JSONSymbol> learned) {
        final ReachabilityRelation<L> reachabilityRelation = ReachabilityRelation.computeReachabilityRelation(learned,
                false);
        LOGGER.info("Reachability relation computed");
        final OnAcceptingPathRelation<L> onAcceptingPathRelation = OnAcceptingPathRelation.computeRelation(learned,
                reachabilityRelation, false);
        LOGGER.info("Z_A relation computed");

        final L binLocation = onAcceptingPathRelation.identifyBinLocation(learned);
        LOGGER.info("Bin location " + binLocation);

        final VPDAlphabet<JSONSymbol> alphabet = learned.getInputAlphabet();
        final DefaultOneSEVPA<JSONSymbol> withoutBin = new DefaultOneSEVPA<>(alphabet);
        final Map<L, Location> oldToNewLocations = new HashMap<>();
        for (final L location : learned.getLocations()) {
            if (!Objects.equals(location, binLocation)) {
                final Location newLocation;
                final boolean accepting = learned.isAcceptingLocation(location);
                if (learned.getInitialLocation() == location) {
                    newLocation = withoutBin.addInitialLocation(accepting);
                } else {
                    newLocation = withoutBin.addLocation(accepting);
                }
                oldToNewLocations.put(location, newLocation);
            }
        }

        for (final L source : learned.getLocations()) {
            if (Objects.equals(source, binLocation)) {
                continue;
            }

            for (final JSONSymbol internalSymbol : alphabet.getInternalAlphabet()) {
                final L target = learned.getInternalSuccessor(source, internalSymbol);
                assert target != null;
                if (!Objects.equals(target, binLocation)) {
                    withoutBin.setInternalSuccessor(oldToNewLocations.get(source), internalSymbol,
                            oldToNewLocations.get(target));
                }
            }

            for (final JSONSymbol returnSymbol : alphabet.getReturnAlphabet()) {
                for (final JSONSymbol callSymbol : alphabet.getCallAlphabet()) {
                    for (final L locationBeforeCall : learned.getLocations()) {
                        final int stackSymLearned = learned.encodeStackSym(locationBeforeCall, callSymbol);
                        final L target = learned.getReturnSuccessor(source, returnSymbol, stackSymLearned);
                        assert target != null;

                        if (!Objects.equals(target, binLocation)) {
                            final int stackSymNew = withoutBin.encodeStackSym(oldToNewLocations.get(locationBeforeCall),
                                    callSymbol);
                            withoutBin.setReturnSuccessor(oldToNewLocations.get(source), returnSymbol, stackSymNew,
                                    oldToNewLocations.get(target));
                        }
                    }
                }
            }
        }

        return withoutBin;
    }

    private <L> int numberOfCallTransitions(OneSEVPA<L, JSONSymbol> vpa) {
        return vpa.size() * vpa.getInputAlphabet().getNumCalls();
    }

    private <L> int numberOfReturnTransitions(OneSEVPA<L, JSONSymbol> vpa) {
        int number = 0;
        for (L location : vpa.getLocations()) {
            for (JSONSymbol symbol : vpa.getInputAlphabet().getReturnAlphabet()) {
                for (L source : vpa.getLocations()) {
                    for (JSONSymbol call : vpa.getInputAlphabet().getCallAlphabet()) {
                        if (vpa.getReturnSuccessor(location, symbol, vpa.encodeStackSym(source, call)) != null) {
                            number++;
                        }
                    }
                }
            }
        }
        return number;
    }

    private <L> int numberOfInternalTransitions(OneSEVPA<L, JSONSymbol> vpa) {
        int number = 0;
        for (L location : vpa.getLocations()) {
            for (JSONSymbol symbol : vpa.getInputAlphabet().getInternalAlphabet()) {
                if (vpa.getInternalSuccessor(location, symbol) != null) {
                    number++;
                }
            }
        }
        return number;
    }

    protected abstract EquivalenceOracle<OneSEVPA<?, JSONSymbol>, JSONSymbol, Boolean> getEquivalenceOracle(
            int numberTests, boolean canGenerateInvalid, int maxDocumentDepth, int maxProperties, int maxItems,
            JSONSchema schema, Random random, boolean shuffleKeys, VPDAlphabet<JSONSymbol> alphabet);
}
