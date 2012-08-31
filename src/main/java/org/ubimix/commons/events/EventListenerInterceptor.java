/**
 * 
 */
package org.ubimix.commons.events;

import java.util.List;
import java.util.Map;

/**
 * @author kotelnikov
 */
public class EventListenerInterceptor implements IEventListenerInterceptor {

    /**
     * 
     */
    public EventListenerInterceptor() {
    }

    /**
     * @see org.ubimix.commons.events.IEventListenerInterceptor#done()
     */
    public void done() {
    }

    /**
     * @see org.ubimix.commons.events.IEventListenerInterceptor#init(java.util.Map)
     */
    public void init(Map<Class<?>, List<IEventListener<?>>> listeners) {
        for (Map.Entry<Class<?>, List<IEventListener<?>>> entry : listeners
            .entrySet()) {
            Class<?> eventType = entry.getKey();
            List<IEventListener<?>> list = entry.getValue();
            for (IEventListener<?> listener : list) {
                onAddListener(eventType, listener);
            }
        }
    }

    /**
     * @see org.ubimix.commons.events.IEventListenerInterceptor#onAddListener(java.lang.Class,
     *      org.ubimix.commons.events.IEventListener)
     */
    public void onAddListener(Class<?> eventType, IEventListener<?> listener) {
    }

    /**
     * @see org.ubimix.commons.events.IEventListenerInterceptor#onRemoveListener(java.lang.Class,
     *      org.ubimix.commons.events.IEventListener)
     */
    public void onRemoveListener(Class<?> eventType, IEventListener<?> listener) {

    }

}
