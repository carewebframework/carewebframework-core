package org.carewebframework.shell.property;

/**
 * Used to hold property values prior to plugin initialization. When a plugin is subsequently
 * initialized and registers a property, the value in the corresponding proxy is used to initialize
 * the property. This allows the deserializer to initialize property values even though the plug-in
 * has not yet been instantiated.
 */
public class PropertyProxy {
    
    private Object value;
    
    private final PropertyInfo propInfo;
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    public PropertyInfo getPropertyInfo() {
        return propInfo;
    }
    
    public PropertyProxy(PropertyInfo propInfo, Object value) {
        this.propInfo = propInfo;
        this.value = value;
    }
}
