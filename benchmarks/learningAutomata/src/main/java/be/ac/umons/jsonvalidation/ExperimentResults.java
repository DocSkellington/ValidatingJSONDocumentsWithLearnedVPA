package be.ac.umons.jsonvalidation;

public class ExperimentResults {
    public final boolean finished;
    public final boolean error;
    public final long timeInMillis;

    ExperimentResults(boolean finished, boolean error, long timeInMillis) {
        this.finished = finished;
        this.error = error;
        this.timeInMillis = timeInMillis;
    }
}
