This is a small extension to Wicket that allows the management of asynchronous tasks (starting, interrupting and restarting) and to display a task's progress and state to the user. In order to allow tasks to communicate their progress, they have to implement `IProgressObservableRunnable` which is provided in this package. 

Also, tasks can be managed outside of the scope of a webpage without breaking Wicket's contract that all `Component`s must be `Serializable` by offering a `TaskManager` interface. A default implementation of such a task manager is provided.

The extension can be used like this:

```java
public class DemoPage extends WebPage implements IRunnableFactory {

    public DemoPage() {

        Form<?> form = new Form<Void>("form");
        AbstractTaskContainer taskContainer = DefaultTaskManager.getInstance()
            .makeContainer(1000L, TimeUnit.MINUTES);
        ProgressButton progressButton = new ProgressButton("button", form, 
            Model.of(taskContainer), this, Duration.milliseconds(500L));
        ProgressBar progressBar = new ProgressBar("bar", progressButton);

        add(form);
        form.add(progressButton);
        form.add(progressBar);
    }

    @Override
    public Runnable getRunnable() {
        return new MyRunnable();
    }
    
    private static class MyRunnable implements IProgressObservableRunnable {
        // Implementation of a Runnable with observable progress
        ...
    }
}
```

A demo application is provided in *wicket-async-task-demo*. The actual implementation can be found *wicket-async-task-impl*.

**Important note**: Do not implement `Runnable`s as non-static inner classes of a Wicket class. (This is rather common in Wicket, especially with concern to anonymous inner classes, so be careful.) These classes are expected to be detached and serialized by the Wicket framework. Therfore, do not attemt the following:

```java
public class DemoPage extends WebPage implements IRunnableFactory {
    
    ...

    @Override
    public Runnable getRunnable() {
        return new IProgressObservableRunnable() {
            // This class keeps an implicit reference to its 
            // containing WebPage. The page might be in a 
            // detached state when the inner class tries to access it.
            // Also, this will cause a memory leak to you application 
            // since this instance can not be garbage collected as 
            // long as this task did not expire.
        }
    }
}
```

In addition, do not attempt to call Wicket methods from the background task. If you for example want to transform exceptions in background threads into Wicket error messages, this would be what you want to do:

```java
public class DemoPage extends WebPage implements IRunnableFactory {

    public DemoPage() {

        ...
        
        ProgressButton progressButton = new ProgressButton("button", form, 
            Model.of(taskContainer), this, Duration.milliseconds(500L)) {
            
                @Override
                protected void onTaskError(AjaxRequestTarget ajaxRequestTarget) {
                    // This method is always executed from the GUI thread.
                    // Therefore, it is safe to poll the possible exception
                    // from the background thread.
                    Throwable th = getTaskContainer().getExecutionError();
                    if (th != null) {
                        error(th.getLocalizedMessage()));
                    }
                }
                
        }
    }
    
    ...
}
```

As long as you keep your background tasks free from references to your Wicket classes, you are safe. Be particularly careful with the following:
* Using non-static implementations of *IModel*: models are often implemented as anonymous classes. Therefore, they carry a reference to the containing `Component`.
* Using *@SpringBean* annotated variables: Those Spring beans are actually bound to the application and implemented as proxies. Instead, use `WebApplication.get().getServletContext()` in order to find the beans directly by `WebApplicationContextUtils.getWebApplicationContext(servletContext).getBean(MyBean.class)`

Licensed under the Apache Software License, Version 2.0
