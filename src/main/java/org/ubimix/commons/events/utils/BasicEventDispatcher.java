package org.ubimix.commons.events.utils;

import java.util.LinkedHashSet;
import java.util.Set;

import org.ubimix.commons.events.IEventListener;
import org.ubimix.commons.events.IEventListenerRegistration;

/**
 * @author kotelnikov
 * @param <E>
 */
public class BasicEventDispatcher<E> implements IHasEvents<E> {

    private Set<IEventListener<? super E>> fListeners = new LinkedHashSet<IEventListener<? super E>>();

    /**
     * @see org.ubimix.commons.events.utils.IHasEvents#addListener(IEventListener)
     */
    public IEventListenerRegistration addListener(
        final IEventListener<? super E> listener) {
        fListeners.add(listener);
        return new IEventListenerRegistration() {
            public boolean unregister() {
                return removeListener(listener);
            }
        };
    }

    /**
     * @see org.ubimix.commons.events.utils.IHasEvents#fireEvent(java.lang.Object)
     */
    public void fireEvent(E event) {
        for (IEventListener<? super E> listener : fListeners) {
            listener.handleEvent(event);
        }
    }

    /**
     * @see org.ubimix.commons.events.utils.IHasEvents#fireEvent(java.lang.Object,
     *      org.ubimix.commons.events.IEventListener)
     */
    public void fireEvent(E event, IEventListener<? super E> callback) {
        fireEvent(event);
        callback.handleEvent(event);
    }

    /**
     * @see org.ubimix.commons.events.utils.IHasEvents#removeListener(IEventListener)
     */
    public boolean removeListener(IEventListener<? super E> listener) {
        return fListeners.remove(listener);
    }

}