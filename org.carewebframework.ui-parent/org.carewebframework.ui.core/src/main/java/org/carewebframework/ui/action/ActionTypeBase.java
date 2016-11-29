package org.carewebframework.ui.action;

import java.util.regex.Pattern;

public abstract class ActionTypeBase implements IActionType {
    
    private final String name;
    
    private final Pattern pattern;
    
    public ActionTypeBase(String name, String pattern) {
        this.name = name;
        this.pattern = Pattern.compile(pattern);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean matches(String script) {
        return pattern.matcher(script).matches();
    }
    
    /**
     * Strips the prefix from a script.
     * 
     * @param script The script.
     * @return The script without the prefix.
     */
    protected String stripPrefix(String script) {
        return script.substring(script.indexOf(':') + 1);
    }
    
}
