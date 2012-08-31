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

/**
 * Listeners are used to notify about the corresponding fired events.
 * 
 * @author kotelnikov
 * @param <E> the type of the event for this type of listener
 */
public interface IEventListener<E> {

    /**
     * This method is called when an event is occurred
     * 
     * @param event the fired event
     */
    void handleEvent(E event);

}