/* ************************************************************************** *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 * 
 * This file is licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ************************************************************************** */
package org.ubimix.commons.events;

import java.util.LinkedList;
import java.util.List;

/**
 * This implementation of the {@link IEventManager} interface guarantees that
 * all events are delivered to the registered listeners in the order. It means
 * that the second fired event will be delivered to listeners only when all
 * listeners for the first event are notified. The internal implementation
 * guarantees that there is no recursive calls in the event dispatching - using
 * this implementation it is possible re-fire the same event in the event
 * listener without {@link StackOverflowError} exceptions.
 * 
 * @author kotelnikov
 */
public class EventManager implements IEventManager {

    protected static class EventNode {
        private IEventListener<?> fCallback;

        private Object fEvent;

        public EventNode(Object event, IEventListener<?> callback) {
            fEvent = event;
            fCallback = callback;
        }

        public IEventListener<?> getCallback() {
            return fCallback;
        }

        public Object getEvent() {
            return fEvent;
        }
    }

    private int fDepth;

    private LinkedList<EventNode> fEventNodes = new LinkedList<EventNode>();

    private IEventListenerRegistry fListenerRegistry;

    public EventManager() {
        this(new EventListenerRegistry());
    }

    public EventManager(IEventListenerRegistry listenerRegistry) {
        fListenerRegistry = listenerRegistry;
    }

    /**
     * @see org.ubimix.commons.events.IEventListenerRegistry#addListener(java.lang.Class,
     *      org.ubimix.commons.events.IEventListener)
     */
    public <E> IEventListenerRegistration addListener(
        Class<E> eventType,
        IEventListener<? super E> listener) {
        return fListenerRegistry.addListener(eventType, listener);
    }

    /**
     * @see org.ubimix.commons.events.IEventListenerRegistry#addListenerInterceptor(org.ubimix.commons.events.IEventListenerInterceptor)
     */
    public void addListenerInterceptor(IEventListenerInterceptor interceptor) {
        fListenerRegistry.addListenerInterceptor(interceptor);
    }

    protected EventNode dequeueEvent() {
        synchronized (fEventNodes) {
            EventNode node = null;
            if (!fEventNodes.isEmpty()) {
                node = fEventNodes.remove(0);
            }
            return node;
        }
    }

    @SuppressWarnings("unchecked")
    protected void dispatchEvent(EventNode node) {
        Object event = node.getEvent();
        IEventListener<?> callback = node.getCallback();
        onBegin(event, callback);
        try {
            Class<? extends Object> type = event.getClass();
            while (type != null) {
                List<IEventListener<?>> list = fListenerRegistry
                    .getListeners(type);
                if (list != null) {
                    for (IEventListener<?> listener : list) {
                        try {
                            IEventListener<Object> l = (IEventListener<Object>) listener;
                            l.handleEvent(event);
                        } catch (Throwable t) {
                            onError(event, callback, listener, t);
                        }
                    }
                }
                type = type.getSuperclass();
            }
            if (callback != null) {
                try {
                    IEventListener<Object> l = (IEventListener<Object>) callback;
                    l.handleEvent(event);
                } catch (Throwable t) {
                    onError(event, callback, callback, t);
                }
            }
        } finally {
            onEnd(event, callback);
        }
    }

    protected void enqueueEvent(EventNode node) {
        synchronized (fEventNodes) {
            fEventNodes.add(node);
        }
    }

    /**
     * @see org.ubimix.commons.events.IEventManager#fireEvent(java.lang.Object)
     */
    public <E> void fireEvent(E event) {
        IEventListener<E> callback = null;
        fireEvent(event, callback);
    }

    /**
     * @see org.ubimix.commons.events.IEventManager#fireEvent(java.lang.Object,
     *      org.ubimix.commons.events.IEventListener)
     */
    public <E, L extends IEventListener<? super E>> void fireEvent(
        E event,
        L callback) {
        if (event instanceof IEventWithLifecycle) {
            ((IEventWithLifecycle) event).onFire(this, callback);
        }
        EventNode node = newEventNode(event, callback);
        enqueueEvent(node);
        if (fDepth == 0) {
            fDepth++;
            try {
                while ((node = dequeueEvent()) != null) {
                    dispatchEvent(node);
                }
            } finally {
                fDepth--;
            }
        }
    }

    /**
     * @see org.ubimix.commons.events.IEventListenerRegistry#getListeners(java.lang.Class)
     */
    public <E> List<IEventListener<?>> getListeners(Class<E> eventType) {
        return fListenerRegistry.getListeners(eventType);
    }

    protected <E, L extends IEventListener<? super E>> EventNode newEventNode(
        E event,
        L callback) {
        return new EventNode(event, callback);
    }

    protected void onBegin(Object event, IEventListener<?> callback) {
        if (event instanceof IEventWithLifecycle) {
            IEventWithLifecycle e = (IEventWithLifecycle) event;
            e.onHandleBegin(this, callback);
        }
    }

    protected void onEnd(Object event, IEventListener<?> callback) {
        if (event instanceof IEventWithLifecycle) {
            IEventWithLifecycle e = (IEventWithLifecycle) event;
            e.onHandleEnd();
        }
    }

    /**
     * @param event the event
     * @param callback the callback object
     * @param listener the listener resing the exception
     * @param error the error rised by the listener
     */
    protected void onError(
        Object event,
        IEventListener<?> callback,
        IEventListener<?> listener,
        Throwable error) {
        if (event instanceof IEventWithLifecycle) {
            IEventWithLifecycle e = (IEventWithLifecycle) event;
            e.onHandleError(listener, error);
        }
    }

    /**
     * @see org.ubimix.commons.events.IEventListenerRegistry#removeListener(java.lang.Class,
     *      org.ubimix.commons.events.IEventListener)
     */
    public synchronized <E> boolean removeListener(
        Class<E> eventType,
        IEventListener<? super E> listener) {
        return fListenerRegistry.removeListener(eventType, listener);
    }

    /**
     * @see org.ubimix.commons.events.IEventListenerRegistry#removeListenerInterceptor(org.ubimix.commons.events.IEventListenerInterceptor)
     */
    public void removeListenerInterceptor(IEventListenerInterceptor interceptor) {
        fListenerRegistry.removeListenerInterceptor(interceptor);
    }

}