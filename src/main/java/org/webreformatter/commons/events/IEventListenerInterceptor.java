package org.webreformatter.commons.events;

/**
 * Instances of this type are notified when listeners are added to or removed
 * from the listener registry.
 * <p>
 * Interceptors are very useful for automatic creation of registries of
 * available commands/operations/etc.
 * </p>
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