package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.timetable.android.EventPeriod;

public class EventPeriodTestCase extends TestCase {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	public void testPeriodWeekly() {
		EventPeriod period = new EventPeriod();;
		period.type = EventPeriod.Type.WEEKLY;
		
		period.setWeekOccurrences(2);
		
		assertTrue(period.weekOccurrences[5]);
		
		period.weekOccurrences[4] = true;
		
		assertEquals(6, period.getWeekOccurrences());
		
		period.addWeekOccurrence(EventPeriod.TUESDAY);
		
		assertTrue(period.weekOccurrences[2]);
		
		assertTrue(period.isWeekOccurrence(EventPeriod.TUESDAY));
		
		period.deleteWeekOccurrence(EventPeriod.TUESDAY);
		
		assertFalse(period.isWeekOccurrence(EventPeriod.TUESDAY));
	}
	
	/*
	 * Test that function hasOccurrenceOnDate works normally for period, that does not repeat.
	 */
	public void testHasOccurrenceOnDateTypeNone() throws ParseException {
		EventPeriod period = new EventPeriod();;
		Date searchDate = dateFormat.parse("27.12.2013");
		Date startDate = dateFormat.parse("27.12.2013");
		
		assertTrue(period.hasOccurrenceOnDate(startDate,searchDate));
	
		startDate = dateFormat.parse("24.12.2013");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
	
	}
	
	/*
	 * Test that function hasOccurrenceOnDate works normally for daily period.
	 */
	public void testhasOccurrenceOnDateTypeDaily() throws ParseException {
		Date searchDate = dateFormat.parse("27.12.2013");
		Date startDate = dateFormat.parse("24.12.2013");
		EventPeriod period = new EventPeriod();;
		period.type = EventPeriod.Type.DAILY;
		period.interval = 1;
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.interval = 2;
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.interval = 3;
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.endDate = dateFormat.parse("26.12.2013");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.endDate = dateFormat.parse("27.12.2013");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.endDate = dateFormat.parse("28.12.2013");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		// testing leap years
		
		period.endDate = null;
		startDate = dateFormat.parse("27.02.2012");
		searchDate = dateFormat.parse("01.03.2012");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		startDate = dateFormat.parse("27.02.1900"); //not a leap year
		searchDate = dateFormat.parse("01.03.1900"); 
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
	}
	
	/*
	 * Test that function hasOccurrenceOnDate works normally for weely period. 
	 */
	public void testHasOccurrenceOnDateTypeWeekly() throws ParseException {
		Date searchDate = dateFormat.parse("04.01.2014"); //Saturday
		EventPeriod period = new EventPeriod();;
		period.type = EventPeriod.Type.WEEKLY;
		period.interval = 1;
		Date startDate = dateFormat.parse("29.11.2013"); //Saturday
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.addWeekOccurrence(EventPeriod.SUNDAY); // every Sunday
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.addWeekOccurrence(EventPeriod.SATURDAY); // every Saturday
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.interval = 2;
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		searchDate = dateFormat.parse("11.01.2014");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.endDate = dateFormat.parse("10.01.2014");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.endDate = null;
		period.interval = 2;
		startDate = dateFormat.parse("30.06.2014");
		period.addWeekOccurrence(EventPeriod.MONDAY);
		searchDate = dateFormat.parse("07.07.2014");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		searchDate = dateFormat.parse("14.07.2014");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
	}
	
	/*
	 * Test that function hasOccurrenceOnDate works normally for monthly period.
	 */
	public void testIsTodayPeriodMonthly() throws ParseException {
		Date searchDate = dateFormat.parse("29.01.2014");
		EventPeriod period = new EventPeriod();;
		Date startDate = dateFormat.parse("29.12.2013");
		period.type = EventPeriod.Type.MONTHLY;
		period.interval = 1;
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.interval = 2;
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		searchDate = dateFormat.parse("29.12.2015");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.interval = 6;
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
	
		period.endDate = dateFormat.parse("29.12.2014");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.endDate = dateFormat.parse("30.12.2015");

		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		startDate = dateFormat.parse("30.12.2013");

		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
	}
	
	/*
	 * Test that function hasOccurrenceOnDate works normally for yearly period.
	 */	
	public void testHasOccurrenceOnDateTypeYearly() throws ParseException {
		EventPeriod period = new EventPeriod();;
		Date startDate = dateFormat.parse("29.12.2013");
		period.type = EventPeriod.Type.YEARLY;
		period.interval = 1;
		Date searchDate = dateFormat.parse("29.12.2014");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.interval = 3;
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		searchDate = dateFormat.parse("29.12.2019");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		startDate = dateFormat.parse("30.12.2013");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		startDate = dateFormat.parse("29.11.2013");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
	}
	
	public void testGetNextOccurrenceTypeNone() throws ParseException {
			EventPeriod period = new EventPeriod();
			
			Date startDate = dateFormat.parse("12.05.2014");
			
			Date today = dateFormat.parse("09.02.2013");
			assertEquals(startDate, period.getNextOccurrence(startDate, today));
			
			today = dateFormat.parse("11.05.2014");
			assertEquals(startDate, period.getNextOccurrence(startDate, today));
			
			today = dateFormat.parse("12.05.2014");
			assertNull(period.getNextOccurrence(startDate, today));
			
			today = dateFormat.parse("09.03.20147");
			assertNull(period.getNextOccurrence(startDate, today));
	}
	
