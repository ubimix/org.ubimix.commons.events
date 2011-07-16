package org.webreformatter.commons.events;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.webreformatter.commons.events.calls.CallEventTest;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
            "Test for org.webreformatter.commons.events");
        // $JUnit-BEGIN$
        suite.addTestSuite(EventListenerInterceptorTest.class);
        suite.addTestSuite(EventManagerTest.class);
        suite.addTestSuite(EventObservationTest.class);
        suite.addTestSuite(CallEventTest.class);
        // $JUnit-END$
        return suite;
    }

}
