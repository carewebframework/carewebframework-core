package org.carewebframework.ui.core;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseInputboxComponent;
import org.carewebframework.web.component.BaseLabeledComponent;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Html;
import org.carewebframework.web.component.Hyperlink;

public class CWFUtil {
    
    /**
     * Possible match modes for hierarchical tree search.
     */
    public enum MatchMode {
        AUTO, // Autodetect index vs label
        INDEX, // By node index.
        CASE_SENSITIVE, // Case sensitive by node label.
        CASE_INSENSITIVE // Case insensitive by node label.
    };
    
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
    
    public static BaseLabeledComponent findChildByLabel(BaseComponent parent, String label) {
        for (BaseLabeledComponent comp : parent.getChildren(BaseLabeledComponent.class)) {
            if (label.equals(comp.getLabel())) {
                return comp;
            }
        }
        
        return null;
    }
    
    /**
     * Sets focus to first input element under the parent that is capable of receiving focus.
     * 
     * @param parent Parent component.
     * @param select If true, select contents after setting focus.
     * @return The input element that received focus, or null if focus was not set.
     */
    public static BaseInputboxComponent<?> focusFirst(BaseComponent parent, boolean select) {
        for (BaseComponent child : parent.getChildren()) {
            BaseInputboxComponent<?> ele;
            
            if (child instanceof BaseInputboxComponent) {
                ele = (BaseInputboxComponent<?>) child;
                
                if (ele.isVisible() && !ele.isDisabled() && !ele.isReadonly()) {
                    ele.focus();
                    
                    if (select) {
                        ele.selectAll();
                    }
                    
                    return ele;
                }
            } else if ((ele = focusFirst(child, select)) != null) {
                return ele;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the node associated with the specified \-delimited path.
     * 
     * @param <NODE> Class of the node component.
     * @param root Root component of hierarchy.
     * @param nodeClass Class of the node component.
     * @param path \-delimited path to search.
     * @param create If true, nodes are created as needed.
     * @param matchMode The match mode.
     * @return The node corresponding to the specified path, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <NODE extends BaseComponent> NODE findNode(BaseComponent root, Class<NODE> nodeClass, String path,
                                                             boolean create, MatchMode matchMode) {
        String[] pcs = path.split("\\\\");
        BaseComponent node = null;
        
        try {
            for (String pc : pcs) {
                if (pc.isEmpty()) {
                    continue;
                }
                
                BaseComponent parent = node == null ? root : node;
                node = null;
                int index = matchMode == MatchMode.INDEX || matchMode == MatchMode.AUTO ? NumberUtils.toInt(pc, -1) : -1;
                MatchMode mode = matchMode != MatchMode.AUTO ? matchMode
                        : index >= 0 ? MatchMode.INDEX : MatchMode.CASE_INSENSITIVE;
                List<BaseComponent> children = parent.getChildren();
                int size = children.size();
                
                if (mode == MatchMode.INDEX) {
                    
                    if (index < 0) {
                        index = size;
                    }
                    
                    int deficit = index - size;
                    
                    if (!create && deficit >= 0) {
                        return null;
                    }
                    
                    while (deficit-- >= 0) {
                        parent.addChild(nodeClass.newInstance());
                    }
                    node = children.get(index);
                    
                } else {
                    for (BaseComponent child : children) {
                        String label = BeanUtils.getProperty(child, "label");
                        
                        if (mode == MatchMode.CASE_SENSITIVE ? pc.equals(label) : pc.equalsIgnoreCase(label)) {
                            node = child;
                            break;
                        }
                    }
                    
                    if (node == null) {
                        if (!create) {
                            return null;
                        }
                        node = nodeClass.newInstance();
                        parent.addChild(node);
                        BeanUtils.setProperty(node, "label", pc);
                    }
                }
                
                if (node == null) {
                    break;
                }
            }
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
        
        return (NODE) node;
    }
    
    private CWFUtil() {
    }
}
