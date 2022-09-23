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
