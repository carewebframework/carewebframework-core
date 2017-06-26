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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
    }
    
    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    static {
        try {
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {}
    }
    
    /**
     * Parses XML from a string.
     *
     * @param xml String containing valid XML.
     * @return XML document.
     * @throws Exception Unspecified exception.
     */
    public static Document parseXMLFromString(String xml) throws Exception {
        return parseXMLFromStream(IOUtils.toInputStream(xml, StandardCharsets.UTF_8));
    }

    /**
     * Parses XML from a list of strings.
     *
     * @param xml String iterable containing valid XML.
     * @return XML document.
     * @throws Exception Unspecified exception.
     */
    public static Document parseXMLFromList(Iterable<String> xml) throws Exception {
        return parseXMLFromString(StrUtil.fromList(xml));
    }

    /**
     * Parses XML from a file.
     *
     * @param filePath Full path to a file containing valid XML.
     * @return XML document.
     * @throws Exception Unspecified exception.
     */
    public static Document parseXMLFromLocation(String filePath) throws Exception {
        return parseXMLFromStream(new FileInputStream(filePath));
    }

    /**
     * Parses XML from an input stream.
     *
     * @param stream Input stream containing valid XML.
     * @return XML document.
     * @throws Exception Unspecified exception.
     */
    public static Document parseXMLFromStream(InputStream stream) throws Exception {
        Document document = documentBuilderFactory.newDocumentBuilder().parse(stream);
        stream.close();
        return document;
    }

    /**
     * Converts an XML document to a formatted XML string.
     *
     * @param doc The document to format.
     * @return Formatted XML document.
     */
    public static String toString(Document doc) {
        return toString(doc, 4);
    }

    /**
     * Converts an XML document to a formatted XML string.
     *
     * @param doc The document to format.
     * @param indent Number of characters to indent.
     * @return Formatted XML document.
     */
    public static String toString(Document doc, int indent) {
        if (doc == null) {
            return "";
        }

        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();

            try {
                tf.setAttribute("indent-number", indent);
            } catch (IllegalArgumentException e) {
                // Ignore if not supported.
            }

            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
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
     * @param node The node.
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
    }
}
