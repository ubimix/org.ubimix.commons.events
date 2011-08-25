/**
 * 
 */
package org.webreformatter.commons.events.server;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.webreformatter.commons.events.IEventListener;

/**
 * @author kotelnikov
 */
public class AsyncEventManagerTest extends TestCase {

    /**
     * @param name
     */
    public AsyncEventManagerTest(String name) {
        super(name);
    }

    public void test() throws Exception {
        test(10000, new Executor() {
            public void execute(Runnable command) {
                command.run();
            }
        });
        test(10000, Executors.newCachedThreadPool());
    }

    /**
     * This test creates the specified number of events. The registered listener
     * generates new events for each received events. It is used to check that
     * the asynchronous event manager properly handles events fired in external
     * threads as well as in internal (working) threads.
     * 
     * @param count
     * @param executor
     * @throws Exception
     */
    public void test(final int count, final Executor executor) throws Exception {
        final int totalCycleNumbers = 3;
        FutureTask<Integer> task = new FutureTask<Integer>(
            new Callable<Integer>() {
                public Integer call() throws Exception {
                    final AsyncEventManager manager = new AsyncEventManager();
                    manager.setExecutor(executor);
                    final int[] counter = { 0 };
                    manager.addListener(
                        String.class,
                        new IEventListener<String>() {
                            public void handleEvent(String event) {
                                int pos = event.lastIndexOf(".");
                                int cycle = Integer.parseInt(event
                                    .substring(pos + 1));
                                if (cycle < totalCycleNumbers) {
                                    // Re-fire the event
                                    event = event.substring(0, pos + 1)
                                        + (cycle + 1);
                                    manager.fireEvent(event);
                                } else {
                                    counter[0]++;
                                }
                            }
                        });
                    int cycle = 0;
                    for (int i = 0; i < count; i++) {
                        manager.fireEvent("Event-" + i + "." + cycle);
                    }
                    while (counter[0] < count) {
                        Thread.sleep(50);
                    }
                    return counter[0];
                }
            });
        Executors.newSingleThreadExecutor().execute(task);
        Integer result = task.get(10, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals(count, result.intValue());
    }
}
