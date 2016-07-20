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
package org.carewebframework.maven.plugin.theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.resource.IResource;
import org.carewebframework.maven.plugin.transform.AbstractTransform;

import org.codehaus.plexus.util.StringUtils;

/**
 * Applies a map template to the source CSS to generate additional CSS.
 */
public class CSSTransform extends AbstractTransform {
    
    public CSSTransform(BaseMojo mojo) {
        super(mojo);
    }
    
    private static final String DELIM = "|";
    
    private final Map<String, String> srcMap = new LinkedHashMap<>();
    
    private final Map<String, String> defMap = new LinkedHashMap<>();
    
    @Override
    public void transform(IResource resource, OutputStream outputStream) throws Exception {
        CSSResource css = (CSSResource) resource;
        File mapper = css.getMapper();
        
        if (mapper == null) {
            super.transform(resource, outputStream);
            return;
        }
        
        try (InputStream in = new FileInputStream(mapper)) {
            LineIterator lines = IOUtils.lineIterator(in, "UTF-8");
            String line = "";
            
            while (lines.hasNext()) {
                line += lines.nextLine();
                
                if (line.endsWith("\\")) {
                    line = StringUtils.left(line, line.length() - 1);
                } else {
                    addMapEntry(line);
                    line = "";
                }
            }
            
            addMapEntry(line);
        }
        
        super.transform(resource, outputStream);
    }
    
    /**
     * Parses and adds the entry to the source or default map.
     * 
     * @param s String entry.
     */
    private void addMapEntry(String s) {
        String[] pcs = s.split("\\=", 2);
        
        if (pcs.length == 2) {
            String sel = pcs[0].trim();
            boolean deflt = sel.startsWith("!");
            Map<String, String> map = deflt ? defMap : srcMap;
            sel = deflt ? sel.substring(1) : sel;
            
            if (map.containsKey(sel)) {
                map.put(sel, map.get(sel) + pcs[1]);
            } else {
                map.put(sel, pcs[1]);
            }
        }
    }
    
    /**
     * Checks the selector for a match in either the source map or the reference map. If found in
     * the source map, it is moved to the reference map and added to the matches set. If found in
     * the reference map, it is added to the matches set. If not found, no action is taken.
     * 
     * @param sel The CSS selector.
     * @param matches The matches set.
     * @param refMap The map of referenced selectors.
     */
    private void checkForMatch(String sel, Set<String> matches, Map<String, String> refMap) {
        sel = sel.trim();
        
        if (srcMap.containsKey(sel)) {
            matches.add(sel);
            defMap.remove(sel);
            refMap.put(sel, srcMap.remove(sel));
        } else if (refMap.containsKey(sel)) {
            matches.add(sel);
        }
    }
    
    /**
     * Writes the map contents to the output stream.
     * 
     * @param map Map to write.
     * @throws IOException IO exception.
     */
    private void writeMap(Map<String, String> map, OutputStream outputStream) throws IOException {
        for (String entry : map.values()) {
            if (entry.contains(DELIM)) {
                mojo.getLog().warn("Output contains unresolved reference: " + entry);
            } else {
                outputStream.write(entry.getBytes());
            }
        }
    }
    
    @Override
    public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
        int c = 0;
        int state = 0;
        StringBuilder sb = new StringBuilder();
        Set<String> matches = new HashSet<>();
        Map<String, String> refMap = new LinkedHashMap<>();
        Map<String, String> styles = new HashMap<>();
        String prop = null;
        checkForMatch("@before@", matches, refMap);
        
        while (c != -1) {
            c = inputStream.read();
            
            if (c == -1) {
                state = -1;
            } else {
                outputStream.write(c);
            }
            
            switch (state) {
                case -1: // Process template
                    for (String match : matches) {
                        String template = refMap.get(match);
                        
                        if (!template.contains(DELIM)) {
                            continue;
                        }
                        
                        for (Entry<String, String> entry : styles.entrySet()) {
                            String replace = DELIM + entry.getKey() + DELIM;
                            template = template.replace(replace, entry.getValue());
                        }
                        
                        refMap.put(match, template);
                    }
                    
                    matches.clear();
                    styles.clear();
                    
                    if (c == -1) {
                        continue;
                    }
                    
                    state = 0;
                    // Fall through intended
                    
                case 0: // Baseline
                    switch (c) {
                        case '/': // Possible comment start
                            state = 1;
                            break;
                            
                        case '<': // Directive start
                            state = 10;
                            break;
                            
                        case '{': // Declaration block
                            state = 20;
                            break;
                            
                        case ',': // Selector separator
                            break;
                            
                        case '}': // Don't know why these occur, but ignore them.
                            continue;
                            
                        default:
                            sb.append((char) c);
                            continue;
                    }
                    
                    checkForMatch(sb.toString(), matches, refMap);
                    sb.setLength(0);
                    break;
                    
                case 1: // Possible comment
                    state = c == '*' ? 2 : 0;
                    break;
                    
                case 2: // Possible comment end
                    state = c == '*' ? 3 : state;
                    break;
                    
                case 3: // Comment end
                    state = c == '/' ? 0 : c == '*' ? state : 2;
                    break;
                    
                case 10: // Directive end
                    state = c == '>' ? 0 : state;
                    break;
                    
                case 20: // Declaration block
                    switch (c) {
                        case '}': // End block
                            state = -1;
                            break;
                            
                        case ':': // Start of property value
                            prop = sb.toString().trim();
                            sb.setLength(0);
                            state = 30;
                            break;
                            
                        default: // Build property name
                            sb.append((char) c);
                            break;
                    }
                    break;
                    
                case 30: // Property value
                    switch (c) {
                        case ';': // Property separator
                        case '}': // Block terminator
                            styles.put(prop, sb.toString());
                            sb.setLength(0);
                            state = c == ';' ? 20 : -1;
                            break;
                            
                        default: // Build property value
                            sb.append((char) c);
                            break;
                    }
                    break;
            }
            
        }
        
        checkForMatch("@after@", matches, defMap);
        writeMap(refMap, outputStream);
        writeMap(defMap, outputStream);
        
        if (!srcMap.isEmpty()) {
            mojo.getLog().warn("The following entries failed to match and were ignored:");
            
            for (Entry<String, String> entry : srcMap.entrySet()) {
                mojo.getLog().warn("   " + entry);
            }
        }
    }
}
