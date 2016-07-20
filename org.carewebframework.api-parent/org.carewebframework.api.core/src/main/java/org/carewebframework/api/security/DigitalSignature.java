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

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Digital signature service implementation. To verify a digital signature, the duration property
 * must be set (defaults to 5 minutes). To generate a digital signature, the key name and private
 * key password properties must be set.
 */
public class DigitalSignature implements IDigitalSignature {
    
    private static final Log log = LogFactory.getLog(DigitalSignature.class);
    
    private int duration = 5;
    
    private String keyName;
    
    private String privateKeyPassword;
    
    private KeyStore keystore;
    
    /**
     * Creates an instance of the signature verification service.
     * 
     * @param keystoreLocation Full path to the JKS key store.
     */
    public DigitalSignature(String keystoreLocation) {
        this(keystoreLocation, "JKS");
    }
    
    /**
     * Creates an instance of the signature verification service.
     * 
     * @param keystoreLocation Full path to the key store.
     * @param keystoreType The keystore type.
     */
    public DigitalSignature(String keystoreLocation, String keystoreType) {
        super();
        
        try {
            keystore = CipherUtil.getKeyStore(keystoreLocation, keystoreType);
        } catch (Exception e) {
            log.error("Error attempting to load keystore " + keystoreLocation, e);
        }
    }
    
    /**
     * Verifies the validity of the digital signature using stored key name.
     * 
     * @param base64Signature The digital signature.
     * @param content The authorization string to which the signature was applied.
     * @param timestamp The timestamp of the digital signature.
     * @return True if the signature is valid.
     * @throws Exception Unspecified exception.
     */
    public boolean verify(String base64Signature, String content, String timestamp) throws Exception {
        return verify(base64Signature, content, timestamp, keyName);
    }
    
    /**
     * @see org.carewebframework.api.security.IDigitalSignature#verify(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean verify(String base64Signature, String content, String timestamp, String keyName) throws Exception {
        Certificate cert = keystore.getCertificate(keyName);
        
        if (cert == null) {
            log.error(("Missing public key certificate: " + keyName));
            return false;
        }
        
        return CipherUtil.verify(cert.getPublicKey(), base64Signature, content, timestamp, duration);
    }
    
    /**
     * @see org.carewebframework.api.security.IDigitalSignature#sign(java.lang.String)
     */
    @Override
    public String sign(String content) throws Exception {
        PrivateKey privateKey = (PrivateKey) keystore.getKey(keyName, privateKeyPassword.toCharArray());
        
        if (privateKey == null) {
            throw new SignatureException("No key " + keyName + " found");
        }
        
        return CipherUtil.sign(privateKey, content);
    }
    
    /**
     * Returns the duration, in minutes, that a signed payload will remain valid beyond its
     * timestamp.
     * 
     * @return The duration in minutes.
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * Sets the duration, in minutes, that a signed payload will remain valid beyond its timestamp.
     * 
     * @param duration Duration in minutes.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    /**
     * @see org.carewebframework.api.security.IDigitalSignature#getKeyName()
     */
    @Override
    public String getKeyName() {
        return keyName;
    }
    
    /**
     * Sets the name of the key that will be used to generate a digital signature.
     * 
     * @param keyName The key name.
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
    
    /**
     * Returns the password used to extract the private key for purposes of generating a digital
     * signature.
     * 
     * @return The private key password.
     */
    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }
    
    /**
     * Sets the password used to extract the private key for purposes of generating a digital
     * signature.
     * 
     * @param privateKeyPassword The private key password.
     */
    public void setPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
    }
}
