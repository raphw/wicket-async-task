package no.kantega.lab.wicket.async.task;

import org.apache.wicket.model.LoadableDetachableModel;

import java.util.concurrent.*;

public abstract class AbstractTaskModel extends LoadableDetachableModel<ITaskManagerHook> {

    private final String id;

    private ExecutionException executionException;

    protected AbstractTaskModel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    private Future<?> getFuture() {
        return getObject().getFuture();
    }

    private Runnable getRunnable() {
        return getObject().getRunnable();
    }

    private IProgressObservableRunnable getProgressObservableRunnable() {
        if (getRunnable() == null) {
            return null;
        } else if (getRunnable() instanceof IProgressObservableRunnable) {
            return (IProgressObservableRunnable) getRunnable();
        } else {
            return null;
        }
    }

    public boolean isSubmitted() {
        return getRunnable() != null;
    }

    public boolean isRunning() {
        return isSubmitted() && !getFuture().isDone();
    }

    public boolean isComplete() {
        return getFuture().isDone() && !getFuture().isCancelled() && !isFailed();
    }

    public boolean isFailed() {
        checkForExecutionError();
        return executionException != null;
    }

    public boolean isCancelled() {
        return getFuture().isCancelled();
    }

    public Throwable getExecutionError() {
        checkForExecutionError();
        return executionException.getCause();
    }

    public void submit(Runnable runnable) {
        executionException = null;
        getObject().submit(runnable, true);
    }

    public void cancel() {
        getFuture().cancel(true);
    }

    private void checkForExecutionError() {
        try {
            getFuture().get(1L, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            /* this can only happen if the server's current dispatcher
            thread gets interrupted and this should not happen */
            throw new RuntimeException("Thread was interrupted", e);
        } catch (ExecutionException e) {
            executionException = e;
        } catch (TimeoutException e) {
            /* do nothing */
        } catch (CancellationException e) {
            /* do nothing */
        }
    }

    public Double getProgress() {
        IProgressObservableRunnable runnable = getProgressObservableRunnable();
        if (runnable != null) {
            return runnable.getProgress();
        } else {
            return null;
        }
    }

    public String getProgressMessage() {
        IProgressObservableRunnable runnable = getProgressObservableRunnable();
        if (runnable != null) {
            return runnable.getProgressMessage();
        } else {
            return null;
        }
    }
}
