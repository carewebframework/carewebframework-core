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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.carewebframework.api.security.IDigitalSignature;

/**
 * Class for secure marshaling and unmarshaling of contexts for passing in a url.
 */
public class ContextMarshaller {
    
    public static final String PROPNAME_KEY = "Signature.Key";
    
    public static final String PROPNAME_TIME = "Signature.Time";
    
    private final IDigitalSignature signer;
    
    public ContextMarshaller(IDigitalSignature signer) throws Exception {
        super();
        this.signer = signer;
    }
    
    /**
     * Marshals the current context as a string.
     * 
     * @param contextItems The context items to marshal.
     * @return The marshaled context.
     */
    public String marshal(ContextItems contextItems) {
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMddHHmmssz");
        contextItems.setItem(PROPNAME_TIME, timestampFormat.format(new Date()));
        contextItems.setItem(PROPNAME_KEY, signer.getKeyName());
        return contextItems.toString();
    }
    
    /**
     * Digitally signs the specified text.
     * 
     * @param text Text to sign
     * @return Digital signature
     * @throws Exception Unspecified exception.
     */
    public String sign(String text) throws Exception {
        return signer.sign(text);
    }
    
    /**
     * Unmarshals the marshaled context. Performs digital signature verification, then returns the
     * unmarshaled context items.
     * 
     * @param marshaledContext Marshaled context
     * @param authSignature If set, the digital signature is verified.
     * @return The unmarshaled context.
     * @throws Exception Unspecified exception.
     */
    public ContextItems unmarshal(String marshaledContext, String authSignature) throws Exception {
        ContextItems contextItems = new ContextItems();
        contextItems.addItems(marshaledContext);
        String whichKey = contextItems.getItem(PROPNAME_KEY);
        String timestamp = contextItems.getItem(PROPNAME_TIME);
        
        if (authSignature != null && !signer.verify(authSignature, marshaledContext, timestamp, whichKey)) {
            throw new MarshalException("Invalid digital signature");
        }
        
        return contextItems;
    }
}
