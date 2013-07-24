package no.kantega.lab.wicket.async.task;

import java.util.concurrent.TimeUnit;

public interface ITaskManager {

    AbstractTaskModel makeModel(long lifeTime, TimeUnit unit);

    AbstractTaskModel makeOrRenewModel(String id, long lifeTime, TimeUnit unit);

    AbstractTaskModel getModelOrFail(String id);

    void cleanUp();
}
