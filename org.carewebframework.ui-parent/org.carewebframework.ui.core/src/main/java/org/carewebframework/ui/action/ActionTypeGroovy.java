package org.carewebframework.ui.action;

import java.lang.reflect.Method;

import org.carewebframework.common.MiscUtil;

/**
 * Action type is a Groovy script. Groovy shell classes are loaded on demand.
 */
public class ActionTypeGroovy extends ActionTypeBase {
    
    private static volatile boolean initialized;
    
    private static Object shell;
    
    private static Method parseMethod;
    
    private static Class<?> scriptClass;
    
    private static Method runMethod;
    
    private static synchronized void initialize() {
        if (!initialized) {
            try {
                shell = Class.forName("groovy.lang.GroovyShell").newInstance();
                parseMethod = shell.getClass().getMethod("parse", String.class);
                scriptClass = Class.forName("groovy.lang.Script");
                runMethod = scriptClass.getMethod("run");
                initialized = true;
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
    }
    
    public ActionTypeGroovy() {
        super("groovy", "^groovy:.*");
    }
    
    @Override
    public Object parse(String script) {
        init();
        
        try {
            return parseMethod.invoke(shell, stripPrefix(script));
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    @Override
    public void execute(Object script) {
        init();
        
        try {
            runMethod.invoke(script);
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    private void init() {
        if (!initialized) {
            initialize();
        }
    }
}
