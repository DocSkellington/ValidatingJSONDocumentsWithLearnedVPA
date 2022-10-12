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

package be.ac.umons.jsonvalidation.graph;

import java.io.IOException;
import java.util.Set;

import com.google.common.graph.EndpointPair;

/**
 * Implements utilities to write a {@link KeyGraph} as the contents of a DOT
 * file.
 * 
 * @author Gaëtan Staquet
 */
public class KeyGraphToDot {
    /**
     * Describes the provided key graph using DOT file format.
     * 
     * @param <L>    The type of locations used in the vertices of the key graph
     * @param graph  The key graph
     * @param output Where to write the DOT file in
     * @throws IOException
     */
    public static <L> void write(final KeyGraph<L> graph, final Appendable output) throws IOException {
        final Set<NodeInGraph<L>> nodes = graph.nodes();
        final Set<EndpointPair<NodeInGraph<L>>> edges = graph.edges();

        output.append("strict digraph G {\n");
        for (final NodeInGraph<L> node : nodes) {
            output.append("    '" + node + "' [shape=\"circle\"];\n");
        }
        for (final EndpointPair<NodeInGraph<L>> edge : edges) {
            final NodeInGraph<L> source = edge.nodeU();
            final NodeInGraph<L> target = edge.nodeV();
            output.append("    '" + source + "' -> '" + target + "';\n");
        }
        output.append("}");
    }
}
