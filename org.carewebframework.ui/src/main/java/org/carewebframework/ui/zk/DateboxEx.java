/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import java.util.Date;

import org.carewebframework.common.DateUtil;

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Datebox;

/**
 * Extends the ZK datebox by enhancing date parsing.
 */
public class DateboxEx extends Datebox {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Parses the input value using enhanced date parsing.
     * 
     * @param value String value to parse.
     * @return A Date object resulting from the parsed value.
     * @throws WrongValueException
     */
    @Override
    protected Object coerceFromString(String value) throws WrongValueException {
        if (value == null || value.length() == 0) {
            return null;
        }
        
        Date date = DateUtil.parseDate(value);
        
        if (date == null) {
            return super.coerceFromString(value);
        }
        
        return date;
    }
    
    /**
     * Returns the value as a string.
     * 
     * @param value Object to transform.
     * @return Object value as a string.
     */
    @Override
    protected String coerceToString(Object value) {
        return value instanceof String ? (String) value : super.coerceToString(value);
    }
    
}
