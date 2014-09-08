/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell;

import java.io.IOException;

import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.common.XMLUtil;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.Resource;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Spring xml configuration file parser extension. This is an abstract base class for implementing
 * CareWeb extensions to the Spring configuration xml schema.
 */
public class BaseXmlParser extends AbstractSingleBeanDefinitionParser {
    
    /**
     * Find the child element whose tag matches the specified tag name.
     * 
     * @param tagName Tag name to locate.
     * @param element Parent element whose children are to be searched.
     * @return The matching node (first occurrence only) or null if not found.
     */
    protected Element findTag(String tagName, Element element) {
        Node result = element.getFirstChild();
        
        while (result != null) {
            if (result instanceof Element
                    && (tagName.equals(((Element) result).getNodeName()) || tagName
                            .equals(((Element) result).getLocalName()))) {
                break;
            }
            result = result.getNextSibling();
        }
        
        return (Element) result;
    }
    
    /**
     * Returns the children under the specified tag. Compensates for namespace usage.
     * 
     * @param tagName Name of tag whose children are sought.
     * @param element Element to search for tag.
     * @return Node list containing children of tag.
     */
    protected NodeList getTagChildren(String tagName, Element element) {
        return element.getNamespaceURI() == null ? element.getElementsByTagName(tagName) : element.getElementsByTagNameNS(
            element.getNamespaceURI(), tagName);
    }
    
    /**
     * Adds all attributes of the specified elements as properties in the current builder.
     * 
     * @param element Element whose attributes are to be added.
     * @param builder Target builder.
     */
    protected void addProperties(Element element, BeanDefinitionBuilder builder) {
        NamedNodeMap attributes = element.getAttributes();
        
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            String attrName = getNodeName(node);
            attrName = "class".equals(attrName) ? "clazz" : attrName;
            builder.addPropertyValue(attrName, node.getNodeValue());
        }
    }
    
    /**
     * Returns the node name. First tries local name. If this is null, returns instead the full node
     * name.
     * 
     * @param node DOM node to examine.
     * @return Name of the node.
     */
    protected String getNodeName(Node node) {
        String result = node.getLocalName();
        return result == null ? node.getNodeName() : result;
    }
    
    /**
     * Parses an xml extension from an xml string.
     * 
     * @param xml XML containing the extension.
     * @param tagName The top level tag name.
     * @return Result of the parsed extension.
     * @throws Exception Unspecified exception.
     */
    protected Object fromXml(String xml, String tagName) throws Exception {
        Document document = XMLUtil.parseXMLFromString(xml);
        NodeList nodeList = document.getElementsByTagName(tagName);
        
        if (nodeList == null || nodeList.getLength() != 1) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "Top level tag '" + tagName + "' was not found.");
        }
        
        Element element = (Element) nodeList.item(0);
        Class<?> beanClass = getBeanClass(element);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
        doParse(element, builder);
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.setParentBeanFactory(SpringUtil.getAppContext());
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        factory.registerBeanDefinition(tagName, beanDefinition);
        return factory.getBean(tagName);
    }
    
    /**
     * Return the path of the resource being parsed.
     * 
     * @param parserContext The current parser context.
     * @return The resource being parsed, or null if cannot be determined.
     */
    protected String getResourcePath(ParserContext parserContext) {
        if (parserContext != null) {
            try {
                Resource resource = parserContext.getReaderContext().getResource();
                return resource == null ? null : resource.getURL().getPath();
            } catch (IOException e) {}
        }
        
        return null;
    }
}
