/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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
     * @throws Exception
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
     * @throws Exception
     */
    public boolean verify(String base64Signature, String content, String timestamp, String keyName) throws Exception;
}
