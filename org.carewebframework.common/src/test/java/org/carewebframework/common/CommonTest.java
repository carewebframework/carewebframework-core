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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.carewebframework.common.DateUtil.TimeUnit;
import org.carewebframework.common.ObservedCollection.IObservedCollectionListener;
import org.carewebframework.common.Version.VersionPart;
import org.junit.Test;

public class CommonTest {
    
    private static final String DATE = "21-Nov-1978"; // Reference date
    
    @Test
    public void testPiece() {
        String text = "pc1^pc2^pc3^^pc5^pc6^^^";
        String delm = "^";
        assertEquals("pc1", StrUtil.piece(text, delm));
        assertEquals("pc2", StrUtil.piece(text, delm, 2));
        assertEquals("pc3^^pc5", StrUtil.piece(text, delm, 3, 5));
        assertEquals("pc6^^^", StrUtil.piece(text, delm, 6, 9999));
        assertEquals("", StrUtil.piece(text, delm, 0));
        assertEquals("", StrUtil.piece(text, delm, 0, 0));
        assertEquals("pc1^pc2^pc3^^pc5", StrUtil.piece(text, delm, 0, 5));
    }
    
    @Test
    public void testNumUtil() {
        assertEquals("0", NumUtil.toString(0.0));
        assertEquals("1.25", NumUtil.toString(1.25));
        assertEquals("125", NumUtil.toString(125.0));
        assertTrue(NumUtil.compare(1, 2) < 0);
        assertTrue(NumUtil.compare(5, 5) == 0);
        assertTrue(NumUtil.compare(6, 3) > 0);
        assertTrue(NumUtil.compare(1.34, 2.5) < 0);
        assertTrue(NumUtil.compare(3.54, 3.54) == 0);
        assertTrue(NumUtil.compare(5.45, 5.31) > 0);
        assertEquals(5, NumUtil.enforceRange(10, 1, 5));
        assertEquals(10, NumUtil.enforceRange(10, 1, 20));
        assertEquals(5, NumUtil.enforceRange(1, 5, 20));
    }
    
    @Test
    public void testBooleanUtil() {
        assertTrue(StrUtil.toBoolean("YES"));
        assertTrue(StrUtil.toBoolean("Y"));
        assertTrue(StrUtil.toBoolean("yes"));
        assertTrue(StrUtil.toBoolean("y"));
        assertTrue(StrUtil.toBoolean("TRUE"));
        assertTrue(StrUtil.toBoolean("t"));
        assertTrue(StrUtil.toBoolean("1"));
        assertTrue(StrUtil.toBoolean("100"));
        assertFalse(StrUtil.toBoolean(null));
        assertFalse(StrUtil.toBoolean(""));
        assertFalse(StrUtil.toBoolean("false"));
        assertFalse(StrUtil.toBoolean("0"));
        assertFalse(StrUtil.toBoolean("any old string"));
    }
    
    @Test
    public void testDateUtil() {
        testDate(now());
        testDate(today());
        testDate("T", today(), 0);
        testDate("N", now(), 100);
        testDate("T+30", DateUtil.addDays(today(), 30, false), 0);
        testDate("N+30", DateUtil.addDays(now(), 30, false), 100);
        testDate("T-4", DateUtil.addDays(today(), -4, false), 0);
        testDate("T-50s", new Date(today().getTime() - 50000), 0);
        testDate("N-50s", new Date(now().getTime() - 50000), 100);
        testDate("T-50h", new Date(today().getTime() - 50 * 60 * 60 * 1000), 0);
        testDate("T-50n", new Date(today().getTime() - 50 * 60 * 1000), 0);
    }
    
    private Date now() {
        return new Date();
    }
    
    private Date today() {
        return DateUtil.stripTime(now());
    }
    
    private void testDate(String value, Date expected, int threshold) {
        Date actual = DateUtil.parseDate(value);
        testDate(actual);
        long diff = Math.abs(expected.getTime() - actual.getTime());
        assertTrue("Difference exceeded threshold " + diff + " (" + threshold + ")", diff <= threshold);
    }
    
    private void testDate(Date date) {
        testDate(date, true, true);
        testDate(date, false, false);
        testDate(date, true, false);
        testDate(date, false, true);
    }
    
    private void testDate(Date date, boolean showTimezone, boolean ignoreTime) {
        String text = DateUtil.formatDate(date, showTimezone, ignoreTime);
        print(text);
        Date date2 = DateUtil.parseDate(text);
        String text2 = DateUtil.formatDate(date2, showTimezone, ignoreTime);
        assertEquals(text, text2);
    }
    
