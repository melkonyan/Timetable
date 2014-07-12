package com.timetable.android.tests;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.timetable.android.functional.TimetableFunctional;
import com.timetable.android.R;

import junit.framework.TestCase;

public class TimetableFunctionalTestCase extends TestCase {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	
	public void testAreEqualOrNulls() {
		String t1 = "String1";
		String t2 = t1;
		
		assertEquals(true, TimetableFunctional.areEqualOrNulls(null, null));

		assertEquals(false, TimetableFunctional.areEqualOrNulls(null, t1));
		
		assertEquals(false, TimetableFunctional.areEqualOrNulls(t1, null));
	
		assertEquals(true, TimetableFunctional.areEqualOrNulls(t1, t2));
	
		t2 = "String2";
		
		assertEquals(false, TimetableFunctional.areEqualOrNulls(t1, t2));
	}
	
	public void testAreSameDates() {
		try {
		
		Date date1 = dateFormat.parse("29.12.2013 00:00:00");
		Date date2 = dateFormat.parse("29.12.2013 23:59:59");
		assertEquals(true, TimetableFunctional.areSameDates(date1, date2));
		
		date1 = dateFormat.parse("30.12.2013 00:00:00");
		
		assertEquals(false, TimetableFunctional.areSameDates(date1, date2));
		
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
