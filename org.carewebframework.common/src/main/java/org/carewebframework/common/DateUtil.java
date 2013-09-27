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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * Utility methods for managing dates.
 */
public class DateUtil {
    
    private static ThreadLocal<DecimalFormat> decimalFormat = new ThreadLocal<DecimalFormat>() {
        
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("##0.##");
        }
    };
    
    /**
     * Interface for accessing and setting user's timezone
     */
    public static interface ITimeZoneAccessor {
        
        /**
         * Returns the current time zone.
         * 
         * @return TimeZone instance
         */
        TimeZone getTimeZone();
        
        /**
         * Sets the current time zone.
         * 
         * @param timezone New time zone.
         */
        void setTimeZone(TimeZone timezone);
        
    }
    
    public static ITimeZoneAccessor localTimeZone = new ITimeZoneAccessor() {
        
        @Override
        public TimeZone getTimeZone() {
            return TimeZone.getDefault();
        }
        
        @Override
        public void setTimeZone(TimeZone timezone) {
            TimeZone.setDefault(timezone);
        }
        
    };
    
    private static final String HL7_DATE_PATTERN = "yyyyMMdd";
    
    private static final String HL7_DATE_TIME_PATTERN = HL7_DATE_PATTERN + "HHmmssz";
    
    private static final String[] DATE_PATTERNS = new String[] { "dd-MMM-yyyy HH:mm zzz", "dd-MMM-yyyy HH:mm",
            "dd-MMM-yyyy", HL7_DATE_PATTERN, HL7_DATE_TIME_PATTERN };
    
    private static final String UNKNOWN = "Unknown";
    
    /*
     * Defines a regular expression pattern representing a permissible
     * extended style date that can be converted into a traditional date.
     *
     * Such a value starts with either 't' or 'n', and then optionally plus or
     * minus a numeric value of 'd' (days), 'm' (months), or 'y' (years).
     */
    private static final Pattern PATTERN_EXT_DATE = Pattern
            .compile("^\\s*[t|n]{1}\\s*([+|-]{1}\\s*[\\d]*\\s*[d|m|y]?)?\\s*$");
    
    /*
     * Defines a regular expression pattern representing a value ending in one
     * of the acceptable extended style date units (d, m, or y).
     */
    private static final Pattern PATTERN_SPECIFIES_UNITS = Pattern.compile("^.*[d|m|y]$");
    
    private static final double[] SECONDS_FP = new double[] { 31557600.0, 2592000.0, 604800.0, 86400.0, 3600.0, 60.0, 1.0 };
    
    private static final long[] SECONDS_LG = new long[] { 31557600000L, 2592000000L, 604800000L, 86400000L, 3600000L,
            60000L, 1000L, 1L };
    
    public static String[][] DURATION_UNITS = new String[][] { { "year", "years", "yr", "yrs" },
            { "month", "months", "mo", "mos" }, { "week", "weeks", "wk", "wks" }, { "day", "days", "day", "days" },
            { "hour", "hours", "hr", "hrs" }, { "minute", "minutes", "min", "mins" },
            { "second", "seconds", "sec", "secs" }, { "millisecond", "milliseconds", "ms", "ms" } };
    
    public enum Accuracy {
        YEARS, MONTHS, WEEKS, DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS
    };
    
    /**
     * <p>
     * Convert a string value to a date/time. Attempts to convert using the four locale-specific
     * date formats (FULL, LONG, MEDIUM, SHORT). If these fail, looks to see if T+/-offset or
     * N+/-offset is used.
     * </p>
     * <p>
     * TODO: probably we can make the "Java parse" portion a bit smarter by using a better variety
     * of formats, maybe to catch Euro-style input as well.
     * </p>
     * <p>
     * TODO: probably we can add something like "t+d" or "t-y" as valid cases; in these scenarios,
     * the coefficient was omitted and could be defaulted to 1.
     * </p>
     * 
     * @param s <code>String</code> containing value to be converted.
     * @return <code>Date</code> object corresponding to the input value, or <code>null</code> if
     *         the parsing failed to resolve a valid Date.
     */
    public static Date parseDate(String s) {
        Date result = null;
        
        if (s != null && !s.isEmpty()) {
            s = s.toLowerCase(); // make lc
            
            if ((PATTERN_EXT_DATE.matcher(s)).matches()) { // is an extended date?
                try {
                    s = s.replaceAll("\\s+", ""); // strip space since they not
                    // delim
                    String _k = s.substring(1); // _k will ultimately be the
                    // multiplier value
                    char k = 'd'; // k = d (default), m, or y
                    if (1 == s.length()) {
                        _k = "0";
                    } else {
                        if ((PATTERN_SPECIFIES_UNITS.matcher(s)).matches()) { // has
                            // valid
                            // extended
                            // date
                            // units?
                            _k = s.substring(1, s.length() - 1);
                            k = s.charAt(s.length() - 1);
                        }
                    }
                    if ('+' == _k.charAt(0)) { // if it is positive coefficient...
                        _k = _k.substring(1); // ...clip the sign! parse *hates*
                    }
                    // that
                    int offset = Integer.parseInt(_k);
                    switch (k) {
                        case 'y':
                            offset *= 365;
                            break;
                        case 'm':
                            offset *= 30;
                            break;
                    }
                    Calendar c = new GregorianCalendar();
                    c.setLenient(false);
                    c.add(Calendar.DAY_OF_YEAR, offset);
                    result = c.getTime();
                    if ('t' == s.charAt(0)) {// if 't' format, strip time...
                        result = stripTime(result); // ...it's a no-op for 'n'
                    }
                    // format
                } catch (Exception e) {
                    return null; // found un-parseable date (e.g. t-y)
                }
            } else {
                result = tryParse(s);
                
                if (result != null) {
                    return result;
                }
                
                s = s.replaceAll("[\\.|-]", "/"); // dots, dashes to slashes
                result = tryParse(s);
                
                if (result != null) {
                    return result;
                }
                
                s = s.replaceAll("\\s", "/"); // last chance to parse: spaces to
                result = tryParse(s); // slashes!
            }
        }
        
        return result;
    }
    
    /**
     * Attempts to parse a string containing a date representation using several different date
     * patterns.
     * 
     * @param value String to parse
     * @return If the parsing was successful, returns the date value represented by the input value.
     *         Otherwise, returns null.
     */
    private static Date tryParse(final String value) {
        try {
            return DateUtils.parseDate(value, DATE_PATTERNS);
        } catch (Exception e) {}
        
        for (int i = 3; i >= 0; i--) {
            try {
                return DateFormat.getDateInstance(i).parse(value);
            } catch (Exception e) {}
        }
        
        return null;
    }
    
    /**
     * Clones a date.
     * 
     * @param date Date to clone.
     * @return A clone of the original date, or null if the original date was null.
     */
    public static Date cloneDate(Date date) {
        return date == null ? null : new Date(date.getTime());
    }
    
    /**
     * Adds specified number of days to date and, optionally strips the time component.
     * 
     * @param date Date value to process.
     * @param daysOffset # of days to add.
     * @param stripTime If true, strip the time component.
     * @return Input value the specified operations applied.
     */
    public static Date addDays(Date date, int daysOffset, boolean stripTime) {
        if (date == null) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false); // Make sure the calendar will not perform
        // automatic correction.
        calendar.setTime(date); // Set the time of the calendar to the given
        // date.
        
        if (stripTime) { // Remove the hours, minutes, seconds and milliseconds.
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        
        calendar.add(Calendar.DAY_OF_MONTH, daysOffset);
        return calendar.getTime();
        
    }
    
    /**
     * Strips the time component from a date.
     * 
     * @param date Original date.
     * @return Date without the time component.
     */
    public static Date stripTime(Date date) {
        return addDays(date, 0, true);
    }
    
    /**
     * Returns the input date with the time set to the end of the day.
     * 
     * @param date Original date.
     * @return Date with time set to end of day.
     */
    public static Date endOfDay(Date date) {
        if (date == null) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
    
    /**
     * Returns a date with the current time.
     * 
     * @return Current date and time.
     */
    public static Date now() {
        return new Date();
    }
    
    /**
     * Returns a date with the current day (no time).
     * 
     * @return Current date.
     */
    public static Date today() {
        return stripTime(now());
    }
    
    /**
     * Changes a time from one time zone to another.
     * 
     * @param date Date/time to change.
     * @param oldTimeZone Old time zone.
     * @param newTimeZone New time zone.
     * @return Date/time converted from old time zone to new time zone.
     */
    public static Date changeTimeZone(Date date, TimeZone oldTimeZone, TimeZone newTimeZone) {
        if (!hasTime(date)) {
            return date;
        }
        
        Calendar cal = Calendar.getInstance(oldTimeZone);
        cal.setTime(date);
        cal.setTimeZone(newTimeZone);
        return cal.getTime();
    }
    
    /**
     * Converts a date/time value to a string, using the format dd-mmm-yyyy hh:mm. Because we cannot
     * determine the absence of a time from a time of 24:00, we must assume a time of 24:00 means
     * that no time is present and strip that from the return value.
     * 
     * @param date Date value to convert.
     * @return Formatted string representation of the specified date, or an empty string if date is
     *         null.
     */
    public static String formatDate(Date date) {
        return formatDate(date, false, false);
    }
    
    /**
     * Converts a date/time value to a string, using the format dd-mmm-yyyy hh:mm. Because we cannot
     * determine the absence of a time from a time of 24:00, we must assume a time of 24:00 means
     * that no time is present and strip that from the return value.
     * 
     * @param date Date value to convert.
     * @param showTimezone If true, time zone information is also appended.
     * @return Formatted string representation of the specified date, or an empty string if date is
     *         null.
     */
    public static String formatDate(Date date, boolean showTimezone) {
        return formatDate(date, showTimezone, false);
    }
    
    /**
     * Converts a date/time value to a string, using the format dd-mmm-yyyy hh:mm. Because we cannot
     * determine the absence of a time from a time of 24:00, we must assume a time of 24:00 means
     * that no time is present and strip that from the return value.
     * 
     * @param date Date value to convert
     * @param showTimezone If true, time zone information is also appended.
     * @param ignoreTime If true, the time component is ignored.
     * @return Formatted string representation of the specified date, or an empty string if date is
     *         null.
     */
    public static String formatDate(Date date, boolean showTimezone, boolean ignoreTime) {
        if (date == null) {
            return "";
        }
        
        ignoreTime = ignoreTime || !hasTime(date);
        int idx = ignoreTime ? 2 : showTimezone ? 0 : 1;
        return getFormatter(idx).format(date);
    }
    
    /**
     * Returns a date formatter for the specified format index.
     * 
     * @param idx Format index
     * @return A date formatter for the specified format.
     */
    private static FastDateFormat getFormatter(int idx) {
        boolean ignoreTime = idx == 2 || idx == 3;
        return FastDateFormat.getInstance(DATE_PATTERNS[idx], ignoreTime ? TimeZone.getDefault() : getLocalTimeZone());
    }
    
    /**
     * Same as formatDate(Date, boolean) except replaces the time separator with the specified
     * string.
     * 
     * @param date Date value to convert
     * @param timeSeparator String to use in place of default time separator
     * @return Formatted string representation of the specified date using the specified time
     *         separator.
     */
    public static String formatDate(Date date, String timeSeparator) {
        return formatDate(date).replaceFirst(" ", timeSeparator);
    }
    
    /**
     * Convert a date to HL7 format.
     * 
     * @param date
     * @return The HL7-formatted date.
     */
    public static String toHL7(Date date) {
        return date == null ? "" : getFormatter(hasTime(date) ? 4 : 3).format(date);
    }
    
    /**
     * Returns true if the date has an associated time.
     * 
     * @param date Date value to check.
     * @return True if the date has a time component.
     */
    public static boolean hasTime(Date date) {
        long time1 = date.getTime();
        long time2 = stripTime(date).getTime();
        return time1 != time2; // Do not use "Date.equals" since date may be of
        // type Timestamp.
    }
    
    /**
     * Return elapsed time in ms to displayable format with units.
     * 
     * @param elapsed Elapsed time in ms.
     * @return Elapsed time in displayable format.
     */
    public static String formatElapsed(double elapsed) {
        return formatElapsed(elapsed, true, false, false);
    }
    
    /**
     * Return elapsed time in ms to displayable format with units.
     * 
     * @param elapsed Elapsed time in ms.
     * @param pluralize If true, pluralize units when appropriate.
     * @param abbreviated If true, use abbreviated form of units.
     * @param round If true, round result to an integer.
     * @return Elapsed time in displayable format.
     */
    public static String formatElapsed(double elapsed, boolean pluralize, boolean abbreviated, boolean round) {
        int index = SECONDS_FP.length - 1;
        String prefix = "";
        elapsed /= 1000.0;
        
        if (elapsed < 0) {
            elapsed = -elapsed;
            prefix = "-";
        }
        
        for (int i = 0; i < SECONDS_FP.length; i++) {
            if (elapsed >= SECONDS_FP[i]) {
                elapsed /= SECONDS_FP[i];
                index = i;
                break;
            }
        }
        
        if (round) {
            elapsed = Math.floor(elapsed);
        }
        
        return prefix + decimalFormat.get().format(elapsed) + " "
                + getDurationUnits(index, pluralize && elapsed != 1.0, abbreviated);
    }
    
    /**
     * Formats a duration in ms.
     * 
     * @param duration Duration in ms.
     * @return Formatted duration.
     */
    public static String formatDuration(long duration) {
        return formatDuration(duration, null);
    }
    
    /**
     * Formats a duration in ms to the specified accuracy.
     * 
     * @param duration Duration in ms.
     * @param accuracy Accuracy of output.
     * @return Formatted duration.
     */
    public static String formatDuration(long duration, Accuracy accuracy) {
        return formatDuration(duration, accuracy, true, false);
    }
    
    /**
     * Formats a duration in ms to the specified accuracy.
     * 
     * @param duration Duration in ms.
     * @param accuracy Accuracy of output.
     * @param pluralize If true, pluralize units when appropriate.
     * @param abbreviated If true, use abbreviated form of units.
     * @return Formatted duration.
     */
    public static String formatDuration(long duration, Accuracy accuracy, boolean pluralize, boolean abbreviated) {
        StringBuilder sb = new StringBuilder();
        
        if (duration < 0) {
            duration = -duration;
            sb.append('-');
        }
        
        accuracy = accuracy == null ? Accuracy.MILLISECONDS : accuracy;
        int last = accuracy.ordinal();
        boolean empty = true;
        
        for (int i = 0; i <= last; i++) {
            long val = duration / SECONDS_LG[i];
            duration -= val * SECONDS_LG[i];
            
            if (val != 0 || (empty && i == last)) {
                if (!empty) {
                    sb.append(' ');
                } else {
                    empty = false;
                }
                
                sb.append(val).append(' ').append(getDurationUnits(i, pluralize && val != 1, abbreviated));
            }
        }
        
        return sb.toString();
    }
    
    private static String getDurationUnits(Accuracy accuracy, boolean plural, boolean abbreviated) {
        return getDurationUnits(accuracy.ordinal(), plural, abbreviated);
    }
    
    private static String getDurationUnits(int index, boolean plural, boolean abbreviated) {
        int which = (plural ? 1 : 0) + (abbreviated ? 2 : 0);
        return DURATION_UNITS[index][which];
    }
    
    /**
     * Returns the user's time zone.
     * 
     * @return The user's time zone.
     */
    public static TimeZone getLocalTimeZone() {
        return localTimeZone.getTimeZone();
    }
    
    /**
     * <p>
     * Returns age as a formatted string expressed in days, months, or years, depending on whether
     * person is an infant (< 2 mos), toddler (> 2 mos, < 2 yrs), or more than 2 years old.
     * </p>
     * 
     * @param dob Date of person's birth
     * @return the age display string
     */
    public static String getAgeForDisplay(Date dob) {
        return getAgeForDisplay(dob, true, null);
    }
    
    /**
     * <p>
     * Returns age as a formatted string expressed in days, months, or years, depending on whether
     * person is an infant (< 2 mos), toddler (> 2 mos, < 2 yrs), or more than 2 years old.
     * </p>
     * <p>
     * Allows the caller to specify an &quot;as-of&quot; date. The calculated age will be as-of the
     * provided date, rather than as-of the current date.
     * </p>
     * <p>
     * Allows the caller to specify whether or not to pluralize the age units in the age display
     * string.
     * </p>
     * 
     * @param dob Date of person's birth
     * @param pluralize If true, pluralize the age units in the age display string.
     * @param ref The date as of which to calculate the Person's age (null means today).
     * @return the age display string
     */
    public static String getAgeForDisplay(Date dob, boolean pluralize, Date ref) {
        if (dob == null) {
            return UNKNOWN;
        }
        
        Calendar asOf = Calendar.getInstance();
        asOf.setTimeInMillis(ref == null ? System.currentTimeMillis() : ref.getTime());
        Calendar bd = Calendar.getInstance();
        bd.setTime(dob);
        long birthDateInDays = (asOf.getTimeInMillis() - bd.getTimeInMillis()) / 1000 / 60 / 60 / 24;
        
        if (birthDateInDays < 0) {
            return UNKNOWN;
        }
        
        if (birthDateInDays <= 1) {
            return "newborn";
        }
        
        // If person is less than 2 months old, then display age in days
        if (birthDateInDays <= 60) {
            return formatUnits(birthDateInDays, Accuracy.DAYS, pluralize);
        }
        
        int birthYear = bd.get(Calendar.YEAR);
        int birthMonth = bd.get(Calendar.MONTH);
        int birthDay = bd.get(Calendar.DATE);
        int refYear = asOf.get(Calendar.YEAR);
        int refMonth = asOf.get(Calendar.MONTH);
        int refDay = asOf.get(Calendar.DATE);
        
        if (birthDateInDays <= 730) {
            // If person is more than 2 months but less than 2 years then display age in months
            if (refMonth >= birthMonth && refDay >= birthDay) {
                // If person has had a birthday already this year
                return formatUnits((refYear - birthYear) * 12 + refMonth - birthMonth, Accuracy.MONTHS, pluralize);
            }
            // then age in months = # years old * 12 + months so far this year
            // If person has not yet had a birthday this year, subtract 1 month
            return formatUnits((refYear - birthYear) * 12 + refMonth - birthMonth - 1, Accuracy.MONTHS, pluralize);
        }
        // If person is more than 2 years old then display age in years
        return formatUnits(getAgeInYears(birthYear, birthMonth, birthDay, refYear, refMonth, refDay), Accuracy.YEARS,
            pluralize);
    }
    
    private static int getAgeInYears(int birthYear, int birthMonth, int birthDay, int refYear, int refMonth, int refDay) {
        // If person has had a birthday already this year
        if (refMonth > birthMonth || (refMonth == birthMonth && refDay >= birthDay)) {
            return refYear - birthYear;
        }
        
        // If person has not yet had a birthday this year, subtract 1
        return refYear - birthYear - 1;
    }
    
    private static String formatUnits(long value, Accuracy accuracy, boolean pluralize) {
        return value + " " + getDurationUnits(accuracy, pluralize && value != 1, true);
    }
    
    /**
     * Converts day, month, and year to a date.
     * 
     * @param day Day of month.
     * @param month Month (1=January, etc.)
     * @param year Year (4 digit).
     * @return Date instance.
     */
    public static Date toDate(int day, int month, int year) {
        return toDate(day, month, year, 0, 0, 0);
    }
    
    /**
     * Converts day, month, year and time parameters to a date.
     * 
     * @param day Day of month.
     * @param month Month (1=January, etc.)
     * @param year Year (4 digit).
     * @param hr Hour of day.
     * @param min Minutes past the hour.
     * @param sec Seconds past the minute.
     * @return Date instance.
     */
    public static Date toDate(int day, int month, int year, int hr, int min, int sec) {
        Calendar cal = Calendar.getInstance(localTimeZone.getTimeZone());
        cal.set(year, month - 1, day, hr, min, sec);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    /**
     * Enforce static class.
     */
    private DateUtil() {
    };
}
