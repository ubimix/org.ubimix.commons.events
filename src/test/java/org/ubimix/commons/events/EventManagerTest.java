/**
 * 
 */
package org.ubimix.commons.events;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;

import org.ubimix.commons.events.EventManager;
import org.ubimix.commons.events.IEventListener;
import org.ubimix.commons.events.IEventListenerRegistration;
import org.ubimix.commons.events.IEventManager;

import junit.framework.TestCase;


/**
 * @author kotelnikov
 */
public class EventManagerTest extends TestCase {

    static class TestEvent {

        public final int id;

        public TestEvent(int id) {
            this.id = id;
        }
    }

    private static class TestEventA {
    }

    private static class TestEventB extends TestEventA {
    }

    private static class TestEventC extends TestEventA {
    }

    public static <T> T[] getArray(T... t) {
        return t;
    }

    /**
     * @param name
     */
    public EventManagerTest(String name) {
        super(name);
    }

    protected IEventManager newEventManager() {
        EventManager manager = new EventManager() {
            @Override
            protected void onError(
                Object event,
                IEventListener<?> callback,
                IEventListener<?> listener,
                Throwable error) {
                error.printStackTrace();
            }
        };
        return manager;
    }

    public void test() throws Exception {
        int count = 1000;

        Set<Integer> control = new HashSet<Integer>();
        Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < count; i++) {
            control.add(r.nextInt());
        }

        final Set<Integer> a = new HashSet<Integer>();
        final Set<Integer> b = new HashSet<Integer>();
        IEventManager manager = newEventManager();
        IEventListenerRegistration registry = manager.addListener(
            TestEvent.class,
            new IEventListener<TestEvent>() {
                public void handleEvent(TestEvent event) {
                    a.add(event.id);
                }
            });

        for (Integer i : control) {
            TestEvent event = new TestEvent(i);
            manager.fireEvent(event, new IEventListener<TestEvent>() {
                public void handleEvent(TestEvent event) {
                    b.add(event.id);
                }
            });
        }

        assertEquals(control, a);
        assertEquals(control, b);

        a.clear();
        b.clear();
        registry.unregister();

        for (Integer i : control) {
            TestEvent event = new TestEvent(i);
            manager.fireEvent(event, new IEventListener<TestEvent>() {
                public void handleEvent(TestEvent event) {
                    b.add(event.id);
                }
            });
        }
        assertEquals(control, b);
        assertTrue(a.isEmpty());
    }

    public void testAsyncCalls() throws Exception {
        int count = 30;
        final IEventManager eventManager = newEventManager();
        final int[] registeredListenerCounter = { 0 };
        final int[] callbackListenerCounter = { 0 };

        class MyEvent {
            public MyEvent() {
            }
        }
        eventManager.addListener(MyEvent.class, new IEventListener<MyEvent>() {
            public void handleEvent(MyEvent event) {
                registeredListenerCounter[0]++;
            }
        });
        final Random r = new Random(System.currentTimeMillis());
        final CyclicBarrier barrier = new CyclicBarrier(count + 1);
        for (int i = 0; i < count; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        try {
                            Thread.sleep(r.nextInt(500));
                            eventManager.fireEvent(
                                new MyEvent(),
                                new IEventListener<MyEvent>() {
                                    public void handleEvent(MyEvent event) {
                                        callbackListenerCounter[0]++;
                                    }
                                });
                        } finally {
                            barrier.await();
                        }
                    } catch (Exception e) {
                    }
                }
            }.start();
        }
        barrier.await();
        assertEquals(count, registeredListenerCounter[0]);
        assertEquals(count, callbackListenerCounter[0]);
    }

    public void testCallbacks() throws Exception {
        final IEventManager eventManager = newEventManager();
        class A {
        }
        class B extends A {
        }
        abstract class AListener implements IEventListener<A> {
        }
        final int[] xCounter = { 0 };
        eventManager.fireEvent(new B(), new AListener() {
            public void handleEvent(A event) {
                xCounter[0]++;
            }
        });
        assertEquals(1, xCounter[0]);
        eventManager.fireEvent(new B(), new IEventListener<A>() {
            public void handleEvent(A event) {
                xCounter[0]++;
            }
        });
        assertEquals(2, xCounter[0]);
        eventManager.fireEvent(new B(), new IEventListener<B>() {
            public void handleEvent(B event) {
                xCounter[0]++;
            }
        });
        assertEquals(3, xCounter[0]);
    }

    public void testClassHierarchy() throws Exception {
        IEventManager manager = newEventManager();
        final int[] aCounter = { 0 };
        final int[] bCounter = { 0 };
        final int[] cCounter = { 0 };
        manager.addListener(TestEventA.class, new IEventListener<TestEventA>() {
            public void handleEvent(TestEventA event) {
                aCounter[0]++;
            }
        });
        manager.addListener(TestEventB.class, new IEventListener<TestEventB>() {
            public void handleEvent(TestEventB event) {
                bCounter[0]++;
            }
        });
        manager.addListener(TestEventC.class, new IEventListener<TestEventC>() {
            public void handleEvent(TestEventC event) {
                cCounter[0]++;
            }
        });
        assertEquals(0, aCounter[0]);
        assertEquals(0, bCounter[0]);
        assertEquals(0, cCounter[0]);

        manager.fireEvent(new TestEventB());
        assertEquals(1, aCounter[0]);
        assertEquals(1, bCounter[0]);
        assertEquals(0, cCounter[0]);

        manager.fireEvent(new TestEventC());
        assertEquals(2, aCounter[0]);
        assertEquals(1, bCounter[0]);
        assertEquals(1, cCounter[0]);

        manager.fireEvent(new TestEventB());
        assertEquals(3, aCounter[0]);
        assertEquals(2, bCounter[0]);
        assertEquals(1, cCounter[0]);

        manager.fireEvent(new TestEventC());
        assertEquals(4, aCounter[0]);
        assertEquals(2, bCounter[0]);
        assertEquals(2, cCounter[0]);
    }

    public void testEventSuperclassListeners() {
        IEventManager manager = newEventManager();
        final int[] counter = { 0 };
        IEventListener<TestEventA> listener = new IEventListener<TestEventA>() {
            public void handleEvent(TestEventA event) {
                counter[0]++;
            }
        };
        manager.addListener(TestEventB.class, listener);
        manager.addListener(TestEventC.class, listener);
        assertEquals(0, counter[0]);
        manager.fireEvent(new TestEventB());
        assertEquals(1, counter[0]);
        manager.fireEvent(new TestEventC());
        assertEquals(2, counter[0]);
    }

    /**
     * This test is used to check that events fired from listeners do not leads
     * to the deep recursion.
     */
    public void testRecursiveCalls() {
        int max = 100000;

        final IEventManager manager = newEventManager();
        class Evt {

            private int fCounter;

            private int fMax;

            public Evt(int max) {
                fMax = max;
            }

            public int getCounter() {
                return fCounter;
            }

            public boolean inc() {
                fCounter++;
                return fCounter < fMax;
            }
        }
        manager.addListener(Evt.class, new IEventListener<Evt>() {
            public void handleEvent(Evt event) {
                if (event.inc()) {
                    manager.fireEvent(event);
                }
            }
        });
        Evt evt = new Evt(max);
        manager.fireEvent(evt);
        assertEquals(max, evt.getCounter());
    }

}
