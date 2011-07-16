/**
 * 
 */
package org.webreformatter.commons.events;

import junit.framework.TestCase;

import org.webreformatter.commons.events.EventListenerRegistry.IEventListenerInterceptor;

/**
 * @author kotelnikov
 */
public class EventListenerInterceptorTest extends TestCase {

    /**
     * @param name
     */
    public EventListenerInterceptorTest(String name) {
        super(name);
    }

    public void test() throws Exception {
        class MyEvent {
        }

        final IEventListener<?>[] result = { null };
        EventListenerRegistry registry = new EventListenerRegistry();
        IEventManager manager = new EventManager(registry);
        registry.addListenerInterceptor(new IEventListenerInterceptor() {
            public void onAddListener(
                Class<?> eventType,
                IEventListener<?> listener) {
                result[0] = listener;
            }

            public void onRemoveListener(
                Class<?> eventType,
                IEventListener<?> listener) {
                result[0] = null;
            }
        });
        assertNull(result[0]);

        IEventListener<MyEvent> listener = new IEventListener<MyEvent>() {
            public void handleEvent(MyEvent event) {
            }
        };
        manager.addListener(MyEvent.class, listener);
        assertSame(listener, result[0]);
        manager.removeListener(MyEvent.class, listener);
        assertNull(result[0]);

        IEventListenerRegistration r = manager.addListener(
            MyEvent.class,
            listener);
        assertSame(listener, result[0]);
        r.unregister();
        assertNull(result[0]);

    }

}
