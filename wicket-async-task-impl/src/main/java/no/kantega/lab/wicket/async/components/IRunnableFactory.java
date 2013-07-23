package no.kantega.lab.wicket.async.components;

import java.io.Serializable;

public interface IRunnableFactory extends Serializable {

    Runnable getRunnable();
}
