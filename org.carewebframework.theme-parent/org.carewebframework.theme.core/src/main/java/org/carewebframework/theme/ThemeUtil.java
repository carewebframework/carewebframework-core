package org.carewebframework.theme;

import org.zkoss.zk.ui.HtmlBasedComponent;

/**
 * Utility methods for manipulating theme styles.
 */
public class ThemeUtil {
    
    public interface IThemeClass {
        
        public String getThemeClass();
    }
    
    /**
     * Applies one or more theme classes to a component.
     * 
     * @param component Component to receive the theme classes.
     * @param baseClass A base theme class (may be null).
     * @param themeClasses A list of theme classes to apply.
     */
    public static void applyThemeClass(HtmlBasedComponent component, String baseClass, IThemeClass... themeClasses) {
        StringBuilder sb = new StringBuilder();
        
        if (baseClass != null) {
            component.setZclass(baseClass);
        }
        
        for (IThemeClass themeClass : themeClasses) {
            String cls = themeClass == null ? null : themeClass.getThemeClass();
            
            if (cls != null) {
                sb.append(sb.length() > 0 ? " " : "").append(themeClass.getThemeClass());
            }
        }
        
        component.setSclass(sb.toString());
    }
    
    /**
     * Corresponds to Bootstrap button size classes.
     */
    public enum ButtonSize implements IThemeClass {
        DEFAULT(null), LARGE("btn-lg"), SMALL("btn-sm"), TINY("btn-xs");
        
        private final String themeClass;
        
        ButtonSize(String themeClass) {
            this.themeClass = themeClass;
        }
        
        @Override
        public String getThemeClass() {
            return themeClass;
        }
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    };
    
    /**
     * Corresponds to Bootstrap button style classes.
     */
    public enum ButtonStyle implements IThemeClass {
        DEFAULT, PRIMARY, SUCCESS, INFO, WARNING, DANGER, LINK;
        
        @Override
        public String getThemeClass() {
            return "btn-" + name().toLowerCase();
        }
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    };
    
    /**
     * Corresponds to Bootstrap button size classes.
     */
    public enum LabelSize implements IThemeClass {
        XLARGE("label-xl"), LARGE("label-lg"), DEFAULT("label-rg"), SMALL("label-sm"), TINY("label-xs");
        
        private final String themeClass;
        
        LabelSize(String themeClass) {
            this.themeClass = themeClass;
        }
        
        @Override
        public String getThemeClass() {
            return themeClass;
        }
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    };
    
    /**
     * Corresponds to Bootstrap label style classes.
     */
    public enum LabelStyle implements IThemeClass {
        DEFAULT, PRIMARY, SUCCESS, INFO, WARNING, DANGER, NONE;
        
        @Override
        public String getThemeClass() {
            return "label-" + name().toLowerCase();
        }
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    };
    
    /**
     * Corresponds to Bootstrap panel style classes.
     */
    public enum PanelStyle implements IThemeClass {
        DEFAULT, PRIMARY, SUCCESS, INFO, WARNING, DANGER;
        
        @Override
        public String getThemeClass() {
            return "panel-" + name().toLowerCase();
        }
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    };
    
    /**
     * Enforce static class.
     */
    private ThemeUtil() {
    };
}
