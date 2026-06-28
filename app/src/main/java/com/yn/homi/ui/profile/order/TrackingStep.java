package com.yn.homi.ui.profile.order;

public class TrackingStep {
    private String label;
    private String time;
    private boolean isDone;

    public TrackingStep(String label, String time, boolean isDone) {
        this.label = label;
        this.time = time;
        this.isDone = isDone;
    }

    public String getLabel() { return label; }
    public String getTime() { return time; }
    public boolean isDone() { return isDone; }
}
