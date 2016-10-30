package org.carewebframework.ui.test;

import org.carewebframework.web.client.Session;
import org.carewebframework.web.component.Page;

public class MockSession extends Session {
    
    public MockSession(MockServletContext servletContext, MockWebSocketSession socket) {
        super(servletContext, socket);
        Page page = Page._create("mockpage");
        validatePage(page.getId());
    }
    
    @Override
    protected void destroy() {
        super.destroy();
    }
}
