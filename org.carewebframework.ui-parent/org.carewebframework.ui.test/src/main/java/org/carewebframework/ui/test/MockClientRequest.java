package org.carewebframework.ui.test;

import java.util.Map;

import org.carewebframework.web.client.ClientRequest;

public class MockClientRequest extends ClientRequest {
    
    public MockClientRequest(Map<String, Object> map) {
        super(map);
    }
    
}
