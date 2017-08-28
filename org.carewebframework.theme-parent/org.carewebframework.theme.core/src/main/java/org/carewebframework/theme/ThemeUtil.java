/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.theme;

import org.fujion.component.BaseUIComponent;

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
     * @param themeClasses A list of theme classes to apply.
     */
    public static void applyThemeClass(BaseUIComponent component, IThemeClass... themeClasses) {
        StringBuilder sb = new StringBuilder();
        
        for (IThemeClass themeClass : themeClasses) {
            String cls = themeClass == null ? null : themeClass.getThemeClass();
            
            if (cls != null) {
                sb.append(sb.length() > 0 ? " " : "").append(themeClass.getThemeClass());
            }
        }
        
        component.addClass(sb.toString());
    }
    
    /**
     * Corresponds to Bootstrap button size classes.
     */
    public enum ButtonSize implements IThemeClass {
        LARGE("lg"), MEDIUM("md"), SMALL("sm"), TINY("xs");
        
        private final String themeClass;
        
        ButtonSize(String themeClass) {
            this.themeClass = themeClass;
        }
        
        @Override
        public String getThemeClass() {
            return themeClass == null ? null : "size:btn-" + themeClass;
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
            return "flavor:btn-" + name().toLowerCase();
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
        XLARGE("xl"), LARGE("lg"), DEFAULT("rg"), SMALL("sm"), TINY("xs");
        
        private final String themeClass;
        
        LabelSize(String themeClass) {
            this.themeClass = themeClass;
        }
        
        @Override
        public String getThemeClass() {
            return "size:label-" + themeClass;
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
            return "flavor:label-" + name().toLowerCase();
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
            return "flavor:panel-" + name().toLowerCase();
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
    }
}
