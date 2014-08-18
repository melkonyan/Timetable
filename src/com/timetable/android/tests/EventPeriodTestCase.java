package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.timetable.android.EventPeriod;
import com.timetable.android.utils.DateFormatFactory;

public class EventPeriodTestCase extends TestCase {

	private SimpleDateFormat dateFormat = DateFormatFactory.getDateFormat();
	
	private SimpleDateFormat dateTimeFormat = DateFormatFactory.getDateTimeFormat();
	
	public void testGetTypeInt() {
		EventPeriod period = new EventPeriod();
		period.setType(EventPeriod.DAILY);
		
		period.setType(period.getTypeInt());
		
		assertEquals(EventPeriod.DAILY, period.getType());
		
	}
	
	public void testIsFinished() throws ParseException {
		EventPeriod period = new EventPeriod(); 
		period.setType(EventPeriod.DAILY);
		period.setInterval(1);
		
		Date today = dateTimeFormat.parse("01.08.2014 18:47");
		
		assertFalse(period.isFinished(today));
		
		period.setEndDate("01.08.2014");
		
		assertTrue(period.isFinished(today));
		
		period.setEndDate("02.08.2014");
		
		assertFalse(period.isFinished(today));
	}
	
	public void testPeriodWeekly() {
		EventPeriod period = new EventPeriod();;
		period.setType(EventPeriod.WEEKLY);
		
		period.setWeekOccurrences(2);
		
		assertTrue(period.getWeekOccurrences()[5]);
		
		period.getWeekOccurrences()[4] = true;
		
		assertEquals(6, period.getWeekOccurrencesInt());
		
		period.addWeekOccurrence(EventPeriod.TUESDAY);
		
		assertTrue(period.getWeekOccurrences()[2]);
		
		assertTrue(period.isWeekOccurrence(EventPeriod.TUESDAY));
		
		period.deleteWeekOccurrence(EventPeriod.TUESDAY);
		
		assertFalse(period.isWeekOccurrence(EventPeriod.TUESDAY));
	}
	
