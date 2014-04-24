package com.timetable.app.tests;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.timetable.app.Event;
import com.timetable.app.EventAlarm;
import com.timetable.app.EventPeriod;

public class EventTestCase extends TestCase {
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	public void setUp() {
		
	}
	
	public void testIsTodayPeriodNone() {
		try {
			Event event = new Event();
			event.period = new EventPeriod();
			Date searchDate = dateFormat.parse("27.12.2013");
			event.date = dateFormat.parse("27.12.2013");
			
			assertEquals(true, event.isToday(searchDate));
		
			event.date = dateFormat.parse("24.12.2013");
			
			assertEquals(false, event.isToday(searchDate));
		
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testIsTodayPeriodDaily() {
		try {
			Event event = new Event();
			Date searchDate = dateFormat.parse("27.12.2013");
			event.date = dateFormat.parse("24.12.2013");
			event.period.type = EventPeriod.Type.DAILY;
			event.period.interval = 1;
			
			assertEquals(true, event.isToday(searchDate));
			
			event.period.interval = 2;
			
			assertEquals(false, event.isToday(searchDate));
			
			event.period.interval = 3;
			
			assertEquals(true, event.isToday(searchDate));
			
			event.period.endDate = dateFormat.parse("26.12.2013");
			event.startTime = timeFormat.parse("23:59:59");
			
			assertEquals(false, event.isToday(searchDate));
			
			event.period.endDate = dateFormat.parse("27.12.2013");
			
			assertEquals(true, event.isToday(searchDate));
			
			event.period.endDate = dateFormat.parse("28.12.2013");
			
			assertEquals(true, event.isToday(searchDate));
			
			// testing leap years
			
			event.period.endDate = null;
			event.date = dateFormat.parse("27.02.2012");
			searchDate = dateFormat.parse("01.03.2012");
			
			assertEquals(true, event.isToday(searchDate));
			
			event.date = dateFormat.parse("27.02.1900"); //not a leap year
			searchDate = dateFormat.parse("01.03.1900"); 
			
			assertEquals(false, event.isToday(searchDate));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testIsTodayPeriodWeekly() {
		try {
			Date searchDate = dateFormat.parse("04.01.2014"); //Saturday
			Event event = new Event();
			event.period.type = EventPeriod.Type.WEEKLY;
			event.period.interval = 1;
			event.date = dateFormat.parse("29.11.2013"); //Saturday
			
			assertEquals(false, event.isToday(searchDate));
			
			event.period.setWeekOccurrences(64); // every Sunday
			
			assertEquals(false, event.isToday(searchDate));
			
			event.period.setWeekOccurrences(1); // every Saturday
			
			assertEquals(true, event.isToday(searchDate));
			
			event.period.interval = 2;
			
			assertEquals(false, event.isToday(searchDate));
			
			searchDate = dateFormat.parse("11.01.2014");
			
			assertEquals(true, event.isToday(searchDate));
			
			event.period.endDate = dateFormat.parse("10.01.2014");
			
			assertEquals(false, event.isToday(searchDate));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testIsTodayPeriodMonthly() {
		try {
			Date searchDate = dateFormat.parse("29.01.2014");
			Event event = new Event();
			event.date = dateFormat.parse("29.12.2013");
			event.period.type = EventPeriod.Type.MONTHLY;
			event.period.interval = 1;
			
			assertEquals(true, event.isToday(searchDate));
			
			event.period.interval = 2;
			
			assertEquals(false, event.isToday(searchDate));
			
			searchDate = dateFormat.parse("29.12.2015");
			
			assertEquals(true, event.isToday(searchDate));
			
			event.period.interval = 6;
			
			assertEquals(true, event.isToday(searchDate));
		
			event.period.endDate = dateFormat.parse("29.12.2014");
			
			assertEquals(false, event.isToday(searchDate));
			
			event.period.endDate = dateFormat.parse("30.12.2015");

			assertEquals(true, event.isToday(searchDate));
			
			event.date = dateFormat.parse("30.12.2013");

			assertEquals(false, event.isToday(searchDate));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testIsTodayPeriodYearly() {
		try {
			Event event = new Event();
			event.date = dateFormat.parse("29.12.2013");
			event.period.type = EventPeriod.Type.YEARLY;
			event.period.interval = 1;
			Date searchDate = dateFormat.parse("29.12.2014");
			
			assertEquals(true, event.isToday(searchDate));
			
			event.period.interval = 3;
			
			assertEquals(false, event.isToday(searchDate));
			
			searchDate = dateFormat.parse("29.12.2019");
			
			assertEquals(true, event.isToday(searchDate));
			
			event.date = dateFormat.parse("30.12.2013");
			
			assertEquals(false, event.isToday(searchDate));
			
			event.date = dateFormat.parse("29.11.2013");
			
			assertEquals(false, event.isToday(searchDate));
			
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testIsOk() {
		try {
			Event event = new Event();
			
			assertEquals(false, event.isOk());
			
			event.date = dateFormat.parse("29.12.2013");
			
			assertEquals(false, event.isOk());
			
			event.startTime = timeFormat.parse("17:00:00");
			
			assertEquals(false, event.isOk());
			
			event.name = "Name";
			
			assertEquals(true, event.isOk());
			
			event.name = null;
			
			assertEquals(false, event.isOk());
			
			event.name = "Name";
			event.place = null;
			
			assertEquals(false, event.isOk());
			
			event.place = "Place";
			event.note = null;
			
			assertEquals(false, event.isOk());
			
			event.note = "Note";
			event.period.type =  EventPeriod.Type.MONTHLY;
			
			assertEquals(false, event.isOk());
			
			event.period.interval = 2;
			
			assertEquals(true, event.isOk());
			
			event.period.type = EventPeriod.Type.WEEKLY;
			
			assertEquals(false, event.isOk());
			
			event.period.setWeekOccurrences(0);
			
			assertEquals(true, event.isOk());
			
			event.name = "";
			for (int i = 0; i < Event.MAX_NAME_LENGTH; i++) {
				event.name = event.name.concat("a");
			}
			
			assertEquals(true, event.isOk());
			
			event.name = event.name.concat("a");
			
			assertEquals(false, event.isOk());
			
			event.name = "Name";
			event.place = "";
			for (int i = 0; i < Event.MAX_PLACE_LENGTH; i++) {
				event.place = event.place.concat("a");
			}
			
			assertEquals(true, event.isOk());
			
			event.place = event.place.concat("a");
			
			assertEquals(false, event.isOk());
			
			event.place = "Place";
			event.note = "";
			for (int i = 0; i < Event.MAX_NOTE_LENGTH; i++) {
				event.note = event.note.concat("a");
			}
			
			assertEquals(true, event.isOk());
			
			event.note = event.note.concat("a");
			
			assertEquals(false, event.isOk());
			
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testEquals() {
		try {
			Event event1 = new Event();
			event1.name = "Name";
			event1.date = dateFormat.parse("30.12.2013");
			event1.startTime = timeFormat.parse("11:00:00");
			
			Event event2 = new Event();
			event2.name = event1.name;
			event2.date = event1.date;
			event2.startTime = event1.startTime;
			
			assertEquals(true, event1.equals(event2));
			
			event2.name = "Name2";
			
			assertEquals(false, event1.equals(event2));
			
			event2.name = event1.name;
			event2.date = dateFormat.parse("31.12.2013");
			
			assertEquals(false, event1.equals(event2));
			
			event2.date = event1.date;
			event2.startTime = timeFormat.parse("11:00:01");
			
			assertEquals(false, event1.equals(event2));
			
			event2.startTime = event1.startTime;
			event1.alarm = new EventAlarm();
			
			assertEquals(false, event1.equals(event2));
			
			event2.alarm = event1.alarm;
			
			assertEquals(true, event1.equals(event2));
			
			
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}
	public void tearDown() {
		
	}
}
