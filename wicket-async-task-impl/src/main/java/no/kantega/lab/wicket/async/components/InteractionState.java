package no.kantega.lab.wicket.async.components;

public enum InteractionState {

    STARTABLE,
    RESTARTABLE,
    CANCELABLE,
    NON_INTERACTIVE;

    public static InteractionState findInteractionState(ProgressButton progressButton) {
        if (progressButton.canStart()) {
            return STARTABLE;
        } else if (progressButton.canRestart()) {
            return RESTARTABLE;
        } else if (progressButton.canInterrupt()) {
            return CANCELABLE;
        } else {
            return NON_INTERACTIVE;
        }
    }

}
