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
package org.carewebframework.ui.spring;

import org.carewebframework.common.Localizer;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;

/**
 * Allow Thymeleaf templates to resolve externalized messages.
 */
public class ThymeleafMessageResolver extends AbstractMessageResolver {
    
    @Override
    public String createAbsentMessageRepresentation(ITemplateContext context, Class<?> origin, String key,
                                                    Object[] messageParameters) {
        return null;
    }
    
    @Override
    public String resolveMessage(ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
        return Localizer.getMessage(key, context.getLocale(), messageParameters);
    }

}
