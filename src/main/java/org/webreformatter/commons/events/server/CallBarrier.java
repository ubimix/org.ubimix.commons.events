/**
 * 
 */
package org.webreformatter.commons.events.server;

import org.webreformatter.commons.events.IEventListener;
import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.calls.CallEvent;

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
 *       CallBarrier handler = new CallBarrier(manager);
 *      final List<String> list = Collections
 *          .synchronizedList(new ArrayList<String>());
 *      CallListener<MyEvent> listener = new CallListener<MyEvent>() {
 *          @Override
 *          protected void handleResponse(MyEvent event) {
 *              System.out.println(event.getResponse());
 *              list.add(event.getRequest());
 *          };
 *      };
 *      handler.fireEvent(new MyEvent("John"), listener);
 *      handler.fireEvent(new MyEvent("Bill"), listener);
 *      handler.fireEvent(new MyEvent("Mike"), listener);
 *      handler.await();
 *      System.out.println("The following people were called: " + list);
 * </pre>
 * 
 * @author kotelnikov
 */
public class CallBarrier implements IEventListener<CallEvent<?, ?>> {

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
                try {
                    if (listener != null) {
                        listener.handleEvent(event);
                    }
                } finally {
                    CallEvent<?, ?> e = (CallEvent<?, ?>) event;
                    CallBarrier.this.handleEvent(e);
                }
            }
        };
    }

    public void await() {
        synchronized (fMutex) {
            while (fRequestCounter > 0) {
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

    public void handleEvent(CallEvent<?, ?> event) {
        if (event != null && event.isResponseStage()) {
            decCounter();
        }
    }

    private void incCounter() {
        synchronized (fMutex) {
            fRequestCounter++;
        }
    }

}
