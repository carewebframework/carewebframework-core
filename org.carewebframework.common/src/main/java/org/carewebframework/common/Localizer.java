/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides localization support.
 */
public class Localizer {
    
    public interface IMessageSource {
        
        /**
         * Retrieve a message for the specified locale given its id.
         * 
         * @param id The message identifier.
         * @param locale The locale.
         * @param args Optional message arguments.
         * @return A fully formatted message, or null if none was found.
         */
        String getMessage(String id, Locale locale, Object... args);
        
    }
    
    public interface ILocaleFinder {
        
        /**
         * Returns the default locale when none is specified.
         * 
         * @return The default locale
         */
        Locale getLocale();
    }
    
    private static final Log log = LogFactory.getLog(Localizer.class);
    
    private static final List<IMessageSource> messageSources = new ArrayList<>();
    
    private static ILocaleFinder localeFinder = new ILocaleFinder() {
        
        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
        
    };
    
    /**
     * Registers a message source for resolving messages.
     * 
     * @param messageSource The message source.
     */
    public static void registerMessageSource(IMessageSource messageSource) {
        messageSources.add(messageSource);
    }
    
    private Localizer() {
    }
    
    /**
     * Returns a formatted message given a label identifier. Recognizes line continuation with
     * backslash characters.
     * 
     * @param id A label identifier.
     * @param locale The locale.
     * @param args Optional replaceable parameters.
     * @return The formatted label.
     */
    public static String getMessage(String id, Locale locale, Object... args) {
        locale = locale == null ? getDefaultLocale() : locale;
        
        for (IMessageSource messageSource : messageSources) {
            try {
                return messageSource.getMessage(id, locale, args).replace("\\\n", "");
            } catch (Exception e) {
                // Ignore and try next message source.
            }
        }
        // Failing resolution, just return null.
        log.warn("Label not found for identifier: " + id);
        return null;
    }
    
    public static Locale getDefaultLocale() {
        return localeFinder.getLocale();
    }
    
    public static void setLocaleFinder(ILocaleFinder localeFinder) {
        Localizer.localeFinder = localeFinder;
    }
    
}
