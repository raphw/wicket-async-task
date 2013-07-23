package no.kantega.lab.wicket.async.demo;

import no.kantega.lab.wicket.async.task.IProgressObservableRunnable;

public class GenericTask implements IProgressObservableRunnable {

    private final int steps;
    private final long waitingTime;

    public GenericTask(int steps, long waitingTime) {
        this.steps = steps;
        this.waitingTime = waitingTime;
    }

    private double progress;
    private String message;

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public String getProgressMessage() {
        return message;
    }

    private void setProgress(double progress) {
        this.progress = progress;
    }

    private void setProgressMessage(String message) {
        this.message = message;
    }

    @Override
    public void run() {

        double progressIncrement = 1d / steps;

        try {

            for (int i = 0; i < steps; i++) {
                Thread.sleep(waitingTime);
                setProgress(this.progress + progressIncrement);
                System.out.printf("Background task: %.2f (%d/%d)%n", progress, i + 1, steps);
            }

        } catch (InterruptedException e) {
            System.out.printf("Interrupted task: %.2f%n", progress);
        }

        System.out.printf("Progress finished: %.2f%n", progress);
    }
}
