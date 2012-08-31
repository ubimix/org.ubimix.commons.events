/**
 * 
 */
package org.ubimix.commons.events.utils;

import org.ubimix.commons.events.IEventListener;
import org.ubimix.commons.events.IEventListenerRegistration;

/**
 * This type is used to provide access to event containers of a specific type.
 * 
 * @param <E> the type of events
 * @author kotelnikov
 */
public interface IHasEvents<E> {

    /**
     * Adds a new listener to the internal list of listeners
     * 
     * @param listener the listener to add
     * @return the listener registration object used to unregister the given
     *         listener
     */
    IEventListenerRegistration addListener(IEventListener<? super E> listener);

    /**
     * Fires the specified event and notifies all registered listeners about it.
     * This method do the same thing as the
     * {@link #fireEvent(Object, IEventListener)} method with an empty callback
     * parameter.
     * 
     * @param <L> the type of the listener to add
     * @param event
     */
    void fireEvent(E event);

    /**
     * Fires the given event and notifies all registered listeners about it. The
     * specified callback object is used to make know that the event is already
     * delivered to all registered listeners.
     * 
     * @param event the event to fire
     * @param callback the callback parameter
     */
    void fireEvent(E event, IEventListener<? super E> callback);

    /**
     * Removes the specified listener from the list of listeners.
     * 
     * @param listener the listener to remove
     * @return <code>true</code> if the listener was successfully removed
     */
    boolean removeListener(IEventListener<? super E> listener);
}
