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
package org.ubimix.commons.events.calls;

import org.ubimix.commons.events.IEventListener;

/**
 * @author kotelnikov
 */
public abstract class CallListener<E extends CallEvent<?, ?>>
    implements
    IEventListener<E> {

    /**
     * 
     */
    public CallListener() {
    }

    /**
     * @see org.ubimix.commons.events.IEventListener#handleEvent(java.lang.Object)
     */
    public final void handleEvent(E event) {
        if (event.isRequestStage()) {
            handleRequest(event);
        } else if (event.isResponseStage()) {
            handleResponse(event);
        }
    }

    protected void handleRequest(E event) {
    }

    protected void handleResponse(E event) {
    }

}