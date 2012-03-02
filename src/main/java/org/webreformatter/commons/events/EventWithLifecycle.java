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

import java.util.HashSet;
import java.util.Set;

/**
 * A default implementation of an event with the lifecycle management methods.
 * This class stores in the internal field values of the event manager and a
 * callback (if any) associated with it (see the {@link #getCallback()} and
 * {@link #getEventManager()} methods). Note that these methods are initialized
 * only when the event manager really begins to dispatch this event to
 * registered listeners and NOT when this is initially enqueued by the
 * {@link IEventManager#fireEvent(Object, IEventListener...)} method.
 * 
 * @author kotelnikov
 */
public class EventWithLifecycle implements IEventWithLifecycle {

    /**
     * A list of events associated with this event
     */
    private IEventListener<?> fCallback;

    /**
     * Errors added to this event. Most of the time these errors are fired by
     * registered event listeners.
     */
    private Set<Throwable> fErrors;

    /**
     * The event manager used to initially fire this event.
     */
    private IEventManager fEventManager;

    /**
     * The default constructor.
     */
    public EventWithLifecycle() {
    }

    /**
     * Clears all internal fields (event manager, callbacks, errors).
     */
    public void clear() {
        fErrors = null;
        fEventManager = null;
        fCallback = null;
    }

    /**
     * Returns a callback associated with this event when this event is fired by
     * the manager.
     * 
     * @return an callback associated with this event when this event is fired
     *         by the manager.
     */
    public IEventListener<?> getCallback() {
        return fCallback;
    }

    /**
     * Returns a set of errors fired by listeners of this event while the
     * current handle stage.
     * 
     * @return the errors
     */
    public Set<Throwable> getErrors() {
        return fErrors;
    }

    /**
     * Returns the event manager initially firing this event.
     * 
     * @return the event manager dispatching this event.
     */
    public IEventManager getEventManager() {
        return fEventManager;
    }

    /**
     * Returns <code>true</code> if some errors fired by event listeners where
     * reported.
     * 
     * @return <code>true</code> if some errors fired by event listeners where
     *         reported
     * @see #getErrors()
     */
    public boolean hasErrors() {
        return fErrors != null;
    }

    /**
     * This method is used to add an error or exception to this event.
     * 
     * @param error the error to add
     */
    public void onError(Throwable error) {
        if (fErrors == null) {
            fErrors = new HashSet<Throwable>();
        }
        fErrors.add(error);
    }

    /**
     * @see org.webreformatter.commons.events.IEventWithLifecycle#onFire(org.webreformatter.commons.events.IEventManager,
     *      org.webreformatter.commons.events.IEventListener)
     */
    public void onFire(IEventManager eventManager, IEventListener<?> callback) {
        if (fEventManager == null) {
            fEventManager = eventManager;
            fCallback = callback;
        }
    }

    /**
     * @see org.webreformatter.commons.events.IEventWithLifecycle#onHandleBegin(org.webreformatter.commons.events.IEventManager,
     *      org.webreformatter.commons.events.IEventListener)
     */
    public void onHandleBegin(
        IEventManager eventManager,
        IEventListener<?> callback) {
    }

    /**
     * @see org.webreformatter.commons.events.IEventWithLifecycle#onHandleEnd()
     */
    public void onHandleEnd() {
        if (fErrors != null) {
            reportErrors(fErrors);
        }
    }

    /**
     * @see org.webreformatter.commons.events.IEventWithLifecycle#onHandleError(org.webreformatter.commons.events.IEventListener,
     *      java.lang.Throwable)
     */
    public void onHandleError(IEventListener<?> listener, Throwable error) {
        onError(error);
    }

    /**
     * This method could be overloaded in subclasses to handle/report errors
     * added to this event.
     * 
     * @param errors the errors to report
     */
    protected void reportErrors(Set<Throwable> errors) {
    }

}
