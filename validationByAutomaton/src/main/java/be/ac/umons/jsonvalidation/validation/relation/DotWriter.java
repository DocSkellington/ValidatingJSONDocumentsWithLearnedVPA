package be.ac.umons.jsonvalidation.validation.relation;

import java.io.IOException;
import java.util.Set;

import com.google.common.graph.EndpointPair;

public class DotWriter {
    public static void write(final ReachabilityGraph graph, final Appendable output) throws IOException {
        final Set<NodeInGraph> nodes = graph.nodes();
        final Set<EndpointPair<NodeInGraph>> edges = graph.edges();

        output.append("strict digraph G {\n");
        for (final NodeInGraph node : nodes) {
            output.append("    \"" + node + "\" [shape=\"circle\"];\n");
        }
        for (final EndpointPair<NodeInGraph> edge : edges) {
            final NodeInGraph source = edge.nodeU();
            final NodeInGraph target = edge.nodeV();
            output.append("    \"" + source + "\" -> \"" + target + "\";\n");
        }
        output.append("}");
    }
}
