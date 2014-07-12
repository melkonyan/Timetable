package com.timetable.android.tests;

import java.text.SimpleDateFormat;

import junit.framework.TestCase;

import com.timetable.android.EventPeriod;

public class EventPeriodTestCase extends TestCase {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	public void testPeriodWeekly() {
		EventPeriod period = new EventPeriod();
		period.type = EventPeriod.Type.WEEKLY;
		
		period.setWeekOccurrences(2);
		
		assertEquals(true, period.weekOccurrences[5]);
		
		period.weekOccurrences[4] = true;
		
		assertEquals(6, period.getWeekOccurrences());
		
		period.addWeekOccurrence(EventPeriod.TUESDAY);
		
		assertEquals(true, period.weekOccurrences[2]);
		
		assertEquals(true, period.isWeekOccurrence(EventPeriod.TUESDAY));
		
		period.deleteWeekOccurrence(EventPeriod.TUESDAY);
		
		assertEquals(false, period.isWeekOccurrence(EventPeriod.TUESDAY));
	}
	
	public void testEquals() {
		try {
			EventPeriod period1 = new EventPeriod();
			EventPeriod period2 = new EventPeriod();
			
			assertEquals(true, period1.equals(period2));
			
			period1.type = EventPeriod.Type.DAILY;
			
			assertEquals(false, period1.equals(period2));
			
			period2.type = period1.type;
			
			assertEquals(true, period1.equals(period2));
			
			period1.interval = 2;
			
			assertEquals(false, period1.equals(period2));
			
			period2.interval = period1.interval;
			
			assertEquals(true, period1.equals(period2));
			
			period1.endDate = dateFormat.parse("30.12.2013");
			
			assertEquals(false, period1.equals(period2));
			
			period2.endDate = period1.endDate;
			
			assertEquals(true, period1.equals(period2));
			
			period1.type = EventPeriod.Type.NONE;
			period2.type = period1.type;
			period2.endDate = null;
			period2.interval = 0;
			
			assertEquals(true, period1.equals(period2));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
