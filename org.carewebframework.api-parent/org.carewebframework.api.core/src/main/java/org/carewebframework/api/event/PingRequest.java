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
package org.carewebframework.api.event;

import java.io.Serializable;
import java.util.List;

import org.carewebframework.api.messaging.Recipient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a ping request.
 */
public class PingRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public final Recipient requestor;
    
    public final String responseEvent;
    
    public final List<PingFilter> filters;
    
    @JsonCreator
    public PingRequest(@JsonProperty("responseEvent") String responseEvent,
        @JsonProperty("filters") List<PingFilter> filters, @JsonProperty("requestor") Recipient requestor) {
        this.responseEvent = responseEvent;
        this.filters = filters;
        this.requestor = requestor;
    }
}
