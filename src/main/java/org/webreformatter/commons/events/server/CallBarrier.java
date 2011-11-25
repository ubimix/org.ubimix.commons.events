/**
 * 
 */
package org.webreformatter.commons.events.server;

import org.webreformatter.commons.events.IEventListener;
import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.calls.CallEvent;
import org.webreformatter.commons.events.calls.CallListener;

/**
 * This handler is used to wait results of multiple asynchronous calls.
 * <p>
 * Example:
 * </p>
 * 
 * <pre>
 *      class MyEvent extends CallEvent<String, String> {
 *          public MyEvent(String request) {
 *              super(request);
 *          }
 *      }
 *      IEventManager manager = new AsyncEventManager();
 *      // IEventManager manager = new EventManager();
 *      manager.addListener(MyEvent.class, new CallListener<MyEvent>() {
 *          @Override
 *          protected void handleRequest(MyEvent event) {
 *              event.setResponse("Hello, " + event.getRequest() + "!");
 *          }
 *      });
 *      final List<String> list = Collections
 *          .synchronizedList(new ArrayList<String>());
 *      CallListener<MyEvent> listener = new CallListener<MyEvent>() {
 *          @Override
 *          protected void handleResponse(MyEvent event) {
 *              System.out.println(event.getResponse());
 *              list.add(event.getRequest());
 *          };
 *      };
 *      CallBarrier handler = new CallBarrier(manager);
 *      handler.fireEvent(manager, new MyEvent("John"), listener);
 *      handler.fireEvent(manager, new MyEvent("Bill"), listener);
 *      handler.fireEvent(manager, new MyEvent("Mike"), listener);
 *      handler.await();
 *      System.out.println("The following people were called: " + list);
 * </pre>
 * 
 * @author kotelnikov
 */
public class CallBarrier {

    /**
     * This method fires the given call event, waits for the response and
     * returns results of the call.
     * 
     * @param manager an {@link IEventManager} instance used to fire the call
     * @param e a call event to fire
     * @return the result of the execution of the specified event
     */
    @SuppressWarnings("unchecked")
    public static <A, E extends CallEvent<?, A>> A syncCall(
        IEventManager manager,
        E e) {
        CallBarrier barrier = new CallBarrier();
        final Object[] result = { null };
        manager.fireEvent(e, barrier.add(new CallListener<E>() {
            @Override
            protected void handleResponse(E event) {
                result[0] = event.getResponse();
            }
        }));
        barrier.await();
        return (A) result[0];
    }

    private Object fMutex = new Object();

    private int fRequestCounter;

    /**
     * 
     */
    public CallBarrier() {
    }

    public <E, L extends IEventListener<? super E>> IEventListener<E> add() {
        // This variable is here to make compilers happy.
        IEventListener<? super E> listener = null;
        return add(listener);
    }

    public <E, L extends IEventListener<? super E>> IEventListener<E> add(
        final L listener) {
        incCounter();
        return new IEventListener<E>() {
            public void handleEvent(E event) {
                CallEvent<?, ?> e = (CallEvent<?, ?>) event;
                boolean response = e.isResponseStage();
                try {
                    if (listener != null) {
                        listener.handleEvent(event);
                    }
                } finally {
                    if (response) {
                        decCounter();
                    }
                }
            }
        };
    }

    public void await() {
        while (true) {
            synchronized (fMutex) {
                if (fRequestCounter == 0) {
                    break;
                }
                try {
                    fMutex.wait(getWaitTimeout());
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private void decCounter() {
        synchronized (fMutex) {
            fRequestCounter--;
            if (fRequestCounter <= 0) {
                fMutex.notifyAll();
            }
        }
    }

    public <E extends CallEvent<?, ?>> void fireEvent(
        IEventManager manager,
        E event) {
        // This variable is here to make compilers happy.
        IEventListener<? super E> listener = null;
        manager.fireEvent(event, add(listener));
    }

    public <E, L extends IEventListener<? super E>> void fireEvent(
        IEventManager manager,
        E event,
        final L listener) {
        manager.fireEvent(event, add(listener));
    }

    protected long getWaitTimeout() {
        return 100;
    }

    private void incCounter() {
        synchronized (fMutex) {
            fRequestCounter++;
        }
    }

}
