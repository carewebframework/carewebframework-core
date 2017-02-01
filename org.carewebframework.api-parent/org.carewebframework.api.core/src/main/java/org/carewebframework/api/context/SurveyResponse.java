package org.carewebframework.api.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * Tracks responses to a context change survey. Supports asynchronous polling of subscribers.
 */
public class SurveyResponse implements ISurveyResponse {
    
    public enum ResponseState {
        NULL, ACCEPTED, REJECTED, DEFERRED
    }
    
    private final List<String> responses = new ArrayList<>();
    
    private ISurveyCallback callback;
    
    private final boolean silent;
    
    private ResponseState state = ResponseState.NULL;
    
    public SurveyResponse() {
        this(false);
    }
    
    public SurveyResponse(boolean silent) {
        this.silent = silent;
    }
    
    public SurveyResponse(String response) {
        this(false);
        responses.add(response);
    }
    
    @Override
    public boolean rejected() {
        return !responses.isEmpty();
    }
    
    @Override
    public boolean isSilent() {
        return silent;
    }
    
    @Override
    public void merge(ISurveyResponse response) {
        responses.addAll(response.getResponses());
    }
    
    @Override
    public Collection<String> getResponses() {
        return Collections.unmodifiableCollection(responses);
    }
    
    @Override
    public void accept() {
        updateState(ResponseState.ACCEPTED);
    }
    
    @Override
    public void defer() {
        if (silent) {
            throw new ContextException("May not defer a response in silent mode");
        }
        
        updateState(ResponseState.DEFERRED);
    }
    
    @Override
    public void reject(String response) {
        responses.add(response == null || response.isEmpty() ? "Unspecified reason" : response);
        
        try {
            updateState(ResponseState.REJECTED);
        } catch (ContextException e) {
            responses.remove(responses.size() - 1);
            throw e;
        }
    }
    
    protected void reset(ISurveyCallback callback) {
        state = ResponseState.NULL;
        this.callback = callback;
    }
    
    protected ResponseState getState() {
        return state;
    }
    
    private void updateState(ResponseState newState) {
        if (state != newState) {
            switch (state) {
                case NULL:
                    state = newState;
                    break;
                
                case ACCEPTED:
                case REJECTED:
                    throw new ContextException("A response has already been registered for this subscriber.");
                    
                case DEFERRED:
                    state = newState;
                    
                    if (callback != null) {
                        callback.response(this);
                    }
            }
        }
    }
    
    @Override
    public String toString() {
        return StringUtils.collectionToDelimitedString(responses, "\n");
    }
    
}
