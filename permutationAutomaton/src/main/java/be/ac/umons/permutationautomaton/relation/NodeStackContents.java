package be.ac.umons.permutationautomaton.relation;

import javax.annotation.Nullable;

public class NodeStackContents {
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

    public NodeStackContents push(boolean rejected) {
        return new NodeStackContents(rejected, this);
    }

    public static NodeStackContents push(boolean rejected, @Nullable final NodeStackContents rest) {
        return new NodeStackContents(rejected, rest);
    }
}
