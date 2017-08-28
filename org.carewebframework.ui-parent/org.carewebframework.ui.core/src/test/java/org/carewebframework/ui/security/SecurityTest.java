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
package org.carewebframework.ui.security;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.fujion.common.StrUtil;
import org.fujion.core.WebUtil;
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

    private void applyAlgorithm(String plainText) throws Exception {
        String base64Encoded = Base64.encodeBase64String(plainText.getBytes());
        String urlEncoded = URLEncoder.encode(base64Encoded, StrUtil.UTF8_STR);
        String decoded = new String(Base64.decodeBase64(URLDecoder.decode(urlEncoded, StrUtil.UTF8_STR)));
        Assert.assertEquals(plainText, decoded);
        String encodedByAPI = WebUtil.encodeCookieValue(plainText);
        Assert.assertEquals(plainText, WebUtil.decodeCookieValue(encodedByAPI));
    }
}
