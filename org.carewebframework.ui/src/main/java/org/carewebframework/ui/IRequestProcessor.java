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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <b>Implementations of RequestProcessor are assumed to be stateless and thread-safe.</b> Servlets
 * typically run on multithreaded servers, so be aware that a servlet must handle concurrent
 * requests and be careful to synchronize access to shared resources. Shared resources include
 * in-memory data such as instance or class variables and external objects such as files, database
 * connections, and network connections.
 */
public interface IRequestProcessor {
    
    void process(HttpServletRequest req, HttpServletResponse res) throws Exception;
    
}
