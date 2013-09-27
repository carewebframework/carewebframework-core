/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.security;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkWebSupport;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for security implementation
 */
public class SecurityTest {
    
    /**
     * Algorithm for encoding/decoding cookie values.
     * <p>
     * Encoding: Base64 encode, then URLEncode
     * </p>
     * <p>
     * Decoding: URLDecode, Base64 decode
     * </p>
     * <i>Note: I intentionally chose to use URLEncoder/Decoder over the options URL_SAFE route of
     * Base64 class.</i>
     * <p>
     * With Version 0 cookies, values should not contain white space, brackets, parentheses, equals
     * signs, commas, double quotes, slashes, question marks, at signs, colons, and semicolons.
     * Empty values may not behave the same way on all browsers.
     * </p>
     * 
     * @throws Exception when error occurs encoding or decoding
     */
    @Test
    public void validateCookieEncodingAndDecodingAlgorithm() throws Exception {
        applyAlgorithm("Application logged out.");
        applyAlgorithm("ILLEGAL[$]=,\"//?@: Version0  ; COOKIE Chars====");
    }
    
    private void applyAlgorithm(final String plainText) throws Exception {
        final String base64Encoded = Base64.encodeBase64String(plainText.getBytes());
        final String urlEncoded = URLEncoder.encode(base64Encoded, StrUtil.CHARSET);
        final String decoded = new String(Base64.decodeBase64(URLDecoder.decode(urlEncoded, StrUtil.CHARSET)));
        Assert.assertEquals(plainText, decoded);
        
        final String encodedByAPI = FrameworkWebSupport.encodeCookieValue(plainText);
        Assert.assertEquals(plainText, FrameworkWebSupport.decodeCookieValue(encodedByAPI));
    }
}
