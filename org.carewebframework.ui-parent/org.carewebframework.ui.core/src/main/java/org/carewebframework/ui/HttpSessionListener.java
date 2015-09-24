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

import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class extends org.zkoss.zk.ui.http.HttpSessionListener simply to log native HttpSessionListener
 * and ServletContextListener events, prior to delegating to ZK.
 */
public class HttpSessionListener extends org.zkoss.zk.ui.http.HttpSessionListener {
    
    private static final Log log = LogFactory.getLog(HttpSessionListener.class);
    
    /**
     * @see org.zkoss.zk.ui.http.HttpSessionListener23#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        log.info(String.format("Native HttpSession Creation Event : %s : %s",event.getSession().getId(), event));
        super.sessionCreated(event);
    }
    
    /**
     * @see org.zkoss.zk.ui.http.HttpSessionListener23#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        log.info(String.format("Native HttpSession Destruction Event : %s : %s", event.getSession().getId(), event));
        super.sessionDestroyed(event);
    }
    
    /**
     * @see org.zkoss.zk.ui.http.HttpSessionListener23#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        log.info("Native ServletContext Creation Event : " + event);
        super.contextInitialized(event);
    }
    
    /**
     * @see org.zkoss.zk.ui.http.HttpSessionListener23#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        log.info("Native ServletContext Destruction Event : " + event);
        super.contextDestroyed(event);
    }
    
}
