package org.carewebframework.ui.session;

/**
 * Events used to control session state via administrator functions.
 */
public enum SessionControl {
    
    SHUTDOWN_START, SHUTDOWN_ABORT, SHUTDOWN_PROGRESS, LOCK;
    
    public static final String EVENT_ROOT = "SESSION_CONTROL";

    private static final String EVENT_PREFIX = EVENT_ROOT + ".";

    /**
     * Returns the enum member corresponding to the event name.
     *
     * @param eventName The event name.
     * @return The corresponding member, or null if none.
     */
    public static SessionControl fromEvent(String eventName) {
        if (eventName.startsWith(EVENT_PREFIX)) {
            String name = eventName.substring(EVENT_PREFIX.length()).replace(".", "_");

            try {
                return SessionControl.valueOf(name);
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }

    private final String eventName;
    
    SessionControl() {
        eventName = EVENT_PREFIX + name().replace("_", ".");
    }
    
    /**
     * Returns the event name for this member.
     *
     * @return The event name.
     */
    public String getEventName() {
        return eventName;
    }
}
