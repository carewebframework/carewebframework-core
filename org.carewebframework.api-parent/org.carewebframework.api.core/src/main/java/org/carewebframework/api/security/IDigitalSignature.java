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
package org.carewebframework.api.security;

/**
 * Interface implemented by a digital signature provider.
 */
public interface IDigitalSignature {
    
    /**
     * Returns the name of the key that will be used to generate a digital signature.
     * 
     * @return Key name.
     */
    public String getKeyName();
    
    /**
     * Returns a digital signature for the content string.
     * 
     * @param content Content to be signed.
     * @return A base 64-encoded digital signature.
     * @throws Exception Unspecified exception.
     */
    public String sign(String content) throws Exception;
    
    /**
     * Verifies the validity of the digital signature.
     * 
     * @param base64Signature The digital signature.
     * @param content The authorization string to which the signature was applied.
     * @param timestamp The timestamp of the digital signature.
     * @param keyName Key name to use.
     * @return True if the signature is valid.
     * @throws Exception Unspecified exception.
     */
    public boolean verify(String base64Signature, String content, String timestamp, String keyName) throws Exception;
}
