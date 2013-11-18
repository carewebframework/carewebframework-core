/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.managementsupport.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Class to manipulate/support System properties
 */
@ManagedResource(description = "Manage System properties at Runtime")
public class SystemPropertySupport {
    
    private static final Log log = LogFactory.getLog(SystemPropertySupport.class);
    
    /**
     * Returns the value for a given property key/name
     * 
     * @param propertyName Name/key of System property
     * @return String the system property
     */
    @ManagedOperation(description = "Returns the value for a given property key/name")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "propertyName", description = "Name of property") })
    public static String getSystemProperty(final String propertyName) {
        return System.getProperty(propertyName);
    }
    
    /**
     * Returns a list of System properties {@link Properties#list(PrintStream)}
     * {@link System#getProperties()}
     * 
     * @return String list of properties
     * @throws IOException Thrown by OutputStream errors
     * @throws ClassCastException Thrown by {@link Properties#list(PrintStream)}
     */
    @ManagedOperation(description = "List all current System propeties")
    public static String listSystemProperties() throws IOException, ClassCastException {
        String propertiesDump = null;
        OutputStream baos = null;
        PrintStream pStream = null;
        try {
            baos = new ByteArrayOutputStream();
            pStream = new PrintStream(baos);
            final Properties properties = System.getProperties();
            properties.list(pStream);
            propertiesDump = baos.toString();
            return propertiesDump;
        } catch (final ClassCastException cce) {
            log.error("Properties.list threw ClassCastException", cce);
            throw cce;
        } finally {
            try {
                pStream.close();
                baos.close();
            } catch (final IOException ioe) {
                log.error("IOException occured returning properties list");
                throw ioe;
            }
        }
    }
    
    /**
     * Sets a system property.
     * 
     * @param propertyName Property key/name
     * @param propertyValue Value for set key/name
     */
    @ManagedOperation(description = "Set System property")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "propertyName", description = "Name of property"),
            @ManagedOperationParameter(name = "propertyValue", description = "Value of property") })
    public static void setSystemProperty(final String propertyName, final String propertyValue) {
        System.setProperty(propertyName, propertyValue);
    }
}
