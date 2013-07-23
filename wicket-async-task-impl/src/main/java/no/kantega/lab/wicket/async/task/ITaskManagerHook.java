package no.kantega.lab.wicket.async.task;

import java.util.concurrent.Future;

public interface ITaskManagerHook {

    String getId();

    Future<?> getFuture();

    Runnable getRunnable();

    void submit(Runnable runnable, boolean cancelExistent);
}
