package no.kantega.lab.wicket.async.util;

import org.apache.wicket.model.IModel;

/**
 * A model implementation that relies on a volatile variable which allows it to be used
 * by different tasks.
 *
 * @param <T> The underlying variable type.
 */
public class VolatileModel<T> implements IModel<T> {

    private volatile T object;

    public VolatileModel() {
        /* empty */
    }

    public VolatileModel(T object) {
        this.object = object;
    }

    @Override
    public T getObject() {
        return object;
    }

    @Override
    public void setObject(T object) {
        this.object = object;
    }

    @Override
    public void detach() {
        /* empty */
    }
}
