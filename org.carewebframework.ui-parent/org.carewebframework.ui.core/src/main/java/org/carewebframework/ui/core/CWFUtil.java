package org.carewebframework.ui.core;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Html;
import org.carewebframework.web.component.Hyperlink;

public class CWFUtil {
    
    /**
     * Returns the CWF resource path for the specified class.
     * 
     * @param clazz Class to evaluate
     * @return String representing resource path
     */
    public static final String getResourcePath(Class<?> clazz) {
        return getResourcePath(clazz.getPackage());
    }
    
    /**
     * Returns the CWF resource path for the specified class.
     * 
     * @param clazz Class to evaluate
     * @param up Number of path levels to remove
     * @return String representing resource path
     */
    public static final String getResourcePath(Class<?> clazz, int up) {
        return getResourcePath(clazz.getPackage(), up);
    }
    
    /**
     * Returns the CWF resource path for the specified package.
     * 
     * @param pkg Package to evaluate
     * @return String representing resource path
     */
    public static final String getResourcePath(Package pkg) {
        return getResourcePath(pkg.getName());
    }
    
    /**
     * Returns the CWF resource path for the specified package.
     * 
     * @param pkg Package to evaluate
     * @param up Number of path levels to remove
     * @return String representing resource path
     */
    public static final String getResourcePath(Package pkg, int up) {
        return getResourcePath(pkg.getName(), up);
    }
    
    /**
     * Returns the CWF resource path for the package name.
     * 
     * @param name Package name
     * @return String representing resource path
     */
    public static final String getResourcePath(String name) {
        return getResourcePath(name, 0);
    }
    
    /**
     * Returns the CWF resource path for the package name.
     * 
     * @param name Package name
     * @param up Number of path levels to remove
     * @return String representing resource path
     */
    public static final String getResourcePath(String name, int up) {
        String path = StringUtils.chomp(name.replace('.', '/'), "/");
        
        while (up > 0) {
            int i = path.lastIndexOf("/");
            
            if (i <= 0) {
                break;
            } else {
                path = path.substring(0, i);
                up--;
            }
        }
        
        return "web/" + path + "/";
    }
    
    /**
     * Formats an exception for display.
     * 
     * @param exc Exception to format.
     * @return The displayable form of the exception.
     */
    public static String formatExceptionForDisplay(Throwable exc) {
        Throwable root = ExceptionUtils.getRootCause(exc);
        return exc == null ? null : ExceptionUtils.getMessage(root == null ? exc : root);
    }
    
    /**
     * Returns a component of a type suitable for displaying the specified text. For text that is a
     * URL, returns a hyperlink. For text that begins with &lt;html&gt;, returns an HTML component.
     * All other text returns a label.
     * 
     * @param text Text to be displayed.
     * @return BaseComponent of the appropriate type.
     */
    public static BaseComponent getTextComponent(String text) {
        String frag = text == null ? "" : StringUtils.substring(text, 0, 20).toLowerCase();
        
        if (frag.contains("<html>")) {
            return new Html(text);
        }
        
        if (frag.matches("^https?:\\/\\/.+$")) {
            Hyperlink link = new Hyperlink();
            link.setHref(text);
            link.setTarget("_blank");
            return link;
        }
        
        return new Cell(text);
    }
    
    /**
     * Wires variables from a map into a controller. Useful to inject parameters passed in an
     * argument map.
     * 
     * @param map The argument map.
     * @param controller The controller to be wired.
     */
    public static void wireController(Map<?, ?> map, Object controller) {
        if (map == null || map.isEmpty() || controller == null) {
            return;
        }
        
        for (Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            
            try {
                PropertyUtils.setProperty(controller, key, value);
            } catch (Exception e) {
                try {
                    FieldUtils.writeField(controller, key, value, true);
                } catch (Exception e1) {}
            }
            
        }
    }
    
    private CWFUtil() {
    }
}
