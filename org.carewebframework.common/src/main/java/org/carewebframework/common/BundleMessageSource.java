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
package org.carewebframework.common;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Support for resource bundles as a message source.
 */
public class BundleMessageSource implements Localizer.IMessageSource {
    
    /**
     * The first "."-delimited piece of the id is the bundle base with the remainder being the
     * message key.
     */
    @Override
    public String getMessage(String id, Locale locale, Object... args) {
        try {
            String[] pcs = id.split("\\.", 2);
            String message = pcs.length < 2 ? null : ResourceBundle.getBundle(pcs[0], locale).getString(pcs[1]);
            return message == null ? null : format(message, locale, args);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Formats the message with the specified arguments. If a format error is encountered, the
     * unformatted message is returned.
     * 
     * @param message The unformatted message.
     * @param locale The locale.
     * @param args The arguments to be used for formatting.
     * @return The formatted message.
     */
    private String format(String message, Locale locale, Object... args) {
        try {
            return new MessageFormat(message, locale).format(args);
        } catch (Exception e) {
            return message;
        }
    }
    
}
