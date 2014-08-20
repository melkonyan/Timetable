package com.timetable.android.tests;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.Utils;

public class TimetableUtilsTestCase extends TestCase {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	
	public void testAreEqualOrNulls() {
		String t1 = "String1";
		String t2 = t1;
		
		assertEquals(true, Utils.areEqualOrNulls(null, null));

		assertEquals(false, Utils.areEqualOrNulls(null, t1));
		
		assertEquals(false, Utils.areEqualOrNulls(t1, null));
	
		assertEquals(true, Utils.areEqualOrNulls(t1, t2));
	
		t2 = "String2";
		
		assertEquals(false, Utils.areEqualOrNulls(t1, t2));
	}
	
	public void testAreSameDates() {
		try {
		
		Date date1 = dateFormat.parse("29.12.2013 00:00:00");
		Date date2 = dateFormat.parse("29.12.2013 23:59:59");
		assertEquals(true, DateUtils.areSameDates(date1, date2));
		
		date1 = dateFormat.parse("30.12.2013 00:00:00");
		
		assertEquals(false, DateUtils.areSameDates(date1, date2));
		
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
