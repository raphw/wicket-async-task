package no.kantega.lab.wicket.async.task;

public interface IProgressObservableRunnable extends Runnable {

    double getProgress();

    String getProgressMessage();
}
