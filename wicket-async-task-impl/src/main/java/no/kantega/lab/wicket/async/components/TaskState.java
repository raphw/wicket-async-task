package no.kantega.lab.wicket.async.components;

import no.kantega.lab.wicket.async.task.AbstractTaskModel;

public enum TaskState {

    PLAIN_RUNNING,
    PLAIN_NON_RUNNING,
    ERROR_NON_RUNNING,
    CANCELED_RUNNING,
    CANCELED_NON_RUNNING;

    public static TaskState findRunningState(AbstractTaskModel taskModel) {
        if (taskModel.isRunning()) {
            if (taskModel.isCancelled()) {
                return CANCELED_RUNNING;
            } else {
                return PLAIN_RUNNING;
            }
        } else {
            if (taskModel.isFailed()) {
                return ERROR_NON_RUNNING;
            } else if (taskModel.isCancelled()) {
                return CANCELED_NON_RUNNING;
            } else {
                return PLAIN_NON_RUNNING;
            }
        }
    }

}
