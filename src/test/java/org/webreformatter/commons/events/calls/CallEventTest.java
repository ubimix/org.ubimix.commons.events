/**
 * 
 */
package org.webreformatter.commons.events.calls;

import junit.framework.TestCase;

import org.webreformatter.commons.events.EventManager;
import org.webreformatter.commons.events.IEventManager;

/**
 * @author kotelnikov
 */
public class CallEventTest extends TestCase {

    public static class MyEvent extends CallEvent<String, String> {
        public MyEvent(String request) {
            super(request);
        }
    }

    /**
     * @param name
     */
    public CallEventTest(String name) {
        super(name);
    }

    public void test() throws Exception {
        IEventManager manager = new EventManager();

        final int[] listenerCounter = { 0 };
        // This result counter is used to check that even callback listeners are
        // properly called (one time for the request and one time for the
        // response).
        final int[] callbackCounter = { 0 };
        manager.addListener(MyEvent.class, new CallListener<MyEvent>() {

            @Override
            protected void handleRequest(MyEvent event) {
                assertEquals("Hello", event.getRequest());
                assertNull(event.getResponse());
                listenerCounter[0]++;
            }

            @Override
            protected void handleResponse(MyEvent event) {
                assertEquals("Hello", event.getRequest());
                assertEquals("World", event.getResponse());
                listenerCounter[0]++;
            }
        });

        MyEvent event = new MyEvent("Hello");
        manager.fireEvent(event, new CallListener<MyEvent>() {
            @Override
            protected void handleRequest(MyEvent event) {
                assertEquals("Hello", event.getRequest());
                assertNull(event.getResponse());
                callbackCounter[0]++;
            }

            @Override
            protected void handleResponse(MyEvent event) {
                assertEquals("Hello", event.getRequest());
                assertEquals("World", event.getResponse());
                callbackCounter[0]++;
            }
        });
        // Counters should be called only once - for the request.
        assertEquals(1, listenerCounter[0]);
        assertEquals(1, callbackCounter[0]);

        // A little bit later...
        Thread.sleep(200);
        event.reply("World");

        // Now the counter should be incremented the second time - for the
        // response.
        assertEquals(2, listenerCounter[0]);
        assertEquals(2, callbackCounter[0]);

    }
}
