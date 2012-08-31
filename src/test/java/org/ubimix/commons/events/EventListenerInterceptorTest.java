/**
 * 
 */
package org.ubimix.commons.events;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author kotelnikov
 */
public class EventListenerInterceptorTest extends TestCase {

    private static class Add extends CommandLineEvent {
    }

    private static class CommandLineEvent {
    }

    private static class Create extends CommandLineEvent {
    }

    private static class Delete extends CommandLineEvent {
    }

    private static class Remove extends CommandLineEvent {
    }

    /**
     * @param name
     */
    public EventListenerInterceptorTest(String name) {
        super(name);
    }

    public void test() throws Exception {
        class MyEvent {
        }

        final IEventListener<?>[] result = { null };
        IEventManager manager = new EventManager();
        manager.addListenerInterceptor(new EventListenerInterceptor() {
            @Override
            public void onAddListener(
                Class<?> eventType,
                IEventListener<?> listener) {
                result[0] = listener;
            }

            @Override
            public void onRemoveListener(
                Class<?> eventType,
                IEventListener<?> listener) {
                result[0] = null;
            }
        });
        assertNull(result[0]);

        IEventListener<MyEvent> listener = new IEventListener<MyEvent>() {
            public void handleEvent(MyEvent event) {
            }
        };
        manager.addListener(MyEvent.class, listener);
        assertSame(listener, result[0]);
        manager.removeListener(MyEvent.class, listener);
        assertNull(result[0]);

        IEventListenerRegistration r = manager.addListener(
            MyEvent.class,
            listener);
        assertSame(listener, result[0]);
        r.unregister();
        assertNull(result[0]);

    }

    public void testCommands() {
        IEventManager manager = new EventManager();
        final Set<String> listOfCommands = new HashSet<String>();
        manager.addListenerInterceptor(new EventListenerInterceptor() {
            private String getCommandName(Class<? extends CommandLineEvent> type) {
                String name = type.getName();
                int idx = name.lastIndexOf("$");
                name = name.substring(idx + 1);
                name = name.toLowerCase();
                return name;
            }

            @Override
            public void onAddListener(
                Class<?> eventType,
                IEventListener<?> listener) {
                if (CommandLineEvent.class.isAssignableFrom(eventType)) {
                    // Automatically add this command to the list.
                    @SuppressWarnings("unchecked")
                    Class<? extends CommandLineEvent> type = (Class<? extends CommandLineEvent>) eventType;
                    String command = getCommandName(type);
                    listOfCommands.add(command);
                }
            }

            @Override
            public void onRemoveListener(
                Class<?> eventType,
                IEventListener<?> listener) {
                if (CommandLineEvent.class.isAssignableFrom(eventType)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends CommandLineEvent> type = (Class<? extends CommandLineEvent>) eventType;
                    String command = getCommandName(type);
                    listOfCommands.remove(command);
                }
            }
        });

        assertEquals(0, listOfCommands.size());
        IEventListener<CommandLineEvent> commandListener = new IEventListener<CommandLineEvent>() {
            public void handleEvent(CommandLineEvent event) {
                // Do nothing
            }
        };
        manager.addListener(Add.class, commandListener);
        assertEquals(1, listOfCommands.size());
        assertTrue(listOfCommands.contains("add"));

        manager.addListener(Remove.class, commandListener);
        assertEquals(2, listOfCommands.size());
        assertTrue(listOfCommands.contains("add"));
        assertTrue(listOfCommands.contains("remove"));

        manager.addListener(Create.class, commandListener);
        assertEquals(3, listOfCommands.size());
        assertTrue(listOfCommands.contains("add"));
        assertTrue(listOfCommands.contains("remove"));
        assertTrue(listOfCommands.contains("create"));

        manager.addListener(Delete.class, commandListener);
        assertEquals(4, listOfCommands.size());
        assertTrue(listOfCommands.contains("add"));
        assertTrue(listOfCommands.contains("remove"));
        assertTrue(listOfCommands.contains("create"));
        assertTrue(listOfCommands.contains("delete"));

        manager.removeListener(Delete.class, commandListener);
        assertEquals(3, listOfCommands.size());
        assertTrue(listOfCommands.contains("add"));
        assertTrue(listOfCommands.contains("remove"));
        assertTrue(listOfCommands.contains("create"));

        manager.removeListener(Create.class, commandListener);
        assertEquals(2, listOfCommands.size());
        assertTrue(listOfCommands.contains("add"));
        assertTrue(listOfCommands.contains("remove"));

        manager.removeListener(Remove.class, commandListener);
        assertEquals(1, listOfCommands.size());
        assertTrue(listOfCommands.contains("add"));

        manager.removeListener(Add.class, commandListener);
        assertEquals(0, listOfCommands.size());
    }

}
