package org.carewebframework.ui.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class MockWebSocketSession implements WebSocketSession {
    
    private URI uri;
    
    private final Map<String, Object> attributes = new HashMap<>();
    
    private Principal principal;
    
    int messageSizeLimitText = 5000;
    
    int messageSizeLimitBinary = 5000;
    
    boolean open = true;
    
    public MockWebSocketSession() throws Exception {
        uri = new URI("http://mock.domain.org");
        
        principal = new Principal() {
            
            @Override
            public String getName() {
                return "mock-principle";
            }
            
        };
    }
    
    @Override
    public String getId() {
        return "mock-id";
    }
    
    @Override
    public URI getUri() {
        return uri;
    }
    
    @Override
    public HttpHeaders getHandshakeHeaders() {
        return null;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public Principal getPrincipal() {
        return principal;
    }
    
    @Override
    public InetSocketAddress getLocalAddress() {
        return null;
    }
    
    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }
    
    @Override
    public String getAcceptedProtocol() {
        return null;
    }
    
    @Override
    public void setTextMessageSizeLimit(int messageSizeLimit) {
        messageSizeLimitText = messageSizeLimit;
    }
    
    @Override
    public int getTextMessageSizeLimit() {
        return messageSizeLimitText;
    }
    
    @Override
    public void setBinaryMessageSizeLimit(int messageSizeLimit) {
        messageSizeLimitBinary = messageSizeLimit;
    }
    
    @Override
    public int getBinaryMessageSizeLimit() {
        return messageSizeLimitBinary;
    }
    
    @Override
    public List<WebSocketExtension> getExtensions() {
        return null;
    }
    
    @Override
    public void sendMessage(WebSocketMessage<?> message) throws IOException {
    }
    
    @Override
    public boolean isOpen() {
        return open;
    }
    
    @Override
    public void close() throws IOException {
        open = false;
    }
    
    @Override
    public void close(CloseStatus status) throws IOException {
        open = false;
    }
    
}
