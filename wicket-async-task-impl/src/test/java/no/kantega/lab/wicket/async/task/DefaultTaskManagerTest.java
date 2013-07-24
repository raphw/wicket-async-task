package no.kantega.lab.wicket.async.task;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

public class DefaultTaskManagerTest {

    private DefaultTaskManager taskManager;

    @BeforeMethod
    public void setUp() throws Exception {
        taskManager = new DefaultTaskManager() {
            @Override
            protected AbstractTaskModel makeTaskModel(final String id) {
                return new AbstractTaskModel(id) {
                    @Override
                    protected ITaskManagerHook load() {
                        return taskManager.makeTaskManagerHook(id);
                    }
                };
            }
        };
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testTaskRemovalRemoved() throws Exception {

        final String testId = "test";
        final long removalMilliseconds = 200L;
        final double sleepMultiplier = 2d;

        assertTrue(sleepMultiplier > 1d);

        taskManager.makeOrRenewModel(testId, removalMilliseconds, TimeUnit.MILLISECONDS);

        Thread.sleep((long) (removalMilliseconds * sleepMultiplier));

        // Since the implementation is built on top of weak references, GC must be triggered manually
        taskManager.cleanUp();
        System.gc();

        taskManager.getModelOrFail(testId);
    }

    @Test
    public void testTaskRemovalNonRemoved() throws Exception {

        final String testId = "test";
        final long removalMilliseconds = 200L;
        final double sleepMultiplier = 0.5d;

        assertTrue(sleepMultiplier < 1d);

        taskManager.makeOrRenewModel(testId, removalMilliseconds, TimeUnit.MILLISECONDS);

        Thread.sleep((long) (removalMilliseconds * sleepMultiplier));
        taskManager.cleanUp();

        taskManager.getModelOrFail(testId);
    }
}