/**
 * 
 */
package org.webreformatter.commons.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kotelnikov
 */
public class EventListenerRegistry implements IEventListenerRegistry {

    private Map<Class<?>, List<IEventListener<?>>> fMap = new HashMap<Class<?>, List<IEventListener<?>>>();

    /**
     * 
     */
    public EventListenerRegistry() {
    }

    /**
     * @see org.webreformatter.commons.events.IEventListenerRegistry#addListener(java.lang.Class,
     *      org.webreformatter.commons.events.IEventListener)
     */
    public synchronized <E> IEventListenerRegistration addListener(
        final Class<E> eventType,
        final IEventListener<? super E> listener) {
        List<IEventListener<?>> list = fMap.get(eventType);
        if (list != null) {
            list = new ArrayList<IEventListener<?>>(list);
        } else {
            list = new ArrayList<IEventListener<?>>();
        }
        if (list.add(listener)) {
            fMap.put(eventType, list);
        }
        return new IEventListenerRegistration() {
            public boolean unregister() {
                return removeListener(eventType, listener);
            }
        };
    }

    /**
     * @see org.webreformatter.commons.events.IEventListenerRegistry#getListeners(java.lang.Class)
     */
    public synchronized <E> List<IEventListener<?>> getListeners(
        Class<E> eventType) {
        List<IEventListener<?>> listeners = fMap.get(eventType);
        return listeners;
    }

    /**
     * @see org.webreformatter.commons.events.IEventListenerRegistry#removeListener(java.lang.Class,
     *      org.webreformatter.commons.events.IEventListener)
     */
    public synchronized <E> boolean removeListener(
        Class<E> eventType,
        IEventListener<? super E> listener) {
        List<IEventListener<?>> list = fMap.get(eventType);
        boolean result = false;
        if (list != null) {
            list = new ArrayList<IEventListener<?>>(list);
            if (list.remove(listener)) {
                result = true;
                if (list.isEmpty()) {
                    fMap.remove(eventType);
                } else {
                    fMap.put(eventType, list);
                }
            }
        }
        return result;
    }

}
