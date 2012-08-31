/**
 * 
 */
package org.ubimix.commons.events;

import junit.framework.TestCase;

/**
 * @author kotelnikov
 */
public class EventObservationTest extends TestCase {

    private static class TestLifecycleEvent implements IEventWithLifecycle {
        private boolean fBeginHandled;

        private boolean fEndHandled;

        private int fErrorCounter;

        private void check(int errorCount) {
            assertTrue(fBeginHandled);
            assertTrue(fEndHandled);
            assertEquals(errorCount, fErrorCounter);
        }

        public void onFire(
            IEventManager eventManager,
            IEventListener<?> callback) {
        }

        public void onHandleBegin(
            IEventManager eventManager,
            IEventListener<?> callback) {
            fBeginHandled = true;
        }

        public void onHandleEnd() {
            fEndHandled = true;
        }

        public void onHandleError(IEventListener<?> listener, Throwable error) {
            fErrorCounter++;
        }

        public void reset() {
            fBeginHandled = false;
            fEndHandled = false;
            fErrorCounter = 0;
        }

    }

    /**
     * @param name
     */
    public EventObservationTest(String name) {
        super(name);
    }

    public void testLifecycle() {
        IEventManager manager = new EventManager();

        TestLifecycleEvent event = new TestLifecycleEvent();
        manager.fireEvent(event);
        event.check(0);

        event.reset();
        int errorListeners = 5;
        for (int i = 0; i < errorListeners; i++) {
            manager.fireEvent(event, new IEventListener<TestLifecycleEvent>() {
                public void handleEvent(TestLifecycleEvent event) {
                    throw new RuntimeException();
                }
            });
        }
        event.check(errorListeners);

        event.reset();
        for (int i = 0; i < errorListeners; i++) {
            manager.addListener(
                TestLifecycleEvent.class,
                new IEventListener<TestLifecycleEvent>() {
                    public void handleEvent(TestLifecycleEvent event) {
                        throw new RuntimeException();
                    }
                });
        }
        manager.fireEvent(event);
        event.check(errorListeners);
    }

}
