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

import org.carewebframework.common.Localizer.IMessageSource;

import org.zkoss.text.MessageFormats;
import org.zkoss.util.resource.Labels;

/**
 * Wraps ZK's Labels class in as message source.
 */
public class LabelResolver implements IMessageSource {
    
    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        String msg = Labels.getLabel(code);
        return msg == null ? null : args == null ? msg : MessageFormats.format(msg, args, locale);
    }
    
}
