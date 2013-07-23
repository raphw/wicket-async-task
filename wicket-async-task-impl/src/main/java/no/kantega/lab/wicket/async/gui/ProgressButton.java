package no.kantega.lab.wicket.async.gui;

import no.kantega.lab.wicket.async.task.AbstractTaskModel;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.time.Duration;

import java.lang.reflect.Method;

public class ProgressButton extends AjaxFallbackButton {

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
        super(id, model, form);
        this.taskModel = taskModel;
        this.runnableFactory = runnableFactory;
        this.refreshBehavior = new RefreshBehavior(duration);
        this.setOutputMarkupId(true);
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
        } else {
            return;
        }

        if (target != null) {
            activateRefresh(target);
            target.add(this);
        }

    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && (canStart() || canRestart() || canInterrupt());
    }

    private void activateRefresh(AjaxRequestTarget target) {
        if (getBehaviors(RefreshBehavior.class).size() == 0) {
            add(refreshBehavior);
        } else {
            refreshBehavior.restart(target);
        }
    }

    protected void refresh(AjaxRequestTarget target) {
        target.add(this);
        if (!taskModel.isRunning()) {
            refreshBehavior.stop(target);
        }
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

}
