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
     * This method is called just before the event is dispatched to the
     * listeners.
     * 
     * @param manager the event manager calling this method
     * @param callback a callback associated with this event
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