	public void testGetNextOccurrenceTypeDaily() throws ParseException {	
		EventPeriod period = new EventPeriod();
		period.type = EventPeriod.Type.DAILY;
		period.interval = 1;
		Date startDate = dateFormat.parse("12.05.2014");
		
		Date today = dateFormat.parse("11.05.2014");
		assertEquals(startDate, period.getNextOccurrence(startDate, today));
		
		today = dateFormat.parse("12.05.2014");
		assertEquals(today, period.getNextOccurrence(startDate, today));
		
		today = dateFormat.parse("13.05.2014");
		assertEquals(today, period.getNextOccurrence(startDate, today));
		
		period.interval = 6;
		
		today = dateFormat.parse("13.05.2014");
		assertEquals(dateFormat.parse("18.05.2014"), period.getNextOccurrence(startDate, today));
		
		startDate = dateFormat.parse("30.12.2014");
		
		today = dateFormat.parse("31.12.2014");
		assertEquals(dateFormat.parse("05.01.2015"), period.getNextOccurrence(startDate, today));
		
		period.endDate = dateFormat.parse("03.01.2015");
		assertNull(period.getNextOccurrence(startDate, today));
	}
	/*
	 * Test function getNextOccurrence for weekly period, assuming, that period should not have occurrence on it's startDate
	 */
	public void testGetNextOccurrenceTypeWeekly() throws ParseException {
		EventPeriod period = new EventPeriod();
		period.interval = 1;
		period.type = EventPeriod.Type.WEEKLY;
		period.addWeekOccurrence(EventPeriod.SUNDAY);
		Date startDate = dateFormat.parse("9.06.2014");//Monday
		
		Date today = dateFormat.parse("6.06.2014");//Friday
		
		//assertTrue(period.hasOccurrenceOnDate(startDate, period.getNextOccurrence(startDate, today)));
		assertEquals(dateFormat.parse("15.06.2014"), period.getNextOccurrence(startDate, today));
		
		period.addWeekOccurrence(EventPeriod.MONDAY);
		
		assertEquals(dateFormat.parse("09.06.2014"), period.getNextOccurrence(startDate, today));
		
		period.deleteWeekOccurrence(EventPeriod.MONDAY);
		period.addWeekOccurrence(EventPeriod.WEDNESDAY);
		period.interval = 3;
		
		assertEquals(dateFormat.parse("11.06.2014"), period.getNextOccurrence(startDate, today));
		
		period.addWeekOccurrence(EventPeriod.MONDAY);
		period.deleteWeekOccurrence(EventPeriod.WEDNESDAY);
		period.interval = 3;
		
		today = dateFormat.parse("10.06.2014");//Tuesday
		
		assertEquals(dateFormat.parse("15.06.2014"), period.getNextOccurrence(startDate, today));
		
		period.addWeekOccurrence(EventPeriod.WEDNESDAY);
		
		assertEquals(dateFormat.parse("11.06.2014"), period.getNextOccurrence(startDate, today));
		
		today = dateFormat.parse("03.07.2014");//Thursday
		startDate = dateFormat.parse("30.06.2014");//Monday
		period.interval = 2;
		period.deleteWeekOccurrence(EventPeriod.WEDNESDAY);
		period.deleteWeekOccurrence(EventPeriod.SUNDAY);
		period.getNextOccurrence(startDate, today);
		assertEquals(dateFormat.parse("14.07.2014"), period.getNextOccurrence(startDate, today));
		
		period.addWeekOccurrence(EventPeriod.WEDNESDAY);
		
		assertEquals(dateFormat.parse("14.07.2014"), period.getNextOccurrence(startDate, today));
		
		period.addWeekOccurrence(EventPeriod.SATURDAY);
		
		assertEquals(dateFormat.parse("05.07.2014"), period.getNextOccurrence(startDate, today));
			
	}
	
	public void testGetNextOccurrenceTypeMonthly() throws ParseException {
		
		EventPeriod period = new EventPeriod();
		period.interval = 8;
		period.type = EventPeriod.Type.MONTHLY;
		Date startDate = dateFormat.parse("2.07.2014");
		
		Date today = dateFormat.parse("2.07.2014");
		
		assertEquals(dateFormat.parse("2.03.2015"), period.getNextOccurrence(startDate, today));
	
	}
	
	public void testGetNextOccurrenceTypeYearly() throws ParseException {
		
		EventPeriod period = new EventPeriod();
		period.interval = 2;
		period.type = EventPeriod.Type.YEARLY;
		Date startDate = dateFormat.parse("02.07.2014");
		
		Date today = dateFormat.parse("03.08.2015");
		
		assertEquals(dateFormat.parse("02.07.2016"), period.getNextOccurrence(startDate, today));
		
		period.endDate = dateFormat.parse("03.07.2016");
		
		assertEquals(dateFormat.parse("02.07.2016"), period.getNextOccurrence(startDate, today));
		
		period.endDate = dateFormat.parse("02.07.2016");
		
		assertNull(period.getNextOccurrence(startDate, today));
		
	}
	
	
	public void testEquals() throws ParseException {
		EventPeriod period1 = new EventPeriod();
		EventPeriod period2 = new EventPeriod();
		
		assertTrue(period1.equals(period2));
		
		period1.type = EventPeriod.Type.DAILY;
		
		assertFalse(period1.equals(period2));
		
		period2.type = period1.type;
		
		assertTrue(period1.equals(period2));
		
		period1.interval = 2;
		
		assertFalse(period1.equals(period2));
		
		period2.interval = period1.interval;
		
		assertTrue(period1.equals(period2));
		
		period1.endDate = dateFormat.parse("30.12.2013");
		
		assertFalse(period1.equals(period2));
		
		period2.endDate = period1.endDate;
		
		assertTrue(period1.equals(period2));
		
		period1.type = EventPeriod.Type.NONE;
		period2.type = period1.type;
		period2.endDate = null;
		period2.interval = 0;
		
		assertTrue(period1.equals(period2));
}
	
	
}
