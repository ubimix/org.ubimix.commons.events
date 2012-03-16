/**
 * 
 */
package org.webreformatter.commons.events;

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
     * @see org.webreformatter.commons.events.IEventListenerInterceptor#done()
     */
    public void done() {
    }

    /**
     * @see org.webreformatter.commons.events.IEventListenerInterceptor#init(java.util.Map)
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
     * @see org.webreformatter.commons.events.IEventListenerInterceptor#onAddListener(java.lang.Class,
     *      org.webreformatter.commons.events.IEventListener)
     */
    public void onAddListener(Class<?> eventType, IEventListener<?> listener) {
    }

    /**
     * @see org.webreformatter.commons.events.IEventListenerInterceptor#onRemoveListener(java.lang.Class,
     *      org.webreformatter.commons.events.IEventListener)
     */
    public void onRemoveListener(Class<?> eventType, IEventListener<?> listener) {

    }

}
