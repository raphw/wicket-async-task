package no.kantega.lab.wicket.async.gui;

import java.io.Serializable;

public interface IRunnableFactory extends Serializable {

    Runnable getRunnable();
}
