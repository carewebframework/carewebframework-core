package org.carewebframework.ui.event;

import org.carewebframework.common.AbstractRegistry;
import org.carewebframework.common.RegistryMap.DuplicateAction;

public class InvocationRequestQueueRegistry extends AbstractRegistry<String, InvocationRequestQueue> {
    
    private static final InvocationRequestQueueRegistry instance = new InvocationRequestQueueRegistry();
    
    public static InvocationRequestQueueRegistry getInstance() {
        return instance;
    }
    
    private InvocationRequestQueueRegistry() {
        super(DuplicateAction.ERROR);
    }
    
    @Override
    protected String getKey(InvocationRequestQueue queue) {
        return queue.getName();
    }
    
}
