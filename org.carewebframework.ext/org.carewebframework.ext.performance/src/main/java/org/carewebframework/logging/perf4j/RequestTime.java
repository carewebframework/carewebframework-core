/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.logging.perf4j;

/**
 * Enum to specify a desired interval from request processing timing data. Values are interpreted as
 * follows:
 * <UL>
 * <LI>SERVER - Time spent by the server processing a request.</LI>
 * <LI>CLIENT - Time spent by the client (browser) processing a request.</LI>
 * <LI>NETWORK - Time spent in network transport (roundtrip).</LI>
 * <LI>TOTAL - Overall time spent servicing a request (sum of all of the above).</LI>
 * </UL>
 */
public enum RequestTime {
    SERVER, CLIENT, NETWORK, TOTAL;
}
