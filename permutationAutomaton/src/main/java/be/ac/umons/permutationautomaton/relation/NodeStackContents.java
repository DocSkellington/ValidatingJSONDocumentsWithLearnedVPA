package be.ac.umons.permutationautomaton.relation;

import javax.annotation.Nullable;

/**
 * The stack used in a {@link NodeInGraph}.
 * 
 * It contains a boolean to indicate whether the node is rejected, and a pointer
 * to the rest of the stack.
 * 
 * @author Gaëtan Staquet
 */
class NodeStackContents {
    private boolean rejected;
    private @Nullable final NodeStackContents rest;

    private NodeStackContents(boolean rejected) {
        this(rejected, null);
    }

    private NodeStackContents(boolean rejected, NodeStackContents rest) {
        this.rejected = rejected;
        this.rest = rest;
    }

    public boolean peek() {
        return rejected;
    }

    public void markRejected() {
        rejected = true;
    }

    public NodeStackContents pop() {
        return rest;
    }

    public static NodeStackContents push(boolean rejected, @Nullable final NodeStackContents rest) {
        return new NodeStackContents(rejected, rest);
    }
}
