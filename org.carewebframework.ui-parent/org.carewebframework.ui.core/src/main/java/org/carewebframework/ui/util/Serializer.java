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
package org.carewebframework.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Serialization to and from file.
 */
public class Serializer {
    
    private static final Log log = LogFactory.getLog(Serializer.class);
    
    private SerializerConfig serializerConfig;
    
    public Serializer(SerializerConfig serializerConfig) {
        this.serializerConfig = serializerConfig;
    }
    
    /**
     * Serializes an object. Uses
     * {@link org.carewebframework.ui.util.SerializerConfig#getSerializationFileLocation()} for
     * location to write file
     * 
     * @param serializationFileName File to receive serialized object.
     * @param object Object to serialize.
     * @throws SerializationException If serialization error.
     * @throws IOException If IO exception.
     */
    public void serializeObject(String serializationFileName, Object object) throws SerializationException, IOException {
        File file = new File(getSerializerConfig().getSerializationFileLocation(), serializationFileName);
        if (!file.isFile()) {
            File dir = new File(getSerializerConfig().getSerializationFileLocation());
            if (!dir.isDirectory()) {
                if (log.isInfoEnabled()) {
                    log.info("Directory not found, attempting to mkdir: " + dir.getAbsolutePath());
                }
                if (!dir.mkdir()) {
                    log.warn("mkdir returned false...???");
                    //throw new IOException("Could Not Make Directory:" + dir.getAbsolutePath());
                }
            }
            
            if (log.isInfoEnabled()) {
                log.info(file.getAbsolutePath() + " not found, attempting to create it.");
            }
            if (!file.createNewFile()) {
                throw new IOException("Could Not Create File:" + file.getAbsolutePath());
            }
            if (log.isInfoEnabled()) {
                log.info("Created file: " + file.getAbsolutePath());
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Serializing Object: " + file.getAbsolutePath());
        }
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        
        try {
            fos = new FileOutputStream(file);
            out = new ObjectOutputStream(fos);
            out.writeObject(object);
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SerializationException(e.getMessage(), e);
        }
    }
    
    /**
     * Deserializes object from a given file Uses
     * {@link org.carewebframework.ui.util.SerializerConfig#getSerializationFileLocation()} for
     * location to read file
     * 
     * @param serializationFileName File containing serialized object.
     * @return Deserialized object.
     * @throws SerializationException If exception on deserialization.
     * @throws FileNotFoundException If file not found.
     */
    public Object deserializeObject(String serializationFileName) throws SerializationException, FileNotFoundException {
        File file = new File(getSerializerConfig().getSerializationFileLocation(), serializationFileName);
        if (log.isTraceEnabled()) {
            log.trace("Deserializing Object: " + file.getAbsolutePath());
        }
        FileInputStream fis = null;
        ObjectInputStream in = null;
        Object o = null;
        
        if (!file.isFile()) {
            throw new FileNotFoundException("File[" + file.getAbsolutePath() + "] not a file");
        }
        try {
            fis = new FileInputStream(file);
            in = new ObjectInputStream(fis);
            o = in.readObject();
            in.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SerializationException(e.getMessage(), e);
        }
        return o;
    }
    
    public SerializerConfig getSerializerConfig() {
        return serializerConfig;
    }
    
    public void setSerializerConfig(SerializerConfig serializerConfig) {
        this.serializerConfig = serializerConfig;
    }
}
