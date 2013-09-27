/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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
     * @param serializationFileName
     * @param object
     * @throws SerializationException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void serializeObject(String serializationFileName, Object object) throws SerializationException,
                                                                            FileNotFoundException, IOException {
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
     * @param serializationFileName
     * @return Deserialized object.
     * @throws SerializationException
     * @throws FileNotFoundException
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
