package org.carewebframework.ui.action;

public interface IActionType {
    
    boolean matches(String script);
    
    Object parse(String script);
    
    void execute(Object script);
    
    String getName();
}
