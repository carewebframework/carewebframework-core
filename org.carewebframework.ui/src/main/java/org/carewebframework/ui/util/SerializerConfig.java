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

/**
 */
public class SerializerConfig {
    
    private String serializationFileLocation;
    
    public SerializerConfig() {
    }
    
    /**
     * Location of serialized file(s) and/or configuration
     * 
     * @param serializationFileLocation
     */
    public SerializerConfig(String serializationFileLocation) {
        this.serializationFileLocation = serializationFileLocation;
    }
    
    public String getSerializationFileLocation() {
        return serializationFileLocation;
    }
    
    public void setSerializationFileLocation(String serializationFileLocation) {
        this.serializationFileLocation = serializationFileLocation;
    }
    
}
