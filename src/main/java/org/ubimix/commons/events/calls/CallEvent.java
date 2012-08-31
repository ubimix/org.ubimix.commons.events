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

import org.ubimix.commons.events.EventWithLifecycle;
import org.ubimix.commons.events.IEventListener;
import org.ubimix.commons.events.IEventManager;

/**
 * This is an abstract super-class for all request/response events. The same
 * events is fired twice - the first time for the request and the second time -
 * for the response. The response call is fired automatically when the
 * {@link #reply(Object)} method is called. This class guarantees that all
 * registered listeners are notified for the request stage (
 * {@link CallEvent.STAGE#REQUEST}) and only after that for the response stage (
 * {@link CallEvent.STAGE#RESPONSE}). <br />
 * Example of usage:
 * 
 * <pre>
 *  public static void main(String[] args) throws Exception {
 *      class MyEvent extends CallEvent<String, String> {
 *          public MyEvent(String request) {
 *              super(request);
 *          }
 *      }
 *      // Important: We have to configure the EventManager instance
 *      // with the EventManagerWithLifecycle!  
 *      IEventManager manager = new EventManager(new EventManagerWithLifecycle());
 *      manager.addListener(MyEvent.class, new CallListener<MyEvent>() {
 *          @Override
 *          public void handleRequest(MyEvent event) {
 *              // Do something useful for the request.
 *              // At this point we don't have any responses.
 *              // So the message is "Hello - null".
 *              System.out.println("OnRequest: '"
 *                  + event.getRequest()
 *                  + " - "
 *                  + event.getResponse()
 *                  + "'");
 *          }
 *          @Override
 *          public void handleResponse(MyEvent event) {
 *              // At this point we already received the response.
 *              // So the message is "Hello - World".
 *              System.out.println("OnResponse: '"
 *                  + event.getRequest()
 *                  + " - "
 *                  + event.getResponse()
 *                  + "'");
 *          }
 *      });
 *
 *      MyEvent event = new MyEvent("Hello");
 *      manager.fireEvent(event);
 *
 *      // A little bit later...
 *      Thread.sleep(200);
 *      event.setResponse("World");
 *  }
 *
 * </pre>
 * 
 * @author kotelnikov
 * @param <Q> - the type of the query
 * @param <A> - the answer type
 */
public abstract class CallEvent<Q, A> extends EventWithLifecycle {

    /**
     * This enumeration defines different state of this event. INIT =>
     * REQUEST_BEGIN => REQUEST_END => RESPONSE_BEGIN => RESPONSE_END
     * 
     * @author kotelnikov
     */
    public enum STAGE {

        /**
         * The event was just created and it was never fired yet.
         */
        INIT,

        /**
         * The event was fired the first time for the request stage.
         */
        REQUEST_BEGIN,
        /**
         * The request stage was finished.
         */
        REQUEST_END,
        /**
         * The event was fired for the response stage.
         */
        RESPONSE_BEGIN,
        /**
         * The event was fired for the response stage.
         */
        RESPONSE_END
    }

    /**
     * This flag
     */
    private boolean fHasResponse;

    private Q fRequest;

    private A fResponse;

    private STAGE fStage = STAGE.INIT;

    public CallEvent(Q request) {
        setRequest(request);
    }

    @Override
    public void clear() {
        super.clear();
        fStage = STAGE.INIT;
    }

    /**
     * @return the request
     */
    public Q getRequest() {
        return fRequest;
    }

    /**
     * @return the response
     */
    public A getResponse() {
        return fResponse;
    }

    /**
     * @return the stage
     */
    public STAGE getStage() {
        return fStage;
    }

    /**
     * Returns <code>true</code> if the response was already stored in this
     * event.
     * 
     * @return <code>true</code> if the response was already stored in this
     *         event
     */
    public boolean hasResponse() {
        return fHasResponse;
    }

    public boolean isRequestStage() {
        return fStage == STAGE.REQUEST_BEGIN;
    }

    public boolean isResponseStage() {
        return fStage == STAGE.RESPONSE_BEGIN;
    }

    @Override
    public void onHandleBegin(
        IEventManager eventManager,
        IEventListener<?> callback) {
        Throwable error = null;
        switch (fStage) {
            case INIT:
                fStage = STAGE.REQUEST_BEGIN;
                break;
            case REQUEST_END:
                // If fHasResponse flag is not true then it means that
                // the user explicitly fired the same event the second time
                // and the #setResponse(...) method was not used.
                if (!fHasResponse) {
                    fHasResponse = true;
                    error = new Error(
                        "This event could be fired explicitly only once. "
                            + "The setResponse(...) method should be used "
                            + "to send the response.");
                }
                fStage = STAGE.RESPONSE_BEGIN;
                break;
            default:
                error = new Error("This event should be cleared "
                    + "by the CallEvent.clear() method.");
                break;
        }
        super.onHandleBegin(eventManager, callback);
        if (error != null) {
            onError(error);
        }
    };

    @Override
    public void onHandleEnd() {
        try {
            super.onHandleEnd();
        } finally {
            switch (fStage) {
                case REQUEST_BEGIN:
                    fStage = STAGE.REQUEST_END;
                    tryToReply();
                    break;
                case RESPONSE_BEGIN:
                    fStage = STAGE.RESPONSE_END;
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Replies to this call. This method sets the specified response value and
     * tries re-fires this event in the "response" state using the original
     * event manager. This event will be launched only when the "request" stage
     * is finished
     * 
     * @param response the response to this call
     */
    public void reply(A response) {
        setResponseValue(response);
        fHasResponse = true;
        tryToReply();
    }

    /**
     * This method is used to set a new request object.
     * 
     * @param request the request to set
     */
    public void setRequest(Q request) {
        fRequest = request;
    }

    /**
     * Sets the specified response and fires this event in the "response" stage
     * using the original event manager. This method is deprecated. Use the
     * {@link #reply(Object)} method instead.
     * 
     * @param response the response to this call
     * @deprecated Use the {@link #reply(Object)} method instead
     * @see #reply(Object)
     */
    @Deprecated
    public final void setResponse(A response) {
        reply(response);
    }

    /**
     * Sets a new response value. This method just changes the value of the
     * {@link #fResponse} field but it does not try to reply to the request.
     * 
     * @param response a new value for the internal "response" field
     */
    public void setResponseValue(A response) {
        fResponse = response;
    }

    /**
     * This method tries to re-fire this event in the "response" state. The
     * event is fired in the "response" stage only when: a) it was delivered to
     * all listeners in the "request" stage and b) after somebody called the
     * {@link #reply(Object)} method.
     */
    @SuppressWarnings("unchecked")
    private void tryToReply() {
        if (fHasResponse && fStage == STAGE.REQUEST_END) {
            IEventListener<? super CallEvent<Q, A>> callback = (IEventListener<? super CallEvent<Q, A>>) getCallback();
            IEventManager eventManager = getEventManager();
            eventManager.fireEvent(this, callback);
        }
    }

}
