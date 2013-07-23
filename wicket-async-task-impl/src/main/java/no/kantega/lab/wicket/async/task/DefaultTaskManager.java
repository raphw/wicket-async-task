package no.kantega.lab.wicket.async.task;

import com.google.common.collect.MapMaker;

import java.util.UUID;
import java.util.concurrent.*;

public class DefaultTaskManager implements ITaskManager {

    private static final DefaultTaskManager INSTANCE = new DefaultTaskManager();

    public static DefaultTaskManager getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<String, ITaskManagerHook> taskManagerHooks;
    private final ExecutorService executorService;

    public DefaultTaskManager() {
        this.taskManagerHooks = new MapMaker().makeMap();
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public AbstractTaskModel makeModel(long lifeTime, TimeUnit unit) {
        return makeOrGetModel(UUID.randomUUID().toString(), lifeTime, unit);
    }

    @Override
    public AbstractTaskModel makeOrGetModel(String id, long lifeTime, TimeUnit unit) {
        taskManagerHooks.putIfAbsent(id, new DefaultTaskManagerHook(id));
        return new DefaultTaskModel(id);
    }

    @Override
    public AbstractTaskModel getModelOrFail(String id) {
        if (!taskManagerHooks.containsKey(id)) throw new IllegalArgumentException("Id " + id + " is not registered");
        return new DefaultTaskModel(id);
    }

    protected ITaskManagerHook findHookForId(String id) {
        return taskManagerHooks.get(id);
    }

    protected Future<?> submit(Runnable runnable) {
        return executorService.submit(runnable);
    }

}
