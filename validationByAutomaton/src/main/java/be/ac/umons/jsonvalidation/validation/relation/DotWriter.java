package be.ac.umons.jsonvalidation.validation.relation;

import java.io.IOException;
import java.util.Set;

import com.google.common.graph.EndpointPair;

public class DotWriter {
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
