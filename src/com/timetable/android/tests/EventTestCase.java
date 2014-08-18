package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;
import android.os.Bundle;

import com.timetable.android.Event;
import com.timetable.android.EventPeriod;
import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.utils.DateFormatFactory;

public class EventTestCase extends TestCase {
	
	SimpleDateFormat dateFormat = DateFormatFactory.getDateFormat();
	
	SimpleDateFormat timeFormat = DateFormatFactory.getTimeFormat();
	
	SimpleDateFormat dateTimeFormat = DateFormatFactory.getDateTimeFormat();
	
	public void testContentValues() throws ParseException {
		Event event = new Event.Builder()
						.setId(20)
						.setName("name")
						.setPlace("place")
						.setDate("09.08.2014")
						.setStartTime("11:03")
						.setEndTime("12:03")
						.setMuteDevice(true)
						.setNote("note")
						.addException("09.08.2014")
						.setPeriodType(EventPeriod.DAILY)
						.setPeriodInterval(1)
						.setPeriodEndDate("09.08.2014")
						.setAlarmTime("08.08.2014 11:37")
						.build();
		
		Bundle eventData = event.convert();
		assertEquals(event, new Event(eventData));
		
		event.deleteEndTime();
		event.getPeriod().deleteEndDate();
		eventData = event.convert();
		
		assertEquals(event, new Event(eventData));
		
		event.deleteAlarm();
		eventData = event.convert();
		
		assertEquals(event, new Event(eventData));
	}
	
	public void testIsException() throws ParseException {
		Event event = new Event.Builder().build();
		Date exception = dateFormat.parse("31.07.2014");
		event.addException(exception);
		assertTrue(event.isException(exception));
		
	}
	
	public void testGetNextStartTime() throws ParseException {
		Event event = new Event.Builder().setDate(dateFormat.parse("07.08.2014")).setStartTime(timeFormat.parse("00:59:00")).build();
		
		Date currentTime = dateTimeFormat.parse("07.08.2014 00:57");
		
		assertEquals(dateTimeFormat.parse("07.08.2014 00:59"), event.getNextStartTime(currentTime));
		
		currentTime = dateTimeFormat.parse("07.08.2014 01:59");
		
		assertNull(event.getNextStartTime(currentTime));
		
		currentTime = dateTimeFormat.parse("08.08.2014 00:15");
		
		assertNull(event.getNextEndTime(currentTime));
		
		event.getPeriod().setType(EventPeriod.DAILY);
		event.getPeriod().setInterval(1);
		event.getNextStartTime(currentTime);
		event.getNextStartTime(currentTime);
		assertEquals(dateTimeFormat.parse("08.08.2014 00:59"), event.getNextStartTime(currentTime));
		
	}
	
	public void testGetNextEndTime() throws ParseException {
		Event event = new Event.Builder().setDate(dateFormat.parse("07.08.2014")).build();
		Date currentTime = dateTimeFormat.parse("07.08.2014 1:04");
		
		assertNull(event.getNextEndTime(currentTime));
	
		event.setEndTime("01:21:00");
		
		assertEquals(dateTimeFormat.parse("07.08.2014 01:21"), event.getNextEndTime(currentTime));
	}
	
	public void testIsTodayPeriodNone() throws ParseException {
		Event event = new Event.Builder().setDate(dateFormat.parse("27.12.2013")).build();
		
		Date searchDate = dateFormat.parse("27.12.2013");
		
		assertTrue(event.isToday(searchDate));
	
		event.setDate("24.12.2013");
		
		assertFalse(event.isToday(searchDate));
	}
	
	public void testIsTodayPeriodDaily() throws ParseException {
		Event event = new Event.Builder()
						.setDate(dateFormat.parse("24.12.2013"))
						.setPeriodType(EventPeriod.Type.DAILY)
						.setPeriodInterval(1)
						.build();
		Date searchDate = dateFormat.parse("27.12.2013");
		
		assertTrue(event.isToday(searchDate));
		
		event.addException(searchDate);
		
		assertFalse(event.isToday(searchDate));
		
		event.getPeriod().setInterval(2);
		
		assertFalse(event.isToday(searchDate));
		
		event.getExceptions().clear();
		event.getPeriod().setInterval(3);
		
		assertTrue(event.isToday(searchDate));
		
		event.getPeriod().setEndDate("26.12.2013");
		event.setStartTime("23:59:59");
		
		assertFalse(event.isToday(searchDate));
		
		event.getPeriod().setEndDate("27.12.2013");
		
		assertFalse(event.isToday(searchDate));
		
		event.getPeriod().setEndDate("28.12.2013");
		
		assertTrue(event.isToday(searchDate));
		
		// testing leap years
		
		event.getPeriod().deleteEndDate();
		event.setDate("27.02.2012");
		searchDate = dateFormat.parse("01.03.2012");
		
		assertTrue(event.isToday(searchDate));
		
		event.setDate("27.02.1900"); //not a leap year
		searchDate = dateFormat.parse("01.03.1900"); 
		
		assertFalse(event.isToday(searchDate));
}
	
