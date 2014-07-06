package com.timetable.app.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.timetable.app.Event;
import com.timetable.app.EventPeriod;
import com.timetable.app.TimetableLogger;
import com.timetable.app.alarm.EventAlarm;

public class EventAlarmTestCase extends TestCase {
	
	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private Date alarmTime;
	
	@Override
	public void setUp() {
		try {
		
		alarmTime = dateTimeFormat.parse("24.04.2014 14:15");
		} catch (ParseException e) {
			fail("Could not parse alarm time");
		}
	}
	
	public void testGetNextOccurrencePeriodNone() {
		try {
			EventAlarm alarm = new EventAlarm();
			alarm.time = dateTimeFormat.parse("12.05.2014 15:35");
			
			Date today = dateTimeFormat.parse("09.02.2013 15:35");
			assertEquals(alarm.time, alarm.getNextOccurrence(today));
			
			today = dateTimeFormat.parse("11.05.2014 15:35");
			assertEquals(alarm.time, alarm.getNextOccurrence(today));
			
			today = dateTimeFormat.parse("11.05.2014 16:35");
			assertEquals(alarm.time, alarm.getNextOccurrence(today));
			
			today = dateTimeFormat.parse("12.05.2014 15:35");
			assertNull(alarm.getNextOccurrence(today));
			
			today = dateTimeFormat.parse("12.05.2014 16:35");
			assertNull(alarm.getNextOccurrence(today));
			
			today = dateTimeFormat.parse("09.03.20147 10:00");
			assertNull(alarm.getNextOccurrence(today));
						
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testGetNextOccurrencePeriodDaily() {
		try {
			
			EventAlarm alarm = new EventAlarm();
			alarm.period.type = EventPeriod.Type.DAILY;
			alarm.period.interval = 1;
			alarm.time = dateTimeFormat.parse("12.05.2014 15:46");
			
			Date today = dateTimeFormat.parse("11.05.2014 15:46");
			assertEquals(alarm.time, alarm.getNextOccurrence(today));
			
			today = dateTimeFormat.parse("12.05.2014 18:46");
			assertEquals(dateTimeFormat.parse("13.05.2014 15:46"), alarm.getNextOccurrence(today));
			
			today = dateTimeFormat.parse("13.05.2014 12:46");
			assertEquals(dateTimeFormat.parse("13.05.2014 15:46"), alarm.getNextOccurrence(today));
			
			today = dateTimeFormat.parse("13.05.2014 16:46");
			assertEquals(dateTimeFormat.parse("14.05.2014 15:46"), alarm.getNextOccurrence(today));
			
			alarm.period.interval = 6;
			
			today = dateTimeFormat.parse("12.05.2014 16:22");
			assertEquals(dateTimeFormat.parse("18.05.2014 15:46"), alarm.getNextOccurrence(today));
			
			alarm.time = dateTimeFormat.parse("30.12.2014 20:00");
			
			today = dateTimeFormat.parse("31.12.2014 17:00");
			assertEquals(dateTimeFormat.parse("05.01.2015 20:00"), alarm.getNextOccurrence(today));
			
			alarm.period.endDate = dateFormat.parse("03.01.2015");
			assertNull(alarm.getNextOccurrence(today));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testGetNextOccurrencePeriodWeekly() throws ParseException {
		EventAlarm alarm = new EventAlarm();
		alarm.period.interval = 1;
		alarm.period.type = EventPeriod.Type.WEEKLY;
		alarm.period.addWeekOccurrence(EventPeriod.SUNDAY);
		alarm.time = EventAlarm.timeFormat.parse("9.06.2014 16:09");//Monday
		
		Date today = dateTimeFormat.parse("6.06.2014 12:00");//Friday
		
		assertEquals(dateTimeFormat.parse("15.06.2014 16:09"), alarm.getNextOccurrence(today));
		
		alarm.period.addWeekOccurrence(EventPeriod.MONDAY);
		
		assertEquals(dateTimeFormat.parse("09.06.2014 16:09"), alarm.getNextOccurrence(today));
		
		alarm.period.deleteWeekOccurrence(EventPeriod.MONDAY);
		alarm.period.addWeekOccurrence(EventPeriod.WEDNESDAY);
		alarm.period.interval = 3;
		
		assertEquals(dateTimeFormat.parse("11.06.2014 16:09"), alarm.getNextOccurrence(today));
		
		alarm.period.addWeekOccurrence(EventPeriod.MONDAY);
		alarm.period.deleteWeekOccurrence(EventPeriod.WEDNESDAY);
		alarm.period.interval = 3;
		
		today = dateTimeFormat.parse("9.06.2014 16:09");//Monday
		
		assertEquals(dateTimeFormat.parse("15.06.2014 16:09"), alarm.getNextOccurrence(today));
		
		alarm.period.addWeekOccurrence(EventPeriod.WEDNESDAY);
		
		assertEquals(dateTimeFormat.parse("11.06.2014 16:09"), alarm.getNextOccurrence(today));
		
		today = dateTimeFormat.parse("03.07.2014 14:11");
		alarm.time = EventAlarm.timeFormat.parse("30.06.2014 16:09");
		alarm.period.interval = 2;
		alarm.period.deleteWeekOccurrence(EventPeriod.WEDNESDAY);
		alarm.period.deleteWeekOccurrence(EventPeriod.SUNDAY);
		alarm.getNextOccurrence(today);
		assertEquals(dateTimeFormat.parse("14.07.2014 16:09"), alarm.getNextOccurrence(today));
		
		alarm.period.addWeekOccurrence(EventPeriod.WEDNESDAY);
		
		assertEquals(dateTimeFormat.parse("14.07.2014 16:09"), alarm.getNextOccurrence(today));
		
		alarm.period.addWeekOccurrence(EventPeriod.SATURDAY);
		
		assertEquals(dateTimeFormat.parse("05.07.2014 16:09"), alarm.getNextOccurrence(today));
		
		
		
		
	}
	
	public void testGetNextOccurrencePeriodMonthly() throws ParseException {
		
		EventAlarm alarm = new EventAlarm();
		alarm.period.interval = 8;
		alarm.period.type = EventPeriod.Type.MONTHLY;
		alarm.time = EventAlarm.timeFormat.parse("2.07.2014 18:56");
		
		Date today = dateTimeFormat.parse("2.07.2014 18:56");
		
		assertEquals(dateTimeFormat.parse("2.03.2015 18:56"), alarm.getNextOccurrence(today));
	
	}
	
	public void testGetNextOccurrencePeriodYearly() throws ParseException {
		
		EventAlarm alarm = new EventAlarm();
		alarm.period.interval = 2;
		alarm.period.type = EventPeriod.Type.YEARLY;
		alarm.time = EventAlarm.timeFormat.parse("02.07.2014 19:00");
		
		Date today = dateTimeFormat.parse("03.08.2015 15:46");
		
		assertEquals(dateTimeFormat.parse("02.07.2016 19:00"), alarm.getNextOccurrence(today));
		
		alarm.period.endDate = dateFormat.parse("03.07.2016");
		
		assertEquals(dateTimeFormat.parse("02.07.2016 19:00"), alarm.getNextOccurrence(today));
		
		alarm.period.endDate = dateFormat.parse("02.07.2016");
		
		assertNull(alarm.getNextOccurrence(today));
		
	}
	
	/*public void testGetEventOccurrence() throws ParseException {
		EventAlarm alarm = new EventAlarm();
		alarm.time = EventAlarm.timeFormat.parse("06.07.2014 15:47");
		Event event = new Event();
		event.date = dateFormat.parse("10.07.2014");
		alarm.event = event;
		assertEquals(dateFormat.parse("10.07.2014"), alarm.getEventOccurrence(alarm.time));
		
	}*/
	public void testIsOk() {
		EventAlarm alarm = new EventAlarm();
		assertEquals(false, alarm.isOk());
		alarm.time = alarmTime;
		assertEquals(true, alarm.isOk());
		alarm.type = null;
		assertEquals(false, alarm.isOk());
	}
	
	public void testEquals() {
		EventAlarm alarm1 = new EventAlarm(), alarm2 = new EventAlarm();
		assertEquals(true, alarm1.equals(alarm2));
		alarm1.time = alarmTime;
		assertEquals(false, alarm1.equals(alarm2));
		alarm2.time = alarm1.time;
		assertEquals(true, alarm1.equals(alarm2));
		alarm1.id = 10;
		assertEquals(false, alarm1.equals(alarm2));
		alarm2.id = alarm1.id;
		assertEquals(true, alarm1.equals(alarm2));
		alarm1.eventId = 20;
		assertEquals(false, alarm1.equals(alarm2));
		alarm2.eventId = alarm1.eventId;
		assertEquals(true, alarm1.equals(alarm2));
	}
}
