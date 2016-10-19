package org.carewebframework.ui.test;

import org.carewebframework.web.client.Synchronizer;

public class MockSynchronizer extends Synchronizer {
    
    MockSynchronizer(MockWebSocketSession session) {
        super(session);
    }
    
}