	public void testIsTodayPeriodWeekly() throws ParseException {
		Date searchDate = dateFormat.parse("04.01.2014"); //Saturday
		Event event = new Event.Builder()
						.setPeriodType(EventPeriod.Type.WEEKLY)
						.setPeriodInterval(1)
						.setDate(dateFormat.parse("29.11.2013")) //Saturday
						.build();
		
		
		assertFalse(event.isToday(searchDate));
		
		event.getPeriod().addWeekOccurrence(EventPeriod.SUNDAY); // every Sunday
		
		assertFalse(event.isToday(searchDate));
		
		event.getPeriod().addWeekOccurrence(EventPeriod.SATURDAY); // every Saturday
		
		assertTrue(event.isToday(searchDate));
		
		event.getPeriod().setInterval(2);
		
		assertFalse(event.isToday(searchDate));
		
		searchDate = dateFormat.parse("11.01.2014");
		
		assertTrue(event.isToday(searchDate));
		
		event.getPeriod().setEndDate("10.01.2014");
		
		assertFalse(event.isToday(searchDate));
		
		event.getPeriod().deleteEndDate();
		event.getPeriod().setInterval(2);
		event.setDate("30.06.2014");
		event.getPeriod().addWeekOccurrence(EventPeriod.MONDAY);
		searchDate = dateFormat.parse("07.07.2014");
		
		assertFalse(event.isToday(searchDate));
		
		searchDate = dateFormat.parse("14.07.2014");
		
		assertTrue(event.isToday(searchDate));
		
	}
	
	public void testIsTodayPeriodMonthly() throws ParseException {
		Date searchDate = dateFormat.parse("29.01.2014");
		Event event = new Event.Builder()
						.setDate("29.12.2013")
						.setPeriodType(EventPeriod.MONTHLY)
						.setPeriodInterval(1)
						.build();
		
		assertTrue(event.isToday(searchDate));
		
		event.getPeriod().setInterval(2);
		
		assertFalse(event.isToday(searchDate));
		
		searchDate = dateFormat.parse("29.12.2015");
		
		assertTrue(event.isToday(searchDate));
		
		event.getPeriod().setInterval(6);
		
		assertTrue(event.isToday(searchDate));
	
		event.getPeriod().setEndDate("29.12.2014");
		
		assertFalse(event.isToday(searchDate));
		
		event.getPeriod().setEndDate("30.12.2015");

		assertTrue(event.isToday(searchDate));
		
		event.setDate("30.12.2013");

		assertFalse(event.isToday(searchDate));
	}
	
	public void testIsTodayPeriodYearly() throws ParseException {
		
		Event event = new Event.Builder()
						.setDate("29.12.2013")
						.setPeriodType(EventPeriod.YEARLY)
						.setPeriodInterval(1)
						.build();
		
		Date searchDate = dateFormat.parse("29.12.2014");
		
		assertTrue(event.isToday(searchDate));
		
		event.getPeriod().setInterval(3);
		
		assertFalse(event.isToday(searchDate));
		
		searchDate = dateFormat.parse("29.12.2019");
		
		assertTrue(event.isToday(searchDate));
		
		event.setDate("30.12.2013");
		
		assertFalse(event.isToday(searchDate));
		
		event.setDate("29.11.2013");
		
		assertFalse(event.isToday(searchDate));
		
	}
	
	
	public void testIsOk() throws ParseException {
		
		Event event = new Event();
		
		assertFalse(event.isOk());
		
		event.setDate("29.12.2013");
		
		assertFalse(event.isOk());
		
		event.setStartTime("17:00:00");
		
		assertFalse(event.isOk());
		
		event.setName("Name");
		
		assertTrue(event.isOk());
		
		event.setName(null);
		
		assertFalse(event.isOk());
		
		event.setName("Name");
		event.setPlace(null);
		
		assertFalse(event.isOk());
		
		event.setPlace("Place");
		event.setNote(null);
		
		assertFalse(event.isOk());
		
		event.setNote("Note");
		event.getPeriod().setType(EventPeriod.MONTHLY);
		
		assertFalse(event.isOk());
		
		event.getPeriod().setInterval(2);
		
		assertTrue(event.isOk());
		
		event.getPeriod().setType(EventPeriod.WEEKLY);
		
		assertTrue(event.isOk());
		
		event.getPeriod().setWeekOccurrences(0);
		
		assertTrue(event.isOk());
		
		event.setName("");
		for (int i = 0; i < Event.MAX_NAME_LENGTH; i++) {
			event.setName(event.getName().concat("a"));
		}
		
		assertTrue(event.isOk());
		
		event.setName(event.getName().concat("a"));
		
		assertFalse(event.isOk());
		
		event.setName("Name");
		event.setPlace("");
		for (int i = 0; i < Event.MAX_PLACE_LENGTH; i++) {
			event.setPlace(event.getPlace().concat("a"));
		}
		
		assertTrue(event.isOk());
		
		event.setPlace(event.getPlace().concat("a"));
		
		assertFalse(event.isOk());
		
		event.setPlace("Place");
		event.setNote("");
		for (int i = 0; i < Event.MAX_NOTE_LENGTH; i++) {
			event.setNote(event.getNote().concat("a"));
		}
		
		assertTrue(event.isOk());
		
		event.setNote(event.getNote().concat("a"));
		
		assertFalse(event.isOk());
	}
	
	public void testEquals() throws ParseException {
		Event event1 = new Event.Builder()
							.setName("Name")
							.setDate("30.12.2013")
							.setStartTime("11:00")
							.build();
							
		Event event2 = new Event.Builder()
							.setName(event1.getName())
							.setDate(event1.getDate())
							.setStartTime(event1.getStartTime())
							.build();
		
		assertTrue(event1.equals(event2));
		
		event2.setName("Name2");
		
		assertFalse(event1.equals(event2));
		
		event2.setName(event1.getName());
		event2.setDate("31.12.2013");
		
		assertFalse(event1.equals(event2));
		
		event2.setDate(event1.getDate());
		event2.setStartTime("11:01");
		
		assertFalse(event1.equals(event2));
		
		event2.setStartTime(event1.getStartTime());
		event1.setAlarm(new EventAlarm(event1));
		
		assertFalse(event1.equals(event2));
		
		event2.setAlarm(event1.getAlarm());
		
		assertTrue(event1.equals(event2));
	}
	
}