	/*
	 * Test that function hasOccurrenceOnDate works normally for period, that does not repeat.
	 */
	public void testHasOccurrenceOnDateTypeNone() throws ParseException {
		EventPeriod period = new EventPeriod();
		Date searchDate = dateFormat.parse("27.12.2013");
		Date startDate = dateFormat.parse("27.12.2013");
		assertTrue(period.hasOccurrenceOnDate(startDate,searchDate));
		
		searchDate = dateTimeFormat.parse("27.12.2013 23:59");
		
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
		period.setType(EventPeriod.DAILY);
		period.setInterval(1);
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setInterval(2);
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setInterval(3);
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setEndDate("26.12.2013");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setEndDate("27.12.2013");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		//searchDate = dateTimeFormat.parse("27.12.2013 17:00");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setEndDate("28.12.2013");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		// testing leap years
		
		period.deleteEndDate();
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
		period.setType(EventPeriod.WEEKLY);
		period.setInterval(1);
		Date startDate = dateFormat.parse("29.11.2013"); //Saturday
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.addWeekOccurrence(EventPeriod.SUNDAY); // every Sunday
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.addWeekOccurrence(EventPeriod.SATURDAY); // every Saturday
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setInterval(2);
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		searchDate = dateFormat.parse("11.01.2014");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setEndDate("10.01.2014");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.deleteEndDate();
		period.setInterval(2);
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
	public void testhasOccurrenceOnDateTypeMonthly() throws ParseException {
		Date searchDate = dateFormat.parse("29.01.2014");
		EventPeriod period = new EventPeriod();;
		Date startDate = dateFormat.parse("29.12.2013");
		period.setType(EventPeriod.MONTHLY);
		period.setInterval(1);
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setInterval(2);
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		searchDate = dateFormat.parse("29.12.2015");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setInterval(6);
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
	
		period.setEndDate("29.12.2014");
		
		assertFalse(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setEndDate("30.12.2015");

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
		period.setType(EventPeriod.YEARLY);
		period.setInterval(1);
		Date searchDate = dateFormat.parse("29.12.2014");
		
		assertTrue(period.hasOccurrenceOnDate(startDate, searchDate));
		
		period.setInterval(3);
		
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
			assertEquals(today, period.getNextOccurrence(startDate, today));
			
			today = dateFormat.parse("09.03.20147");
			assertNull(period.getNextOccurrence(startDate, today));
	}
	
	public void testGetNextOccurrenceTypeDaily() throws ParseException {	
		EventPeriod period = new EventPeriod();
		period.setType(EventPeriod.DAILY);
		period.setInterval(1);
		Date startDate = dateFormat.parse("12.05.2014");
		
		Date today = dateFormat.parse("11.05.2014");
		assertEquals(startDate, period.getNextOccurrence(startDate, today));
		
		today = dateFormat.parse("12.05.2014");
		assertEquals(today, period.getNextOccurrence(startDate, today));
		
		today = dateFormat.parse("13.05.2014");
		assertEquals(today, period.getNextOccurrence(startDate, today));
		
		period.setInterval(6);
		
		today = dateFormat.parse("13.05.2014");
		assertEquals(dateFormat.parse("18.05.2014"), period.getNextOccurrence(startDate, today));
		
		startDate = dateFormat.parse("30.12.2014");
		
		today = dateFormat.parse("31.12.2014");
		assertEquals(dateFormat.parse("05.01.2015"), period.getNextOccurrence(startDate, today));
		
		period.setEndDate("03.01.2015");
		assertNull(period.getNextOccurrence(startDate, today));
	}
	/*
	 * Test function getNextOccurrence for weekly period, assuming, that period should not have occurrence on it's startDate
	 */
	public void testGetNextOccurrenceTypeWeekly() throws ParseException {
		EventPeriod period = new EventPeriod();
		period.setInterval(1);
		period.setType(EventPeriod.WEEKLY);
		period.addWeekOccurrence(EventPeriod.TUESDAY);
		
		Date startDate = dateFormat.parse("22.07.2014");
		Date today = dateFormat.parse("31.07.2014");
		
		period.getNextOccurrence(startDate, today);
		assertEquals(dateFormat.parse("05.08.2014"), period.getNextOccurrence(startDate, today));
		
		period.addWeekOccurrence(EventPeriod.FRIDAY);
		
		assertEquals(dateFormat.parse("01.08.2014"), period.getNextOccurrence(startDate, today));
		
		startDate = dateFormat.parse("29.07.2014");
		period.setInterval(2);
		
		assertEquals(dateFormat.parse("01.08.2014"), period.getNextOccurrence(startDate, today));
		
		period.deleteWeekOccurrence(EventPeriod.FRIDAY);
		
		assertEquals(dateFormat.parse("12.08.2014"), period.getNextOccurrence(startDate, today));
		
		period.setEndDate("12.08.2014");
		
		assertNull(period.getNextOccurrence(startDate, today));
		
	}
	
	public void testGetNextOccurrenceTypeMonthly() throws ParseException {
		
		EventPeriod period = new EventPeriod();
		period.setInterval(8);
		period.setType(EventPeriod.MONTHLY);
		Date startDate = dateFormat.parse("2.07.2014");
		
		Date today = dateFormat.parse("3.07.2014");
		
		assertEquals(dateFormat.parse("2.03.2015"), period.getNextOccurrence(startDate, today));
	
	}
	
	public void testGetNextOccurrenceTypeYearly() throws ParseException {
		
		EventPeriod period = new EventPeriod();
		period.setInterval(2);
		period.setType(EventPeriod.YEARLY);
		Date startDate = dateFormat.parse("02.07.2014");
		
		Date today = dateFormat.parse("03.08.2015");
		
		assertEquals(dateFormat.parse("02.07.2016"), period.getNextOccurrence(startDate, today));
		
		period.setEndDate("03.07.2016");
		
		assertEquals(dateFormat.parse("02.07.2016"), period.getNextOccurrence(startDate, today));
		
		period.setEndDate("02.07.2016");
		
		assertNull(period.getNextOccurrence(startDate, today));
		
	}
	
	
	public void testEquals() throws ParseException {
		EventPeriod period1 = new EventPeriod();
		EventPeriod period2 = new EventPeriod();
		
		assertTrue(period1.equals(period2));
		
		period1.setType(EventPeriod.DAILY);
		
		assertFalse(period1.equals(period2));
		
		period2.setType(period1.getType());
		
		assertTrue(period1.equals(period2));
		
		period1.setInterval(2);
		
		assertFalse(period1.equals(period2));
		
		period2.setInterval(period1.getInterval());
		
		assertTrue(period1.equals(period2));
		
		period1.setEndDate("30.12.2013");
		
		assertFalse(period1.equals(period2));
		
		period2.setEndDate(period1.getEndDate());
		
		assertTrue(period1.equals(period2));
		
		period1.setType(EventPeriod.NONE);
		period2.setType(period1.getType());
		period2.deleteEndDate();
		period2.setInterval(0);
		
		assertTrue(period1.equals(period2));
}
	
	
}
