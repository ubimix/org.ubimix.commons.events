package org.ubimix.commons.events;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ubimix.commons.events.calls.CallEventTest;
import org.ubimix.commons.events.server.AsyncEventManagerTest;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
            "Test for org.ubimix.commons.events");
        // $JUnit-BEGIN$
        suite.addTestSuite(EventListenerInterceptorTest.class);
        suite.addTestSuite(EventManagerTest.class);
        suite.addTestSuite(EventObservationTest.class);
        suite.addTestSuite(CallEventTest.class);
        suite.addTestSuite(AsyncEventManagerTest.class);
        // $JUnit-END$
        return suite;
    }
}
