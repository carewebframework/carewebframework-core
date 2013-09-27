/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.common;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XMLUtil {
    
    public enum TagFormat {
        OPENING, CLOSING, BOTH, EMPTY
    };
    
    /**
     * Parses XML from a string.
     * 
     * @param xml String containing valid XML.
     * @return XML document.
     * @throws Exception
     */
    public static Document parseXMLFromString(String xml) throws Exception {
        return parseXMLFromStream(IOUtils.toInputStream(xml));
    }
    
    /**
     * Parses XML from a list of strings.
     * 
     * @param xml String iterable containing valid XML.
     * @return XML document.
     * @throws Exception
     */
    public static Document parseXMLFromList(Iterable<String> xml) throws Exception {
        return parseXMLFromString(StrUtil.fromList(xml));
    }
    
    /**
     * Parses XML from a file.
     * 
     * @param fileName Full path to a file containing valid XML.
     * @return XML document.
     * @throws Exception
     */
    public static Document parseXMLFromLocation(String fileName) throws Exception {
        return parseXMLFromStream(new FileInputStream(fileName));
    }
    
    /**
     * Parses XML from an input stream.
     * 
     * @param stream Input stream containing valid XML.
     * @return XML document.
     * @throws Exception
     */
    public static Document parseXMLFromStream(InputStream stream) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        stream.close();
        return document;
    }
    
    /**
     * Converts an XML document to a formatted XML string.
     * 
     * @param doc
     * @return Formatted XML document.
     */
    public static String toString(Document doc) {
        if (doc == null) {
            return "";
        }
        
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }
    
    /**
     * Returns the formatted name for the node.
     * 
     * @param node Node to format.
     * @param format Desired format (opening tag, closing tag, empty tag, or both).
     * @return Formatted name.
     */
    public static String formatNodeName(Node node, TagFormat format) {
        StringBuilder sb = new StringBuilder((format == TagFormat.CLOSING ? "</" : "<") + node.getNodeName());
        
        if (format != TagFormat.CLOSING) {
            sb.append(formatAttributes(node));
        }
        
        sb.append(format == TagFormat.EMPTY ? " />" : ">");
        
        if (format == TagFormat.BOTH) {
            sb.append(formatNodeName(node, TagFormat.CLOSING));
        }
        
        return sb.toString();
    }
    
    /**
     * Returns formatted attributes of the node.
     * 
     * @param node
     * @return Formatted attributes.
     */
    public static String formatAttributes(Node node) {
        StringBuilder sb = new StringBuilder();
        NamedNodeMap attrs = node.getAttributes();
        
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            sb.append(' ').append(attr.getNodeName()).append("= '").append(attr.getNodeValue()).append("'");
        }
        
        return sb.toString();
    }
    
    /**
     * Enforce static class.
     */
    private XMLUtil() {
    };
}
