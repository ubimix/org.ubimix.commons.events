/**
 * 
 */
package org.webreformatter.commons.events.utils;

import org.webreformatter.commons.events.EventManager;
import org.webreformatter.commons.events.IEventListener;
import org.webreformatter.commons.events.IEventListenerRegistration;
import org.webreformatter.commons.events.IEventManager;

/**
 * The default implementation of the {@link IHasEvents} interface.
 * 
 * @author kotelnikov
 * @param <E> the type of events.
 */
public class HasEvents<E> implements IHasEvents<E> {

    private IEventManager fEventManager;

    private Class<E> fType;

    /**
     * @param type
     */
    public HasEvents(Class<E> type) {
        this(type, null);
    }

    /**
     * @param type
     * @param eventManager
     */
    public HasEvents(Class<E> type, IEventManager eventManager) {
        if (type == null || type.isInterface() || type.isArray()) {
            throw new IllegalArgumentException(
                "The specified type should be a class. Type: " + type + ".");
        }
        fType = type;
        if (eventManager == null) {
            eventManager = newEventManager();
        }
        fEventManager = eventManager;
    }

    /**
     * @see org.webreformatter.commons.events.utils.IHasEvents#addListener(org.webreformatter.commons.events.IEventListener)
     */
    public IEventListenerRegistration addListener(
        IEventListener<? super E> listener) {
        return fEventManager.addListener(fType, listener);
    }

    /**
     * @see org.webreformatter.commons.events.utils.IHasEvents#fireEvent(java.lang.Object)
     */
    public void fireEvent(E event) {
        fEventManager.fireEvent(event);
    }

    /**
     * @see org.webreformatter.commons.events.utils.IHasEvents#fireEvent(java.lang.Object,
     *      org.webreformatter.commons.events.IEventListener)
     */
    public void fireEvent(E event, IEventListener<? super E> callback) {
        fEventManager.fireEvent(event, callback);
    }

    /**
     * Returns a newly created event manager corresponding to this type.
     * 
     * @return a newly created event manager corresponding to this type.
     */
    protected IEventManager newEventManager() {
        return new EventManager();
    }

    /**
     * @see org.webreformatter.commons.events.utils.IHasEvents#removeListener(org.webreformatter.commons.events.IEventListener)
     */
    public boolean removeListener(IEventListener<? super E> listener) {
        return fEventManager.removeListener(fType, listener);
    }

}
