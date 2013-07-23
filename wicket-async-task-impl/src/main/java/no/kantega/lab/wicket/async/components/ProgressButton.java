package no.kantega.lab.wicket.async.components;

import no.kantega.lab.wicket.async.task.AbstractTaskModel;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.time.Duration;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ProgressButton extends AjaxFallbackButton {

    private final Map<TaskState, IModel<String>> taskStateTextMessages;
    private final Map<TaskState, IModel<String>> taskStateCssClasses;

    private final Collection<Component> refreshDependants;

    private final IRunnableFactory runnableFactory;
    private final AbstractTaskModel taskModel;

    private final RefreshBehavior refreshBehavior;

    public ProgressButton(String id, Form<?> form, AbstractTaskModel taskModel, Duration duration) {
        this(id, null, form, taskModel, null, duration);
    }

    public ProgressButton(String id, IModel<String> model, Form<?> form, AbstractTaskModel taskModel, Duration duration) {
        this(id, model, form, taskModel, null, duration);
    }

    public ProgressButton(String id, Form<?> form, AbstractTaskModel taskModel, IRunnableFactory runnableFactory, Duration duration) {
        this(id, null, form, taskModel, runnableFactory, duration);
    }

    public ProgressButton(String id, IModel<String> model, Form<?> form, AbstractTaskModel taskModel, IRunnableFactory runnableFactory, Duration duration) {
        super(id, null, form);

        this.taskModel = taskModel;
        this.runnableFactory = runnableFactory;

        this.refreshDependants = new HashSet<Component>();

        this.refreshBehavior = new RefreshBehavior(duration);

        this.taskStateTextMessages = new HashMap<TaskState, IModel<String>>();
        this.setModel(new TaskStateDispatcherModel<String>(getDefaultTextModel(model), taskStateTextMessages));

        this.taskStateCssClasses = new HashMap<TaskState, IModel<String>>();
        this.add(new AttributeAppender("class", new TaskStateDispatcherModel<String>(getDefaultCssClassModel(), taskStateCssClasses)));

        this.setOutputMarkupId(true);

        activateRefreshIfRequired(null);
    }

    private IModel<String> getDefaultTextModel(IModel<String> userModel) {
        if (userModel == null) {
            return new AbstractReadOnlyModel<String>() {
                @Override
                public String getObject() {
                    return getMarkupAttributes().getString("value", "");
                }
            };
        } else {
            return userModel;
        }
    }

    private IModel<String> getDefaultCssClassModel() {
        return new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return getMarkupAttributes().getString("class", "");
            }
        };
    }

    protected AbstractTaskModel getTaskModel() {
        return taskModel;
    }

    protected boolean isAllowStart() {
        return true;
    }

    protected boolean isAllowInterrupt() {
        return true;
    }

    protected boolean isAllowRestart() {
        return true;
    }

    private boolean canStart() {
        return runnableFactory != null && isAllowStart() && !taskModel.isSubmitted() && !taskModel.isRunning();
    }

    private boolean canRestart() {
        return runnableFactory != null && isAllowRestart() && taskModel.isSubmitted() && !taskModel.isRunning();
    }

    private boolean canInterrupt() {
        return isAllowInterrupt() && !taskModel.isCancelled() && taskModel.isRunning();
    }

    @Override
    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        super.onSubmit(target, form);

        if (canStart() || canRestart()) {
            taskModel.submit(runnableFactory.getRunnable());
        } else if (canInterrupt()) {
            taskModel.cancel();
        }

        if (target != null) {
            target.add(this);
            activateRefreshIfRequired(target);
        }
    }

    private void activateRefreshIfRequired(AjaxRequestTarget target) {
        if (!taskModel.isRunning()) {
            return;
        }
        if (getBehaviors(RefreshBehavior.class).size() == 0) {
            add(refreshBehavior);
        } else {
            refreshBehavior.restart(target);
        }
    }

    protected void refresh(AjaxRequestTarget target) {
        if (!taskModel.isRunning()) {
            refreshBehavior.stop(target);
        }
        target.add(this);
        for (Component c : refreshDependants) {
            target.add(c);
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && (canStart() || canRestart() || canInterrupt());
    }

    private class RefreshBehavior extends AbstractAjaxTimerBehavior {
        public RefreshBehavior(Duration updateInterval) {
            super(updateInterval);
        }

        @Override
        protected void onTimer(AjaxRequestTarget target) {
            refresh(target);
            System.out.println("-> Refresh");
        }

        @Override
        public boolean canCallListenerInterface(Component component, Method method) {
            // Skip check for the component being enabled
            return component.isVisibleInHierarchy();
        }

        @Override
        protected boolean shouldTrigger() {
            // Again, skip the check for the component being enabled
            return !isStopped() && getComponent().findParent(Page.class) != null;
        }
    }

    public void registerTaskStateMessageModel(TaskState state, IModel<String> textModel) {
        taskStateTextMessages.put(state, textModel);
    }

    public void registerTaskStateCssClassModel(TaskState state, IModel<String> cssClassModel) {
        taskStateCssClasses.put(state, cssClassModel);
    }

    class TaskStateDispatcherModel<T> extends AbstractReadOnlyModel<T> {

        private final IModel<T> defaultValue;

        private final Map<TaskState, IModel<T>> taskStateValues;

        TaskStateDispatcherModel(IModel<T> defaultValue, Map<TaskState, IModel<T>> taskStateValues) {
            this.defaultValue = defaultValue;
            this.taskStateValues = taskStateValues;
        }

        @Override
        public T getObject() {
            IModel<T> actualModel = getActualModel();
            if (actualModel == null) {
                return defaultValue.getObject();
            } else {
                return actualModel.getObject();
            }
        }

        private IModel<T> getActualModel() {
            if (canStart()) {
                return taskStateValues.get(TaskState.START);
            } else if (canRestart()) {
                return taskStateValues.get(TaskState.RESTART);
            } else if (canInterrupt()) {
                return taskStateValues.get(TaskState.CANCEL);
            } else if (taskModel.isFailed()) {
                return taskStateValues.get(TaskState.ERROR);
            } else {
                return taskStateValues.get(TaskState.RUNNING);
            }
        }
    }

    public void addRefreshDependant(Component refreshDependant) {
        refreshDependants.add(refreshDependant);
    }

    public void removeRefreshDependant(Component refreshDependant) {
        refreshDependants.remove(refreshDependant);
    }
}
