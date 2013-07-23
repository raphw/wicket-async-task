package no.kantega.lab.wicket.async.demo;

import no.kantega.lab.wicket.async.components.IRunnableFactory;
import no.kantega.lab.wicket.async.components.ProgressBar;
import no.kantega.lab.wicket.async.components.ProgressButton;
import no.kantega.lab.wicket.async.components.TaskState;
import no.kantega.lab.wicket.async.task.AbstractTaskModel;
import no.kantega.lab.wicket.async.task.DefaultTaskManager;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;

import java.util.concurrent.TimeUnit;

public class DemoPage extends WebPage implements IRunnableFactory {

    public DemoPage() {

        // Create form
        Form<?> form = new Form<Void>("form");

        // Create model of task
        AbstractTaskModel taskModel = DefaultTaskManager.getInstance().makeModel(1000L, TimeUnit.MINUTES);

        // Create a progress button.
        ProgressButton progressButton = new ProgressButton("button", form, taskModel, this, Duration.milliseconds(500L));

        progressButton.registerTaskStateMessageModel(TaskState.START, Model.of("Start"));
        progressButton.registerTaskStateMessageModel(TaskState.RESTART, Model.of("Restart"));
        progressButton.registerTaskStateMessageModel(TaskState.CANCEL, Model.of("Cancel"));
        progressButton.registerTaskStateMessageModel(TaskState.RUNNING, Model.of("Running..."));

        progressButton.registerTaskStateCssClassModel(TaskState.START, Model.of("btn btn-primary"));
        progressButton.registerTaskStateCssClassModel(TaskState.RESTART, Model.of("btn btn-success"));
        progressButton.registerTaskStateCssClassModel(TaskState.CANCEL, Model.of("btn btn-warning"));
        progressButton.registerTaskStateCssClassModel(TaskState.RUNNING, Model.of("btn"));

        // Create a progress bar
        ProgressBar progressBar = new ProgressBar("bar", progressButton);

        // Add components to page
        add(form);
        form.add(progressButton);
        form.add(progressBar);
    }

    @Override
    public Runnable getRunnable() {
        return new GenericTask(10, 150L);
    }
}
