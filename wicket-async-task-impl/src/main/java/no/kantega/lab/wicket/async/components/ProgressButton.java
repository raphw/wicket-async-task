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
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ProgressButton extends AjaxFallbackButton {

    private final Map<StateDescription, IModel<String>> stateTextModels;
    private final Map<StateDescription, IModel<String>> stateCssClasses;

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

        this.stateTextModels = new HashMap<StateDescription, IModel<String>>();
        this.setModel(new StateDispatcherModel<String>(getDefaultTextModel(model), stateTextModels));

        this.stateCssClasses = new HashMap<StateDescription, IModel<String>>();
        this.add(new AttributeAppender("class", new StateDispatcherModel<String>(new Model<String>(), stateCssClasses), " "));

        this.setOutputMarkupId(true);

        activateRefresh(null);
    }

    private IModel<String> getDefaultTextModel(IModel<String> userModel) {
        if (userModel == null) {
            return new Model<String>();
        } else {
            return userModel;
        }
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

    boolean canStart() {
        return runnableFactory != null && isAllowStart() && !taskModel.isSubmitted() && !taskModel.isRunning();
    }

    boolean canRestart() {
        return runnableFactory != null && isAllowRestart() && taskModel.isSubmitted() && !taskModel.isRunning();
    }

    boolean canInterrupt() {
        return isAllowInterrupt() && !taskModel.isCancelled() && taskModel.isRunning();
    }

    @Override
    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        super.onSubmit(target, form);

        if (canStart() || canRestart()) {
            taskModel.submit(runnableFactory.getRunnable());
            onTaskStart(taskModel);
            System.out.println(" -> New task submitted");
        } else if (canInterrupt()) {
            taskModel.cancel();
            onTaskCancel(taskModel);
            System.out.println(" -> Task interrupted");
        } else {
            System.out.println(" -> Ignored button press");
            return;
        }

        if (target != null) {
            renderAll(target);
            activateRefresh(target);
        }
    }

    private void activateRefresh(AjaxRequestTarget target) {
        if (!taskModel.isRunning()) {
            if (getBehaviors(RefreshBehavior.class).size() > 0) {
                refreshBehavior.stop(target);
            }
            if (taskModel.isFailed()) {
                onTaskError(taskModel);
            } else {
                onTaskSuccess(taskModel);
            }
        } else if (getBehaviors(RefreshBehavior.class).size() == 0) {
            add(refreshBehavior);
        } else {
            refreshBehavior.restart(target);
        }
    }

    protected void refresh(AjaxRequestTarget target) {
        if (!taskModel.isRunning()) {
            refreshBehavior.stop(target);
        }
        renderAll(target);
    }

    private void renderAll(AjaxRequestTarget target) {
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

    public void registerMessageModel(IModel<String> textModel, TaskState taskState, InteractionState interactionState) {
        stateTextModels.put(new StateDescription(taskState, interactionState), textModel);
    }

    public void registerMessageModel(IModel<String> textModel, TaskState... taskStates) {
        for (TaskState taskState : taskStates) {
            for (InteractionState interactionState : InteractionState.values()) {
                registerMessageModel(textModel, taskState, interactionState);
            }
        }
    }

    public void registerMessageModel(IModel<String> textModel, InteractionState... interactionStates) {
        for (InteractionState interactionState : interactionStates) {
            for (TaskState taskState : TaskState.values()) {
                registerMessageModel(textModel, taskState, interactionState);
            }
        }
    }

    public void registerCssClassModel(IModel<String> textModel, TaskState taskState, InteractionState interactionState) {
        stateCssClasses.put(new StateDescription(taskState, interactionState), textModel);
    }

    public void registerCssClassModel(IModel<String> textModel, TaskState... taskStates) {
        for (TaskState taskState : taskStates) {
            for (InteractionState interactionState : InteractionState.values()) {
                registerCssClassModel(textModel, taskState, interactionState);
            }
        }
    }

    public void registerCssClassModel(IModel<String> textModel, InteractionState... interactionStates) {
        for (InteractionState interactionState : interactionStates) {
            for (TaskState taskState : TaskState.values()) {
                registerCssClassModel(textModel, taskState, interactionState);
            }
        }
    }

    class StateDispatcherModel<T> extends AbstractReadOnlyModel<T> {

        private final IModel<T> defaultValue;

        private final Map<StateDescription, IModel<T>> stateValues;

        StateDispatcherModel(IModel<T> defaultValue, Map<StateDescription, IModel<T>> taskStateValues) {
            this.defaultValue = defaultValue;
            this.stateValues = taskStateValues;
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
            return stateValues.get(
                    new StateDescription(
                            TaskState.findRunningState(taskModel),
                            InteractionState.findInteractionState(ProgressButton.this)
                    )
            );
        }
    }

    public void addRefreshDependant(Component refreshDependant) {
        refreshDependants.add(refreshDependant);
    }

    public void removeRefreshDependant(Component refreshDependant) {
        refreshDependants.remove(refreshDependant);
    }

    protected void onTaskStart(AbstractTaskModel taskModel) {

    }

    protected void onTaskSuccess(AbstractTaskModel taskModel) {

    }

    protected void onTaskCancel(AbstractTaskModel taskModel) {

    }

    protected void onTaskError(AbstractTaskModel taskModel) {

    }
}
