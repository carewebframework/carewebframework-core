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
package org.carewebframework.api.context;

/**
 * Base interface for context change event. All context objects should define a descendant of this
 * interface.
 */
public interface IContextEvent {
    
    /**
     * Survey of context subscriber
     * 
     * @param silent If true, user interaction is not permitted.
     * @return Null or empty string if context change should proceed. Any other value constitutes a
     *         no vote for the context change request.
     */
    String pending(boolean silent);
    
    /**
     * Committed context event
     */
    void committed();
    
    /**
     * Cancellation of context event
     */
    void canceled();
}
