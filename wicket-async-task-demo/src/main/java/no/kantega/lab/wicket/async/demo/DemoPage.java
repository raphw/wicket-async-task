package no.kantega.lab.wicket.async.demo;

import no.kantega.lab.wicket.async.gui.IRunnableFactory;
import no.kantega.lab.wicket.async.gui.ProgressButton;
import no.kantega.lab.wicket.async.task.DefaultTaskManager;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;

import java.util.concurrent.TimeUnit;

public class DemoPage extends WebPage implements IRunnableFactory {

    public DemoPage() {

        Form<?> form = new Form<Void>("form");

        final ProgressButton progressButton = new ProgressButton("button", Model.of("start"), form,
                DefaultTaskManager.getInstance().makeModel(1000L, TimeUnit.MINUTES), this, Duration.milliseconds(500L)) {
            @Override
            protected boolean isAllowInterrupt() {
                return false;
            }
        };

        add(form);
        form.add(progressButton);
    }

    @Override
    public Runnable getRunnable() {
        return new GenericTask(10, 150L);
    }
}
