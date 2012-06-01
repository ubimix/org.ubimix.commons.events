package org.webreformatter.commons.events.server;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.webreformatter.commons.events.EventListenerRegistry;
import org.webreformatter.commons.events.EventManager;
import org.webreformatter.commons.events.IEventListener;
import org.webreformatter.commons.events.IEventListenerInterceptor;
import org.webreformatter.commons.events.IEventListenerRegistration;
import org.webreformatter.commons.events.IEventListenerRegistry;
import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.IEventWithLifecycle;

/**
 * This implementation of the {@link IEventManager} interface handles events in
 * separate threads so all operations are performed really asynchronously.
 * 
 * @author kotelnikov
 */
public class AsyncEventManager implements IEventManager {

    private static ThreadLocal<IEventManager> fLocalEventManager = new ThreadLocal<IEventManager>();

    private Executor fExecutor;

    private IEventListenerRegistry fListenerRegistry;

    public AsyncEventManager() {
        this(Executors.newCachedThreadPool(), new EventListenerRegistry());
    }

    /**
     * @param executor
     * @param listenerRegistry
     */
    public AsyncEventManager(
        Executor executor,
        IEventListenerRegistry listenerRegistry) {
        setExecutor(executor);
        setListenerRegistry(listenerRegistry);
    }

    /**
     * @see org.webreformatter.commons.events.IEventListenerRegistry#addListener(java.lang.Class,
     *      org.webreformatter.commons.events.IEventListener)
     */
    public <E> IEventListenerRegistration addListener(
        Class<E> eventType,
        IEventListener<? super E> listener) {
        return fListenerRegistry.addListener(eventType, listener);
    }

    /**
     * @see org.webreformatter.commons.events.IEventListenerRegistry#addListenerInterceptor(org.webreformatter.commons.events.IEventListenerInterceptor)
     */
    public void addListenerInterceptor(IEventListenerInterceptor interceptor) {
        fListenerRegistry.addListenerInterceptor(interceptor);
    }

    /**
     * Closes this event manager and shuts down the associated
     */
    public void close() {
        if (fExecutor instanceof ExecutorService) {
            ((ExecutorService) fExecutor).shutdown();
        }
    }

    /**
     * @see org.webreformatter.commons.events.IEventManager#fireEvent(java.lang.Object)
     */
    public <E> void fireEvent(final E event) {
        if (event instanceof IEventWithLifecycle) {
            ((IEventWithLifecycle) event).onFire(this, null);
        }
        fExecutor.execute(new Runnable() {
            public void run() {
                IEventManager manager = getLocalEventManager(true);
                manager.fireEvent(event);
            }
        });
    }

    /**
     * @see org.webreformatter.commons.events.IEventManager#fireEvent(java.lang.Object,
     *      org.webreformatter.commons.events.IEventListener)
     */
    public <E, L extends IEventListener<? super E>> void fireEvent(
        final E event,
        final L listener) {
        if (event instanceof IEventWithLifecycle) {
            ((IEventWithLifecycle) event).onFire(this, listener);
        }
        fExecutor.execute(new Runnable() {
            public void run() {
                IEventManager manager = getLocalEventManager(true);
                manager.fireEvent(event, listener);
            }
        });
    }

    /**
     * Returns the event listener regsitry used by all thread-specific event
     * managers.
     * 
     * @return the event listener regsitry used by all thread-specific event
     *         managers
     */
    public IEventListenerRegistry getListenerRegistry() {
        return fListenerRegistry;
    }

    /**
     * @see org.webreformatter.commons.events.IEventListenerRegistry#getListeners(java.lang.Class)
     */
    public <E> List<IEventListener<?>> getListeners(Class<E> eventType) {
        return fListenerRegistry.getListeners(eventType);
    }

    /**
     * Returns an event manager associated with the current thread. If there is
     * no such a manager and the given parameter <code>create</code> is
     * <code>true</code> then this method creates a new thread-specific event
     * manager and returns it.
     * 
     * @param create if this flag is <code>true</code> and there is no event
     *        managers associated with the current thread then this method will
     *        create a new event manager
     * @return a thread-specific event manager
     */
    protected IEventManager getLocalEventManager(boolean create) {
        IEventManager eventManager = fLocalEventManager.get();
        if (eventManager == null && create) {
            eventManager = newEventManager();
            fLocalEventManager.set(eventManager);
        }
        return eventManager;
    }

    /**
     * Creates and returns a new thread-local event manager.
     * 
     * @return a newly created thread-local event manager
     */
    protected EventManager newEventManager() {
        EventManager localEventManager = new EventManager(fListenerRegistry);
        return localEventManager;
    }

    /**
     * @see org.webreformatter.commons.events.IEventListenerRegistry#removeListener(java.lang.Class,
     *      org.webreformatter.commons.events.IEventListener)
     */
    public <E> boolean removeListener(
        Class<E> eventType,
        IEventListener<? super E> listener) {
        return fListenerRegistry.removeListener(eventType, listener);
    }

    /**
     * @see org.webreformatter.commons.events.IEventListenerRegistry#removeListenerInterceptor(org.webreformatter.commons.events.IEventListenerInterceptor)
     */
    public void removeListenerInterceptor(IEventListenerInterceptor interceptor) {
        fListenerRegistry.removeListenerInterceptor(interceptor);
    }

    /**
     * Sets a new executor
     * 
     * @param executor the executor to set
     */
    public void setExecutor(Executor executor) {
        fExecutor = executor;
    }

    /**
     * Sets a new listener registry used by all thread-local event managers
     * 
     * @param listenerRegistry the registry to set
     */
    public void setListenerRegistry(IEventListenerRegistry listenerRegistry) {
        fListenerRegistry = listenerRegistry;
    }

}