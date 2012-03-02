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
package org.webreformatter.commons.events;

/**
 * Events implementing this interface are notified about individual stages of
 * the event handling process. This type can be considered as a
 * "listener for the listeners" - it is notified just before and after an event
 * is delivered to listeners. It is also notified about errors rised by
 * listeners.
 * 
 * @author kotelnikov
 */
public interface IEventWithLifecycle {

    /**
     * This method is called when the event is fired by an event manager. This
     * method is called every time when an event manager accepts the event by
     * the {@link IEventManager#fireEvent(Object)} or
     * {@link IEventManager#fireEvent(Object, IEventListener)} methods. So this
     * method can be called multiple times, especially if the event is initially
     * fired by a composite or by an asynchronous event manager.
     * 
     * @param manager the event manager used to fire the event.
     * @param callback a callback associated with this event
     * @see #onHandleBegin(IEventManager, IEventListener)
     */
    void onFire(IEventManager eventManager, IEventListener<?> callback);

    /**
     * This method is called just before the event is dispatched to registered
     * listeners. The event manager in this method is the manager responsible
     * for dispatching the event to the listeners and it can be different with
     * the initial listener firing the event.
     * 
     * @param manager the event manager calling this method
     * @param callback a callback associated with this event
     * @see #onFire(IEventManager, IEventListener)
     */
    void onHandleBegin(IEventManager eventManager, IEventListener<?> callback);

    /**
     * This method is called when the given event successfully dispatched to all
     * registered listeners.
     */
    void onHandleEnd();

    /**
     * This method is called to notify about an error occurred in an event
     * listener.
     * 
     * @param listener the event listener generated the error
     * @param error the exception created by the listener
     */
    void onHandleError(IEventListener<?> listener, Throwable error);

}