    @Test
    public void testDefaultTimeZone() throws Exception {
        testDateFormat("EST", "13:04");
        testDateFormat("GMT", "18:04");
        testDateFormat("CST", "12:04");
    }
    
    @Test
    public void testFormatting() throws Exception {
        doTestFormatting("", "");
        doTestFormatting(" 00:00", "");
        doTestFormatting(" 13:24", " 13:24");
        doTestFormatting(" 00:39", " 00:39");
    }
    
    private void doTestFormatting(String time, String expected) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy" + (time.length() == 0 ? "" : " HH:mm"));
        Date date = formatter.parse(DATE + time);
        assertEquals(DateUtil.formatDate(date), DATE + expected);
    }
    
    private void testDateFormat(String tz, String time) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm zzz");
        Date date = formatter.parse(DATE + " 13:04 EST"); // Reference date/time is 21-Nov-1978 13:04 EST
        TimeZone.setDefault(TimeZone.getTimeZone(tz));
        String DATE_TIME_NOTZ = DATE + " " + time;
        String DATE_TIME_TZ = DATE_TIME_NOTZ + " " + tz;
        assertEquals(DATE_TIME_TZ, DateUtil.formatDate(date, true));
        assertEquals(DATE_TIME_NOTZ, DateUtil.formatDate(date));
        assertEquals(DATE, DateUtil.formatDate(date, true, true));
        assertEquals(DATE_TIME_NOTZ, DateUtil.formatDate(date, false));
        assertEquals(DATE_TIME_NOTZ, DateUtil.formatDate(date, false, false));
        
        formatter = new SimpleDateFormat("dd-MMM-yyyy");
        date = formatter.parse(DATE);
        assertEquals(DATE, DateUtil.formatDate(date, true));
        assertEquals(DATE, DateUtil.formatDate(date));
        assertEquals(DATE, DateUtil.formatDate(date, true, true));
        assertEquals(DATE, DateUtil.formatDate(date, false));
        assertEquals(DATE, DateUtil.formatDate(date, false, false));
    }
    
    @Test
    public void testDateRange() {
        DateRange dr = new DateRange("test|12-Jul-2010|15-Aug-2010");
        assertEquals(dr.getStartDate(), DateUtil.toDate(12, 7, 2010));
        assertEquals(dr.getEndDate(), DateUtil.toDate(15, 8, 2010));
        assertTrue(dr.inRange(DateUtil.toDate(1, 8, 2010)));
        assertFalse(dr.inRange(DateUtil.toDate(15, 8, 2010)));
        assertTrue(dr.inRange(DateUtil.toDate(15, 8, 2010), true, true));
        assertFalse(dr.inRange(DateUtil.toDate(15, 8, 2010, 13, 30, 0), true, true));
        assertFalse(dr.inRange(DateUtil.toDate(16, 8, 2010), true, true));
    }
    
    @Test
    public void testSerializer() {
        JSONUtil.registerAlias("TestPerson", TestPerson.class);
        testSerializer(null);
        testSerializer("resourceType");
    }
    
    private void testSerializer(String typeProperty) {
        TestPerson obj = new TestPerson();
        String s = JSONUtil.serialize(typeProperty, obj);
        print(s);
        TestPerson obj2 = (TestPerson) JSONUtil.deserialize(typeProperty, s);
        assertTrue(obj.equals(obj2));
        List<TestPerson> list = new ArrayList<>();
        list.add(obj);
        list.add(obj);
        s = JSONUtil.serialize(typeProperty, list);
        print(s);
        List<TestPerson> list2 = JSONUtil.deserializeList(typeProperty, s, TestPerson.class);
        assertEquals(list, list2);
        @SuppressWarnings("unchecked")
        List<TestPerson> list3 = (List<TestPerson>) JSONUtil.deserialize(typeProperty, s);
        assertEquals(list, list3);
    }
    
    @Test
    public void testElapsed() {
        assertEquals("0.1 seconds", DateUtil.formatElapsed(100.0, TimeUnit.SECONDS));
        assertEquals("1 second", DateUtil.formatElapsed(1000.0));
        assertEquals("1 minute", DateUtil.formatElapsed(60000.0));
        assertEquals("3.59 days", DateUtil.formatElapsed(309898934.0));
        assertEquals("98.2 years", DateUtil.formatElapsed(3098989343984.0));
        assertEquals("-98.2 years", DateUtil.formatElapsed(-3098989343984.0));
        
        assertEquals(100.0, DateUtil.parseElapsed("0.1 seconds"), 0.0);
        assertEquals(1000.0, DateUtil.parseElapsed("1 second"), 0.0);
        assertEquals(60000.0, DateUtil.parseElapsed("1 minute"), 0.0);
        assertEquals(310176000.0, DateUtil.parseElapsed("3.59 days"), 0.0);
        assertEquals(3098956320000.0, DateUtil.parseElapsed("98.2 years"), 0.0);
        assertEquals(-3098956320000.0, DateUtil.parseElapsed("-98.2 years"), 0.0);
        assertEquals(98.2, DateUtil.parseElapsed("98.2 years", TimeUnit.YEARS), 0.0);
    }
    
    @Test
    public void testDuration() {
        assertEquals("0 seconds", DateUtil.formatDuration(100, TimeUnit.SECONDS));
        assertEquals("0 sec", DateUtil.formatDuration(100, TimeUnit.SECONDS, false, true));
        assertEquals("1 second", DateUtil.formatDuration(1000, TimeUnit.SECONDS));
        assertEquals("1 minute", DateUtil.formatDuration(60000, TimeUnit.SECONDS));
        assertEquals("3 days 14 hours 4 minutes 58 seconds", DateUtil.formatDuration(309898934, TimeUnit.SECONDS));
        assertEquals("3 day 14 hour 4 minute 58 second", DateUtil.formatDuration(309898934, TimeUnit.SECONDS, false, false));
        assertEquals("98 years 2 months 1 week 6 days 10 hours 22 minutes 23 seconds",
            DateUtil.formatDuration(3098989343984L, TimeUnit.SECONDS));
        assertEquals("3 days 14 hrs 4 mins 58 secs", DateUtil.formatDuration(309898934, TimeUnit.SECONDS, true, true));
        assertEquals("-98 years 2 months 1 week 6 days 10 hours 22 minutes 23 seconds",
            DateUtil.formatDuration(-3098989343984L, TimeUnit.SECONDS));
    }
    
    @Test
    public void testAge() {
        Date dob = DateUtil.toDate(27, 7, 1958);
        Date ref = DateUtil.toDate(1, 1, 2013);
        assertEquals("54 yrs", DateUtil.formatAge(dob, true, ref));
        assertEquals("54 yr", DateUtil.formatAge(dob, false, ref));
        dob = DateUtil.toDate(15, 12, 2012);
        assertEquals("17 days", DateUtil.formatAge(dob, true, ref));
        dob = DateUtil.toDate(30, 10, 2012);
        assertEquals("2 mos", DateUtil.formatAge(dob, true, ref));
    }
    
    @Test
    public void testColorUtil() {
        testColorUtil("darkorchid", "#9932CC");
        testColorUtil("azure", "#F0FFFF");
    }
    
    public void testColorUtil(String testColor, String testRGB) {
        Color refColor = Color.magenta;
        String rgb = ColorUtil.getRGBFromName(testColor);
        assertEquals(rgb, testRGB);
        String color = ColorUtil.getNameFromRGB(rgb);
        assertEquals(color.toLowerCase(), testColor.toLowerCase());
        Color color1 = ColorUtil.toColor(rgb, refColor);
        Color color2 = ColorUtil.toColor(color, refColor);
        assertEquals(color1, color2);
        Color color3 = ColorUtil.toColor("badvalue", refColor);
        assertEquals(refColor, color3);
    }
    
    @Test
    public void testQueryStringBuilder() {
        QueryStringBuilder sb = new QueryStringBuilder();
        assertEquals(0, sb.length());
        sb.append("q1", "value 1", "value 2");
        List<String> list = new ArrayList<>();
        list.add("value 1");
        list.add("value 2");
        list.add("value 3");
        sb.append("q2", list);
        sb.append("q3", "single");
        sb.append("q4<>", "escape+name");
        String qs = sb.toString();
        print(qs);
        assertEquals("q1=value+1,value+2&q2=value+1&q2=value+2&q2=value+3&q3=single&q4%3C%3E=escape%2Bname", qs);
        sb.clear();
        assertTrue(sb.toString().isEmpty());
    }
    
    @Test
    public void testListMethods() {
        String original = "1,2,3,4,5";
        // Basic conversion to and from list
        List<String> strList = StrUtil.toList(original, ",");
        assertEquals(5, strList.size());
        String str = StrUtil.fromList(strList, ",");
        assertEquals(original, str);
        // Behavior with empty elements and trailing delimiter
        strList = StrUtil.toList(",,3,4,5,", ",");
        assertEquals(5, strList.size());
        // Behavior with non-string list
        List<Integer> intList = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5 });
        str = StrUtil.fromList(intList, ",");
        assertEquals(original, str);
        // Behavior with null list entry, with and without default value
        intList = Arrays.asList(new Integer[] { 1, 2, null, 4, 5 });
        str = StrUtil.fromList(intList, ",");
        assertEquals("1,2,,4,5", str);
        str = StrUtil.fromList(intList, ",", null);
        assertEquals("1,2,4,5", str);
        str = StrUtil.fromList(intList, ",", "3");
        assertEquals(original, str);
        List<Object> iterList = new ArrayList<>();
        iterList.add(new Integer(1));
        iterList.add("string #1");
        iterList.add(new Integer(2));
        iterList.add(new Long(1));
        iterList.add("string #2");
        Iterator<Integer> iter = MiscUtil.iteratorForType(iterList, Integer.class);
        assertTrue(iter.hasNext());
        assertEquals((Integer) 1, iter.next());
        iter.remove();
        assertEquals((Integer) 2, iter.next());
        assertFalse(iter.hasNext());
        assertEquals(4, iterList.size());
        int strCount = 0;
        
        for (String ele : MiscUtil.iterableForType(iterList, String.class)) {
            strCount++;
            assertTrue(ele.equals("string #" + strCount));
        }
        
        assertEquals(2, strCount);
    }
    
    @Test
    public void testObservedList() {
        List<String> list = new ArrayList<>();
        final int[] ops = { 0, 0 };
        
        ObservedCollection<String> col = new ObservedCollection<>(list, new IObservedCollectionListener<String>() {
            
            @Override
            public void onAddElement(String element) {
                ops[0]++;
            }
            
            @Override
            public void onRemoveElement(String element) {
                ops[1]++;
            }
            
        });
        
        col.add("ele1"); // ele1
        col.add("ele2"); // ele1, ele2
        col.remove("ele1"); // ele2
        col.addAll(Arrays.asList(new String[] { "ele3", "ele4", "ele5" })); // ele2, ele3, ele4, ele5
        col.removeAll(Arrays.asList(new String[] { "ele1", "ele4" })); // ele2, ele3, ele5
        col.retainAll(Arrays.asList(new String[] { "ele1", "ele3" })); // ele3
        assertEquals("Add count does not match.", 5, ops[0]);
        assertEquals("Remove count does not match.", 4, ops[1]);
        col.clear();
        assertEquals("Add count does not match.", 5, ops[0]);
        assertEquals("Remove count does not match.", 5, ops[1]);
    }
    
    @Test
    public void testVersion() {
        Version v1 = new Version(1, 2, 3, 4);
        Version v2 = new Version("1.2.3.4");
        assertEquals(v1, v2);
        v1 = new Version("1.2.3");
        assertTrue(v1.compareTo(v2) < 0);
        assertEquals("1.2.3", v1.toString());
        assertEquals("1.2.3", v1.toString(VersionPart.RELEASE));
        assertEquals("1.2.3.0", v1.toString(VersionPart.BUILD));
        v1 = new Version("1.0.3");
        assertEquals("1.0.3", v1.toString());
        assertEquals("1.0.3.0", v1.toString(VersionPart.BUILD));
        assertEquals("1.0.3", v1.toString(VersionPart.MINOR));
        assertEquals("1.0.3", v1.toString(VersionPart.RELEASE));
        v2 = new Version("1.0.3b");
        assertEquals(v1, v2);
        v1 = new Version(".5");
        assertEquals("0.5", v1.toString());
        v1 = new Version("");
        assertEquals("", v1.toString());
        v2 = new Version(null);
        assertEquals(v1, v2);
    }
    
    @Test
    public void testBundle() {
        Localizer.registerMessageSource(new BundleMessageSource());
        Locale locale1 = new Locale("en");
        Locale locale2 = new Locale("fr");
        assertEquals("keyboard", StrUtil.getLabel("message.test1", locale1));
        assertEquals("clavier", StrUtil.getLabel("message.test1", locale2));
    }
    
    @Test
    public void testWeakCollections() {
        List<Object> list = new WeakList<>();
        Map<String, Object> map = new WeakMap<>();
        
        Object o1 = new Object();
        Object o2 = new Object();
        list.add(o1);
        list.add(o2);
        map.put("o1", o1);
        map.put("o2", o2);
        assertEquals(2, list.size());
        assertEquals(2, map.size());
        o1 = null;
        System.gc();
        wait(3000);
        assertEquals(1, list.size());
        assertEquals(1, map.size());
    }
    
    private void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {}
    }
    
    private void print(Object object) {
        System.out.println(object);
    }
    
}
