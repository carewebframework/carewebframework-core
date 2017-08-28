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
package org.carewebframework.api.alias;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.digester.SimpleRegexMatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.fujion.common.StrUtil;

/**
 * Represents a specific alias type (e.g., property or authority aliases).
 */
public class AliasType {
    
    private static final Log log = LogFactory.getLog(AliasType.class);
    
    private static final String WILDCARD_DELIM_REGEX = "((?<=[\\*,\\?])|(?=[\\*,\\?]))";
    
    private static final SimpleRegexMatcher matcher = new SimpleRegexMatcher();
    
    private final String name;
    
    private final Map<String, String> aliasMap = new ConcurrentHashMap<>();
    
    private final Map<String, String> wildcardMap = new ConcurrentHashMap<>();
    
    protected AliasType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String get(String local) {
        String result = aliasMap.get(local);
        
        if (result == null) {
            for (Entry<String, String> entry : wildcardMap.entrySet()) {
                String wc = entry.getKey();
                
                if (matcher.match(local, wc)) {
                    result = transformKey(local, wc, entry.getValue());
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Registers an alias for a key.
     * 
     * @param local Local name.
     * @param alias Alias for the key. A null value removes any existing alias.
     */
    public void register(String local, String alias) {
        Map<String, String> map = local.contains("*") || local.contains("?") ? wildcardMap : aliasMap;
        
        if (alias == null) {
            map.remove(local);
            return;
        }
        
        String oldAlias = map.get(local);
        
        if (oldAlias != null) {
            if (oldAlias.equals(alias)) {
                return;
            }
            
            if (log.isInfoEnabled()) {
                log.info(StrUtil.formatMessage("Replaced %s alias for '%s':  old value ='%s', new value ='%s'.", getName(),
                    local, oldAlias, alias));
            }
        }
        map.put(local, alias);
    }
    
    /**
     * Uses the source and target wildcard masks to transform an input key.
     * 
     * @param key The input key.
     * @param src The source wildcard mask.
     * @param tgt The target wildcard mask.
     * @return The transformed key.
     */
    private String transformKey(String key, String src, String tgt) {
        StringBuilder sb = new StringBuilder();
        String[] srcTokens = src.split(WILDCARD_DELIM_REGEX);
        String[] tgtTokens = tgt.split(WILDCARD_DELIM_REGEX);
        int len = Math.max(srcTokens.length, tgtTokens.length);
        int pos = 0;
        int start = 0;
        
        for (int i = 0; i <= len; i++) {
            String srcx = i >= srcTokens.length ? "" : srcTokens[i];
            String tgtx = i >= tgtTokens.length ? "" : tgtTokens[i];
            pos = i == len ? key.length() : pos;
            
            if ("*".equals(srcx) || "?".equals(srcx)) {
                start = pos;
            } else {
                pos = key.indexOf(srcx, pos);
                
                if (pos > start) {
                    sb.append(key.substring(start, pos));
                }
                
                start = pos += srcx.length();
                sb.append(tgtx);
            }
            
        }
        
        return sb.toString();
    }
    
};
