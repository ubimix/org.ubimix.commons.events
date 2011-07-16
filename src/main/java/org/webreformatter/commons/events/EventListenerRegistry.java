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

    /**
     * Instances of this type are used to notify when listeners are added to or
     * removed from the listener registry.
     * 
     * @author kotelnikov
     */
    public interface IEventListenerInterceptor {

        /**
         * This method is called when a new listener is added to the registry
         * 
         * @param eventType the type of the event
         * @param listener the added listener
         */
        void onAddListener(Class<?> eventType, IEventListener<?> listener);

        /**
         * This method is called when a listener removed from the registry.
         * 
         * @param eventType the type of the event
         * @param listener the removed listener
         */
        void onRemoveListener(Class<?> eventType, IEventListener<?> listener);

    }

    private List<IEventListenerInterceptor> fInterceptors;

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
            if (fInterceptors != null) {
                for (IEventListenerInterceptor interceptor : fInterceptors) {
                    interceptor.onAddListener(eventType, listener);
                }
            }
        }
        return new IEventListenerRegistration() {
            public boolean unregister() {
                return removeListener(eventType, listener);
            }
        };
    }

    /**
     * @param interceptor
     */
    public synchronized void addListenerInterceptor(
        IEventListenerInterceptor interceptor) {
        if (fInterceptors == null) {
            fInterceptors = new ArrayList<IEventListenerInterceptor>();
        }
        fInterceptors.add(interceptor);
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
                if (fInterceptors != null) {
                    for (IEventListenerInterceptor interceptor : fInterceptors) {
                        interceptor.onRemoveListener(eventType, listener);
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param interceptor
     */
    public synchronized void removeListenerInterceptor(
        IEventListenerInterceptor interceptor) {
        if (fInterceptors != null) {
            fInterceptors.remove(interceptor);
        }
    }

}
