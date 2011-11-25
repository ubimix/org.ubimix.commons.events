/**
 * 
 */
package org.webreformatter.commons.events.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.webreformatter.commons.events.EventManager;
import org.webreformatter.commons.events.IEventListener;
import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.calls.CallEvent;
import org.webreformatter.commons.events.calls.CallListener;

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

    private void doTestCallBarrier(Set<String> people) {
        class MyEvent extends CallEvent<String, String> {
            public MyEvent(String request) {
                super(request);
            }
        }
        IEventManager manager = new AsyncEventManager();
        final Set<String> resultGreetingSet = Collections
            .synchronizedSet(new HashSet<String>());
        final Set<String> resultPeopleSet = Collections
            .synchronizedSet(new HashSet<String>());
        manager.addListener(MyEvent.class, new CallListener<MyEvent>() {
            @Override
            protected void handleRequest(MyEvent event) {
                String result = "Hello, " + event.getRequest() + "!";
                event.setResponse(result);
            }
        });

        CallBarrier barrier = new CallBarrier();
        for (String person : people) {
            manager.fireEvent(
                new MyEvent(person),
                barrier.add(new CallListener<MyEvent>() {
                    @Override
                    protected void handleResponse(MyEvent event) {
                        resultPeopleSet.add(event.getRequest());
                        resultGreetingSet.add(event.getResponse());
                    };
                }));
        }
        barrier.await();
        assertEquals(people.size(), resultPeopleSet.size());
        for (String person : people) {
            assertTrue(resultPeopleSet.contains(person));
        }

        Set<String> greetingControl = new HashSet<String>();
        for (String person : people) {
            String greeting = "Hello, " + person + "!";
            greetingControl.add(greeting);
        }
        assertEquals(greetingControl, resultGreetingSet);
    }

    private void doTestCallBarrier(String... people) {
        Set<String> set = new LinkedHashSet<String>();
        for (String person : people) {
            set.add(person);
        }
        doTestCallBarrier(set);
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

    public void testCallBarrier() {
        doTestCallBarrier();
        doTestCallBarrier("John");
        doTestCallBarrier("John", "Bill", "Mike");
        Set<String> set = new LinkedHashSet<String>();
        int count = 10000;
        for (int i = 0; i < count; i++) {
            String name = "Abc " + i;
            set.add(name);
        }
        doTestCallBarrier(set);
    }

    public void testCallBarrier1() {
        IEventManager manager = new EventManager();
        class MyEvent extends CallEvent<String, String> {
            public MyEvent(String request) {
                super(request);
            }
        }
        manager.addListener(MyEvent.class, new CallListener<MyEvent>() {
            @Override
            protected void handleRequest(MyEvent event) {
                String name = event.getRequest();
                String response = "Hello " + name + "!";
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
                event.setResponse(response);
            };
        });
        MyEvent event = new MyEvent("Smith");
        String result = CallBarrier.syncCall(manager, event);
        assertEquals("Hello Smith!", result);
    }
}
