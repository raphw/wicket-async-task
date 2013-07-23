package no.kantega.lab.wicket.async.components;

import no.kantega.lab.wicket.async.task.AbstractTaskModel;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import java.util.HashMap;
import java.util.Map;

public class ProgressBar extends Panel {

    private final ProgressButton progressButton;

    private final Map<TaskState, IModel<String>> taskStateCssClasses;

    public ProgressBar(String id, ProgressButton progressButton) {
        super(id);

        this.progressButton = progressButton;

        WebMarkupContainer wrapper = makeWrapper("wrapper");
        add(wrapper);

        wrapper.add(makeBar("bar").add(new AttributeAppender("style", new TaskProgressPercentageStyleModel())));
        wrapper.add(new Label("message", new TaskProgressMessageModel()));

        taskStateCssClasses = new HashMap<TaskState, IModel<String>>();

//        wrapper.add(new AttributeAppender("class", progressButton.new TaskStateDispatcherModel()));

        progressButton.addRefreshDependant(this);

        this.setOutputMarkupId(true);
    }

    private AbstractTaskModel getTaskModel() {
        return progressButton.getTaskModel();
    }

    protected WebMarkupContainer makeWrapper(String id) {
        return new WebMarkupContainer(id);
    }

    protected WebMarkupContainer makeBar(String id) {
        return new WebMarkupContainer(id);
    }

    protected boolean isShowPercentage() {
        return true;
    }

    protected double getDefaultWidth() {
        return 0d;
    }

    private class TaskProgressMessageModel extends AbstractReadOnlyModel<String> {
        @Override
        public String getObject() {
            Double progress = getTaskModel().getProgress();
            String suffix = "";
            if (isShowPercentage()) {
                if (progress != null) {
                    suffix = String.format("(%d%%)", getPercentProgress());
                }
            }
            String message = getTaskModel().getProgressMessage();
            if (message == null) {
                message = "";
            }
            return String.format("%s %s", message, suffix);
        }
    }

    private class TaskProgressPercentageStyleModel extends AbstractReadOnlyModel<String> {
        @Override
        public String getObject() {
            int percentProgress = getPercentProgress();
            return String.format("width: %d%%;", percentProgress);
        }
    }

    private int getPercentProgress() {
        double width = getTaskModel().getProgress() == null ? getDefaultWidth() : getTaskModel().getProgress();
        return (int) Math.round(Math.max(Math.min(width, 1d), 0d) * 100d);
    }
}
