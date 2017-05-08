package org.carewebframework.ui;

/**
 * Events that control the application state.
 */
public enum ApplicationControl {
    
    SHUTDOWN_START, SHUTDOWN_ABORT, SHUTDOWN_PROGRESS, LOCK;
    
    public static final String EVENT_ROOT = "APP_CONTROL";

    private static final String EVENT_PREFIX = EVENT_ROOT + ".";

    /**
     * Returns the enum member corresponding to the event name.
     *
     * @param eventName The event name.
     * @return The corresponding member, or null if none.
     */
    public static ApplicationControl fromEvent(String eventName) {
        if (eventName.startsWith(EVENT_PREFIX)) {
            String name = eventName.substring(EVENT_PREFIX.length()).replace(".", "_");

            try {
                return ApplicationControl.valueOf(name);
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }

    private final String eventName;
    
    ApplicationControl() {
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
