/**
 * 
 */
package org.ubimix.commons.events.server;

import org.ubimix.commons.events.IEventListener;
import org.ubimix.commons.events.IEventManager;
import org.ubimix.commons.events.calls.CallEvent;
import org.ubimix.commons.events.calls.CallListener;

/**
 * This handler is used to wait results of multiple asynchronous calls.
 * <p>
 * Example:
 * </p>
 * 
 * <pre>
 * class MyEvent extends CallEvent&lt;String, String&gt; {
 *     public MyEvent(String request) {
 *         super(request);
 *     }
 * }
 * IEventManager manager = new AsyncEventManager();
 * // IEventManager manager = new EventManager();
 * manager.addListener(MyEvent.class, new CallListener&lt;MyEvent&gt;() {
 *     &#064;Override
 *     protected void handleRequest(MyEvent event) {
 *         event.setResponse(&quot;Hello, &quot; + event.getRequest() + &quot;!&quot;);
 *     }
 * });
 * final List&lt;String&gt; list = Collections.synchronizedList(new ArrayList&lt;String&gt;());
 * CallListener&lt;MyEvent&gt; listener = new CallListener&lt;MyEvent&gt;() {
 *     &#064;Override
 *     protected void handleResponse(MyEvent event) {
 *         System.out.println(event.getResponse());
 *         list.add(event.getRequest());
 *     };
 * };
 * CallBarrier handler = new CallBarrier(manager);
 * handler.fireEvent(manager, new MyEvent(&quot;John&quot;), listener);
 * handler.fireEvent(manager, new MyEvent(&quot;Bill&quot;), listener);
 * handler.fireEvent(manager, new MyEvent(&quot;Mike&quot;), listener);
 * handler.await();
 * System.out.println(&quot;The following people were called: &quot; + list);
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

    private final Object fMutex = new Object();

    private volatile int fRequestCounter;

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
        lock();
        return new IEventListener<E>() {
            @Override
            public void handleEvent(E event) {
                CallEvent<?, ?> e = (CallEvent<?, ?>) event;
                boolean response = e.isResponseStage();
                try {
                    if (listener != null) {
                        listener.handleEvent(event);
                    }
                } finally {
                    if (response) {
                        unlock();
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

    public void lock() {
        synchronized (fMutex) {
            if (fRequestCounter == 0) {
                onLock();
            }
            fRequestCounter++;
        }
    }

    /**
     * This method is called to notify that this barrier was locked. This method
     * could be overloaded in subclasses to define actions associated with the
     * barrier locking.
     */
    protected void onLock() {
    }

    /**
     * This method is called to notify that the barrier was unlocked. It can be
     * overloaded in subclasses to define additional action associated with the
     * barrier unlocking.
     */
    protected void onUnlock() {
    }

    public void unlock() {
        synchronized (fMutex) {
            fRequestCounter--;
            if (fRequestCounter <= 0) {
                onUnlock();
                fMutex.notifyAll();
            }
        }
    }

}
