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

import java.util.List;

/**
 * Objects of this type are used to register/remove listeners.
 * 
 * @author kotelnikov
 */
public interface IEventListenerRegistry {

    /**
     * Adds a new listener to the internal list of listeners
     * 
     * @param <E> the type of events for which the listener is added
     * @param eventType the type of events for which the listener should be
     *        added
     * @param listener the listener to add
     * @return the listener registration object used to unregister the given
     *         listener
     */
    <E> IEventListenerRegistration addListener(
        Class<E> eventType,
        IEventListener<? super E> listener);

    /**
     * This method adds "interceptors" which are notified every time when a new
     * listener is added to or removed from this registry. Interceptors could be
     * considered as "listeners of listeners".
     * 
     * @param interceptor the interceptor to add
     * @see #removeListenerInterceptor(IEventListenerInterceptor)
     */
    void addListenerInterceptor(IEventListenerInterceptor interceptor);

    /**
     * Returns a list of listeners for the specified event type; the returned
     * value can be <code>null</code>.
     * 
     * @param eventType the type of the event for which
     * @return a list of listeners for the specified event type; the returned
     *         value can be <code>null</code>.
     */
    <E> List<IEventListener<?>> getListeners(Class<E> eventType);

    /**
     * Removes the specified listener from the list of listeners.
     * 
     * @param <E> the type of the events for which the listener should be
     *        removed
     * @param eventType the type of events for which the listener should be
     *        removed
     * @param listener the listener to remove
     * @return <code>true</code> if the listener was successfully removed
     */
    <E> boolean removeListener(
        Class<E> eventType,
        IEventListener<? super E> listener);

    /**
     * This method removes "interceptors" from this registry.
     * 
     * @param interceptor the interceptor to remove
     * @see #addListenerInterceptor(IEventListenerInterceptor)
     */
    void removeListenerInterceptor(IEventListenerInterceptor interceptor);

}