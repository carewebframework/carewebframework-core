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
package org.carewebframework.ui;

import java.util.Locale;

import org.carewebframework.common.Localizer.IMessageSource;

import org.zkoss.text.MessageFormats;
import org.zkoss.util.resource.Labels;

/**
 * Wraps ZK's Labels class as message source.
 */
public class LabelResolver implements IMessageSource {
    
    
    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        String msg = Labels.getLabel(code);
        return msg == null ? null : args == null ? msg : MessageFormats.format(msg, args, locale);
    }
    
}
