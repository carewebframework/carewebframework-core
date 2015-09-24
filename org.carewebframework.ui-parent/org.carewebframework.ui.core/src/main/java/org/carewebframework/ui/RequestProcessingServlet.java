/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Generic request handling servlet. The idea is that component developers could define
 * <code>RequestProcessor</code> beans and then invoke the framework's RequestProcessingServlet via
 * the servlet's mapping (reference framework deployment descriptor for mapping, web.xml). This does
 * not require component developers to create multiple subclasses of HttpServlet. This servlet
 * expects a parameter of {@value #REQUEST_PROCESSOR_BEAN} passed on the request. The
 * {@value #REQUEST_PROCESSOR_BEAN} parameter value is the name of the bean defined in the Root
 * ApplicationContext bean registry. The bean must be an instance of {@link IRequestProcessor}
 * Servlets typically run on multithreaded servers, so be aware that a servlet must handle
 * concurrent requests and be careful to synchronize access to shared resources. Shared resources
 * include in-memory data such as instance or class variables and external objects such as files,
 * database connections, and network connections. {@link IRequestProcessor} is assumed to be
 * stateless and thread-safe.
 */
public class RequestProcessingServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(RequestProcessingServlet.class);
    
    protected static final String REQUEST_PROCESSOR_BEAN = "requestProcessorBean";
    
    private static final String EXC_REC_PROC = "@cwf.error.ui.req.proc";
    
    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
                                                                                           IOException {
        processRequest(req, res);
    }
    
    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException,
                                                                                              IOException {
        processRequest(req, res);
    }
    
    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException,
                                                                                           IOException {
        processRequest(req, res);
    }
    
    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
                                                                                            IOException {
        processRequest(req, res);
    }
    
    /**
     * Method to centralize the processing of various Http request methods (e.g. GET, PUT, POST,
     * DELETE)
     * 
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @throws IllegalArgumentException If there is not the appropriate request parameter or if the
     *             bean found is not of type RequestProcessor
     * @throws ServletException Servlet exception.
     * @throws IOException IO Exception.
     * @throws BeansException If the bean could not be obtained
     * @throws NoSuchBeanDefinitionException If there is no bean definition with the specified name
     * @throws BeanNotOfRequiredTypeException If bean found is not a RequestProcessor implementation
     * @throws RequestProcessingException Request processing exception.
     */
    protected final void processRequest(HttpServletRequest req, HttpServletResponse res)
                                                                                                    throws ServletException,
                                                                                                    IOException,
                                                                                                    NoSuchBeanDefinitionException,
                                                                                                    BeanNotOfRequiredTypeException,
                                                                                                    RequestProcessingException {
        String processorBeanName = req.getParameter(REQUEST_PROCESSOR_BEAN);
        if (processorBeanName == null) {
            throw new IllegalArgumentException(getClass() + " expects the parameter " + REQUEST_PROCESSOR_BEAN
                    + ", which is an instance of org.carewebframework.ui.RequestProcessor");
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Request Method: " + req.getMethod());
            log.debug("Attempting to lookup the following bean in the registry: " + processorBeanName);
        }
        
        IRequestProcessor processor = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext())
                .getBean(processorBeanName, IRequestProcessor.class);
        try {
            processor.process(req, res);
        } catch (Exception e) {
            throw new RequestProcessingException(EXC_REC_PROC, e, processor.getClass().getName());
        }
    }
    
}
