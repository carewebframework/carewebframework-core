/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import org.zkoss.text.MessageFormats;
import org.zkoss.util.resource.Labels;

/**
 * Wraps ZK's Labels class in a Spring message source.
 */
public class LabelResolver implements MessageSource {
    
    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        final String msg = Labels.getLabel(code, defaultMessage);
        return msg == null ? null : args == null ? msg : MessageFormats.format(msg, args, locale);
    }
    
    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        final String msg = getMessage(code, args, null, locale);
        
        if (msg == null) {
            throw new NoSuchMessageException(code);
        }
        
        return msg;
    }
    
    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        final String defaultMessage = resolvable.getDefaultMessage();
        final Object[] args = resolvable.getArguments();
        String lastCode = "no code specified";
        
        for (String code : resolvable.getCodes()) {
            lastCode = code;
            final String msg = getMessage(code, args, defaultMessage, locale);
            
            if (msg != null) {
                return msg;
            }
        }
        
        throw new NoSuchMessageException(lastCode);
    }
}
