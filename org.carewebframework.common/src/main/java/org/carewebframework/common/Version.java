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
package org.carewebframework.common;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Representation of a standard 4-part version number in the format:
 * <p>
 * <code>major.minor.release.build</code>
 * <p>
 * where any missing components are assumed to be 0.
 */
public class Version implements Comparable<Version> {
    
    public enum VersionPart {
        MAJOR, MINOR, RELEASE, BUILD
    };
    
    private final int seq[] = new int[4];
    
    public Version() {
    
    }
    
    public Version(String value) {
        if (value != null && !value.isEmpty()) {
            String[] pcs = StrUtil.split(value, ".", 4);
            
            for (int i = 0; i < 4; i++) {
                seq[i] = StrUtil.extractInt(pcs[i]);
            }
        }
    }
    
    public Version(int major) {
        this(major, 0, 0, 0);
    }
    
    public Version(int major, int minor) {
        this(major, minor, 0, 0);
        
    }
    
    public Version(int major, int minor, int release) {
        this(major, minor, release, 0);
    }
    
    public Version(int major, int minor, int release, int build) {
        seq[VersionPart.MAJOR.ordinal()] = major;
        seq[VersionPart.MINOR.ordinal()] = minor;
        seq[VersionPart.RELEASE.ordinal()] = release;
        seq[VersionPart.BUILD.ordinal()] = build;
    }
    
    @Override
    public boolean equals(Object v) {
        return v instanceof Version && compareTo((Version) v) == 0;
    }
    
    @Override
    public int compareTo(Version v) {
        int diff = 0;
        
        for (int i = 0; i < 4; i++) {
            diff = seq[i] - v.seq[i];
            
            if (diff != 0) {
                break;
            }
        }
        
        return diff;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(seq).hashCode();
    }
    
    /**
     * Returns the text representation of the version, padding to the minimum length specified.
     * 
     * @param part Pad up to and including this version part. Null means no padding.
     * @return The text representation.
     */
    public String toString(VersionPart part) {
        StringBuilder sb = new StringBuilder();
        int pad = part == null ? -1 : part.ordinal();
        String dot = "";
        
        for (int i = 3; i >= 0; i--) {
            int j = seq[i];
            
            if (j != 0) {
                pad = i;
            } else if (i > pad) {
                continue;
            }
            
            sb.insert(0, dot).insert(0, j);
            dot = ".";
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return toString(null);
    }
    
}
