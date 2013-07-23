package no.kantega.lab.wicket.async.task;

public class DefaultTaskModel extends AbstractTaskModel {

    public DefaultTaskModel(String id) {
        super(id);
    }

    @Override
    protected ITaskManagerHook load() {
        return DefaultTaskManager.getInstance().findHookForId(getId());
    }
}
