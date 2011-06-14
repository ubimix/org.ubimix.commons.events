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
 * The event manager is used to deliver user-defined events to the corresponding
 * registered event listeners. Clients can create their own events and register
 * the corresponding specific listeners. This class was designed to properly
 * handle situations when an event listener fires a new event. It has no
 * external dependencies and it can be used on the client side as well as on the
 * server.
 * 
 * <pre>
 * // Example of usage of this class:
 * 
 * // A new user-defined type of events
 * class MyEvent extends Event {
 *     String fMessage;
 *     public MyEvent(String msg) {
 *         super(MyEvent.class);
 *         fMessage = msg;
 *     }
 *     public String getMessage() { return fMessage; }
 * }
 * 
 * public class Test {
 *     public static void main(String[] args) {
 *         IEventManager manager = new EventManager();
 *         // Registers a new listener which is notified about all MyEvent events
 *         IEventListenerRegistration registration = 
 *              manager.addListener(MyEvent.class, new IEventListener<MyEvent>() {
 *                  public void handleEvent(MyEvent event) {
 *                      System.out.println(event.getMessage());
 *                  }
 *              });
 *         // Now we fire a new event
 *         manager.fireEvent(new MyEvent("Hello, world!"));
 *         ...
 *         // Remove listener
 *         registration.unregister();
 *     }
 * }
 </pre>
 * 
 * <pre>
 * Example2: 
 * This example shows how listeners in the 
 * {@link #fireEvent(Object, IEventListener...)} method could be used.
 * 
 * class MyEvent { 
 *      public final String message;
 *      public MyEvent(String str) { this.message = str; }
 * }
 * 
 * class Foo {
 *      private IEventManager fEventManager;
 *      // Initializes the event manager
 *      public Foo(IEventManager manager) { fEventManager = manager; }
 *      
 *      // Performs asynchronous operations and notify about the end by firing 
 *      // of a new MyEvent. Given listeners can be used to notify callers 
 *      // about the end of the operation. 
 *      public void load(
 *          final String message, 
 *          final IEventListener<MyEvent>... listeners) {
 *          new Thread() {
 *              public void run() {
 *                  try {
 *                      Thread.sleep(10 * 1000);
 *                      fEventManager.fireEvent(new MyEvent(message), listeners);
 *                  } catch (Exception e) { 
 *                      // Do nothing
 *                  }
 *              }
 *          }.start();       
 *      }
 *  }
 *  
 *  // Usage of this class:
 *  
 *  IEventManager manager = new EventManager();
 *  Foo foo = new Foo(manager);
 *  // The given "throwable" listener is used as a simple callback. 
 *  // This call does not register this listener in the internal list of 
 *  // listeners.  
 *  foo.load("Hello, world!", new IEventListener<MyEvent>() {
 *      public void handleEvent(MyEvent event) {
 *          System.out.println(event.message);
 *      }
 *  });
 * </pre>
 * 
 * @author kotelnikov
 */
public interface IEventManager extends IEventListenerRegistry {

    /**
     * Fires a new event. This method uses the type of the given event (see the
     * {@link IEvent#getType()} method) to load the corresponding registered
     * event listeners. All events fired by this method are delivered to
     * listeners in the order of arrival so even if a listener fires new events
     * then they are added to the event queue and they are really fired only
     * when the previous events are delivered to all listeners.
     * 
     * @param event the event to fire
     */
    <E> void fireEvent(E event);

    /**
     * Fires a new event. This method uses the type of the given event (see the
     * {@link IEvent#getType()} method) to load the corresponding registered
     * event listeners. All events fired by this method are delivered to
     * listeners in the order of arrival so even if a listener fires new events
     * then they are added to the event queue and they are really fired only
     * when the previous events are delivered to all listeners.
     * <p>
     * This method takes an optional listener to notify it as well. This
     * listener is called after all registered listeners. This method could be
     * especially useful to perform asynchronous operations as it is shown in
     * the following example:
     * </p>
     * 
     * <pre>
     * class Foo {
     *      private IEventManager fEventManager;
     *      ...
     *      public void load(final String m, final IEventListener<MyEvent> l) {
     *          new Thread() {
     *              public void run() {
     *                  try {
     *                      Thread.sleep(10 * 1000);
     *                      fEventManager.fireEvent(new MyEvent(m), l);
     *                  } catch (Exception e) { }
     *              }
     *          }.start();       
     *      }
     * }
     * // Usage:
     *  
     *  IEventManager manager = new EventManager();
     *  Foo foo = new Foo(manager);
     *  foo.load("Hello, world!", new IEventListener<MyEvent>() {
     *      public void handleEvent(MyEvent event) {
     *          System.out.println(event.message);
     *      }
     *  });
     * </pre>
     * 
     * @param event the event to fire
     * @param listener a call-back listener to notify
     */
    <E, L extends IEventListener<? super E>> void fireEvent(E event, L listener);

}