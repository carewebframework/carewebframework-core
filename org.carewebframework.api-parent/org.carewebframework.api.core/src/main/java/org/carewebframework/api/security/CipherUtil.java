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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Encryption/decryption utilities.
 */
public class CipherUtil {
    
    private static final Log log = LogFactory.getLog(CipherUtil.class);
    
    private static final String SIGN_ALGORITHM = "SHA1withRSA";
    
    private static final String CRYPTO_ALGORITHM = "AES/ECB/PKCS5Padding";
    
    /**
     * Returns a key store instance of the specified type from the specified resource.
     * 
     * @param keystoreLocation Path to key store location.
     * @param keystoreType Key store type.
     * @return A key store instance.
     * @throws NoSuchAlgorithmException If algorithm not supported.
     * @throws CertificateException If certificate invalid.
     * @throws IOException If IO exception.
     * @throws KeyStoreException If key store invalid.
     */
    public static KeyStore getKeyStore(String keystoreLocation, String keystoreType) throws NoSuchAlgorithmException,
                                                                                    CertificateException, IOException,
                                                                                    KeyStoreException {
        KeyStore keystore = KeyStore.getInstance(keystoreType);
        InputStream is = CipherUtil.class.getResourceAsStream(keystoreLocation);
        
        if (is == null) {
            is = new FileInputStream(keystoreLocation);
        }
        
        keystore.load(is, null);
        return keystore;
    }
    
    /**
     * Verifies a digitally signed payload.
     * 
     * @param key Public key to verify digital signature.
     * @param base64Signature Digital signature of content.
     * @param content The content that was signed.
     * @param timestamp Optional timestamp for time-sensitive payloads.
     * @param duration Optional validity duration in minutes for time-sensitive payloads.
     * @return True if signature is valid.
     * @throws Exception Unspecified exception.
     */
    public static boolean verify(PublicKey key, String base64Signature, String content, String timestamp, int duration)
                                                                                                                       throws Exception {
        if (key == null || base64Signature == null || content == null || timestamp == null) {
            return false;
        }
        
        try {
            if (timestamp != null && duration > 0) {
                validateTime(timestamp, duration);
            }
            
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initVerify(key);
            signature.update(content.getBytes());
            byte[] signatureBytes = Base64.decodeBase64(base64Signature);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Authentication Exception:verifySignature", e);
            throw e;
        }
    }
    
    /**
     * Returns the digital signature for the specified content.
     * 
     * @param key The private key to sign the content.
     * @param content The content to sign.
     * @return The digital signature.
     * @throws Exception Unspecified exception.
     */
    public static String sign(PrivateKey key, String content) throws Exception {
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(key);
        signature.update(content.getBytes());
        return Base64.encodeBase64String(signature.sign());
    }
    
    /**
     * Validates the timestamp and insures that it falls within the specified duration.
     * 
     * @param timestamp Timestamp in yyyyMMddHHmmssz format.
     * @param duration Validity duration in minutes.
     * @throws Exception Unspecified exception.
     */
    public static void validateTime(String timestamp, int duration) throws Exception {
        Date date = getTimestampFormatter().parse(timestamp);
        long sign_time = date.getTime();
        long now_time = System.currentTimeMillis();
        long diff = now_time - sign_time;
        long min_diff = diff / (60 * 1000);
        
        if (min_diff >= duration) {
            throw new GeneralSecurityException("Authorization token has expired.");
        }
    }
    
    /**
     * Converts a time to timestamp format.
     * 
     * @param time Time to convert, or null for current time.
     * @return Time in timestamp format.
     */
    public static String getTimestamp(Date time) {
        return getTimestampFormatter().format(time == null ? new Date() : time);
    }
    
    /**
     * Returns a formatter capable of producing and parsing timestamps.
     * 
     * @return Formatter for timestamps.
     */
    private static SimpleDateFormat getTimestampFormatter() {
        return new SimpleDateFormat("yyyyMMddHHmmssz");
    }
    
    /**
     * Encrypts the content with the specified key using the default algorithm.
     * 
     * @param key The cryptographic key.
     * @param content The content to encrypt.
     * @return The encrypted content.
     * @throws Exception Unspecified exception.
     */
    public static String encrypt(Key key, String content) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(CRYPTO_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.encodeBase64String(cipher.doFinal(content.getBytes()));
        } catch (Exception e) {
            log.error("Error while encrypting", e);
            throw e;
        }
    }
    
    /**
     * Decrypts the content with the specified key using the default algorithm.
     * 
     * @param key The cryptographic key.
     * @param content The content to decrypt.
     * @return The decrypted content.
     * @throws Exception Unspecified exception.
     */
    public static String decrypt(Key key, String content) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(CRYPTO_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.decodeBase64(content)));
        } catch (Exception e) {
            log.error("Error while decrypting", e);
            throw e;
            
        }
    }
    
    /**
     * Enforce static class.
     */
    private CipherUtil() {
    }
}
