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
 * Instances of this type are returned by the
 * {@link IEventManager#addListener(Class, IEventListener)} method and are
 * used to remove registered listeners.
 * 
 * @author kotelnikov
 */
public interface IEventListenerRegistration {
    /**
     * This method is used to unregister a registered listener. It returns
     * <code>true</code> if a registered listener was successfully removed.
     * This method do the same operation as the
     * {@link IEventManager#removeListener(Class, IEventListener)} method.
     * 
     * @return <code>true</code> if a registered listener was successfully
     *         removed.
     */
    boolean unregister();
}