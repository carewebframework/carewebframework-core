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

import org.zkoss.util.Dates;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.SimpleConstraint;

/**
 * Extends the ZK datebox by enhancing date parsing.
 */
public class DateboxEx extends Datebox {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Uses DateUtil.parseDate for date parsing.
     */
    private static class SimpleDateConstraint extends SimpleConstraint {
        
        private static final long serialVersionUID = 1L;
        
        private Date _beg, _end;
        
        public SimpleDateConstraint(String constraint) {
            super(constraint);
            fixConstraint();
        }
        
        @Override
        protected int parseConstraint(String constraint) throws UiException {
            if (constraint.startsWith("between")) {
                int j = constraint.indexOf("and", 7);
                
                if (j < 0) {
                    throw new UiException("Constraint syntax error: " + constraint);
                }
                
                _beg = parseDate(constraint.substring(7, j));
                _end = parseDate(constraint.substring(j + 3));
                
                if (_beg.compareTo(_end) > 0) {
                    Date d = _beg;
                    _beg = _end;
                    _end = d;
                }
            } else if (constraint.startsWith("before") && !constraint.startsWith("before_")) {
                _end = parseDate(constraint.substring(6));
            } else if (constraint.startsWith("after") && !constraint.startsWith("after_")) {
                _beg = parseDate(constraint.substring(5));
            } else {
                return super.parseConstraint(constraint);
            }
            
            return 0;
        }
        
        private Date parseDate(String val) throws UiException {
            Date date = DateUtil.parseDate(val.trim());
            
            if (date == null) {
                throw new UiException("Not a recognized date: " + val);
            }
            
            return date;
        }
        
        private void fixConstraint() {
            if ((_flags & NO_FUTURE) != 0 && _end == null) {
                _end = Dates.today();
            }
            if ((_flags & NO_PAST) != 0 && _beg == null) {
                _beg = Dates.today();
            }
        }
        
    }
    
    public DateboxEx() {
        super();
        setFormat(DateUtil.Format.WITHOUT_TIME.getPattern());
    }
    
    @Override
    public void setConstraint(String constr) {
        setConstraint(constr != null ? new SimpleDateConstraint(constr) : null); // Bug 2564298
    }
    
    /**
     * Parses the input value using enhanced date parsing.
     * 
     * @param value String value to parse.
     * @return A Date object resulting from the parsed value.
     * @throws WrongValueException If invalid input value.
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
